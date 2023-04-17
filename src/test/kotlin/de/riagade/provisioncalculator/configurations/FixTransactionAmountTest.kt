package de.riagade.provisioncalculator.configurations

import de.riagade.provisioncalculator.entities.Broker
import de.riagade.provisioncalculator.entities.Transaction
import de.riagade.provisioncalculator.infra.*
import org.junit.jupiter.api.*

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

class FixTransactionAmountTest {

    private lateinit var database: MockDatabase
    private lateinit var configuration: FixTransactionAmount
    private lateinit var name: String
    private lateinit var amount: BigDecimal

    @BeforeEach
    fun setUp() {
        database = MockDatabase()
        name = randomString()
        amount = randomAmount()
        configuration = FixTransactionAmount(
            name = name,
            amount = amount
        )
    }

    @AfterEach
    fun tearDown() {
        database.clean()
    }

    @Test
    fun name_is_set() {
        assertEquals(name, configuration.name())
    }

    @ParameterizedTest
    @EnumSource(DayOfWeek::class, names = ["MONDAY"])
    fun do_calculate_on(dayOfWeek: DayOfWeek) {
        // Given
        val date = randomDate().with(dayOfWeek)

        // When
        val canBeCalculatedAt = configuration.canBeCalculatedAt(date)

        // Then
        assertTrue(canBeCalculatedAt)
    }

    @ParameterizedTest
    @EnumSource(DayOfWeek::class, names = ["TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"])
    fun dont_calculate_on(dayOfWeek: DayOfWeek) {
        // Given
        val date = randomDate().with(dayOfWeek)

        // When
        val canBeCalculatedAt = configuration.canBeCalculatedAt(date)

        // Then
        assertFalse(canBeCalculatedAt)
    }

    @Nested
    inner class CalculateConfiguration {
        private lateinit var calculationDate: LocalDate
        private lateinit var lead: LocalDateTime
        private lateinit var sale: LocalDateTime
        private lateinit var brokerCode: String
        private lateinit var broker: Broker

        @BeforeEach
        fun setUp() {
            database.configurations.add(configuration)
            calculationDate = randomDate().with(DayOfWeek.MONDAY)
            lead = calculationDate.atStartOfDay().minusMonths(1)
            sale = calculationDate.atStartOfDay()
            brokerCode = randomString()
        }

        @Nested
        inner class BrokerActive {

            @BeforeEach
            fun setUp() {
                broker = a_broker(
                    codes = listOf(brokerCode),
                    statusHistory = mapOf(lead.toLocalDate() to Broker.Status.ACTIVE),
                    database = database
                )
            }

            @ParameterizedTest
            @ValueSource(ints = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10])
            fun multiple_transactions(transactionAmount: Int) {
                // Given
                val transactions = mutableListOf<Transaction>()
                for (i in 1..transactionAmount) {
                    transactions.add(a_transaction(
                        lead = lead,
                        sale = sale,
                        brokerCode = brokerCode,
                        status = Transaction.Status.SALE,
                        database = database
                    ))
                }

                // When
                val provisions = configuration.calculate(calculationDate, database)

                // Then
                assertEquals(1, provisions.size, "provision size does not match")
                val provision = provisions.first()
                assertEquals(
                    amount.multiply(BigDecimal.valueOf(transactions.size.toLong())),
                    provision.sum,
                    "sum does not match"
                )
                assertEquals(transactions.size, provision.transactions.size, "transaction size does not match")
                provision.transactions.forEach { (t, v) ->
                    assertTrue(transactions.contains(t), "transaction does not match")
                    assertEquals(amount, v.orElseThrow(), "transaction value does not match")
                }
            }

            @Test
            fun no_sale_happened() {
                // Given
                val transactions = mutableListOf<Transaction>()
                transactions.add(a_transaction(
                    lead = lead,
                    brokerCode = brokerCode,
                    status = Transaction.Status.LEAD,
                    database = database
                ))

                // When
                val provisions = configuration.calculate(calculationDate, database)

                // Then
                assertEquals(0, provisions.size, "provision size does not match")
            }

            @Test
            fun wrong_broker_code() {
                // Given
                val transactions = mutableListOf<Transaction>()
                transactions.add(a_transaction(
                    lead = lead,
                    sale = sale,
                    brokerCode = randomString(),
                    status = Transaction.Status.SALE,
                    database = database
                ))

                // When
                val provisions = configuration.calculate(calculationDate, database)

                // Then
                assertEquals(0, provisions.size, "provision size does not match")
            }

            @Nested
            inner class WasCalculatedBefore {

                @Test
                fun not_eligible_for_provision() {
                    // Given
                    val transaction = a_transaction(
                        lead = lead,
                        sale = sale,
                        brokerCode = brokerCode,
                        status = Transaction.Status.SALE,
                        database = database
                    )
                    mark_calculated_before(
                        transaction = transaction,
                        configuration = configuration,
                        database = database
                    )

                    // When
                    val provisions = configuration.calculate(calculationDate, database)

                    // Then
                    assertEquals(0, provisions.size, "provision size does not match")
                }
            }
        }

        @Nested
        inner class BrokerActiveOnlySale {

            @BeforeEach
            fun setUp() {
                a_broker(
                    codes = listOf(brokerCode),
                    statusHistory = mapOf(
                        lead.toLocalDate() to Broker.Status.INACTIVE
                    ),
                    database = database
                )
            }

            @Test
            fun not_eligible_for_provision() {
                // Given
                a_transaction(
                    lead = lead,
                    sale = sale,
                    brokerCode = brokerCode,
                    status = Transaction.Status.SALE,
                    database = database
                )

                // When
                val provisions = configuration.calculate(calculationDate, database)

                // Then
                assertEquals(0, provisions.size, "provision size does not match")
            }
        }

        @Nested
        inner class BrokerActiveOnlyLead {

            @BeforeEach
            fun setUp() {
                a_broker(
                    codes = listOf(brokerCode),
                    statusHistory = mapOf(
                        lead.toLocalDate() to Broker.Status.ACTIVE,
                        sale.toLocalDate() to Broker.Status.INACTIVE
                    ),
                    database = database
                )
            }

            @Test
            fun not_eligible_for_provision() {
                // Given
                a_transaction(
                    lead = lead,
                    sale = sale,
                    brokerCode = brokerCode,
                    status = Transaction.Status.SALE,
                    database = database
                )

                // When
                val provisions = configuration.calculate(calculationDate, database)

                // Then
                assertEquals(0, provisions.size, "provision size does not match")
            }
        }

        @Nested
        inner class BrokerNeverActive {

            @BeforeEach
            fun setUp() {
                a_broker(
                    codes = listOf(brokerCode),
                    statusHistory = mapOf(
                        lead.toLocalDate() to Broker.Status.INACTIVE
                    ),
                    database = database
                )
            }

            @Test
            fun not_eligible_for_provision() {
                // Given
                a_transaction(
                    lead = lead,
                    sale = sale,
                    brokerCode = brokerCode,
                    status = Transaction.Status.SALE,
                    database = database
                )

                // When
                val provisions = configuration.calculate(calculationDate, database)

                // Then
                assertEquals(0, provisions.size, "provision size does not match")
            }
        }

        @Nested
        inner class BrokerActiveUpToSale {

            @BeforeEach
            fun setUp() {
                if (calculationDate.dayOfMonth == 1) {
                    calculationDate = calculationDate.plusWeeks(1)
                }
                a_broker(
                    codes = listOf(brokerCode),
                    statusHistory = mapOf(
                        lead.toLocalDate() to Broker.Status.ACTIVE,
                        sale.toLocalDate().plusDays(1) to Broker.Status.INACTIVE
                    ),
                    database = database
                )
            }

            @Test
            fun is_eligible_for_provision() {
                // Given
                val transaction = a_transaction(
                    lead = lead,
                    sale = sale,
                    brokerCode = brokerCode,
                    status = Transaction.Status.SALE,
                    database = database
                )

                // When
                val provisions = configuration.calculate(calculationDate, database)

                // Then
                assertEquals(1, provisions.size, "provision size does not match")
                val provision = provisions.first()
                assertEquals(amount, provision.sum, "sum does not match")
                assertEquals(1, provision.transactions.size, "transaction size does not match")
                provision.transactions.forEach { (t, v) ->
                    assertEquals(transaction, t, "transaction does not match")
                    assertEquals(amount, v.orElseThrow(), "transaction value does not match")
                }
            }
        }
    }
}
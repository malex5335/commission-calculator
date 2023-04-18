package de.riagade.commissioncalculator.configurations

import de.riagade.commissioncalculator.entities.*
import de.riagade.commissioncalculator.infra.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.*
import java.time.*

class VolumeTransactionCommissionTest {

    @Test
    fun name_is_set() {
        // Given
        val name = randomString()

        // When
        val configuration = VolumeTransactionCommission(
            name = name,
            percent = randomPercentage()
        )

        // Then
        assertEquals(name, configuration.name())
    }

    @ParameterizedTest
    @EnumSource(DayOfWeek::class, names = ["MONDAY"])
    fun do_calculate_on(dayOfWeek: DayOfWeek) {
        // Given
        val date = randomDate().with(dayOfWeek)
        val configuration = VolumeTransactionCommission(
            name = randomString(),
            percent = randomPercentage()
        )

        // When
        val canBeCalculatedAt = configuration.canBeCalculated(date)

        // Then
        assertTrue(canBeCalculatedAt)
    }

    @ParameterizedTest
    @EnumSource(DayOfWeek::class, names = ["TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"])
    fun dont_calculate_on(dayOfWeek: DayOfWeek) {
        // Given
        val date = randomDate().with(dayOfWeek)
        val configuration = VolumeTransactionCommission(
            name = randomString(),
            percent = randomPercentage()
        )

        // When
        val canBeCalculatedAt = configuration.canBeCalculated(date)

        // Then
        assertFalse(canBeCalculatedAt)
    }

    @Nested
    inner class CalculateCommissionConfiguration {
        private lateinit var database: MockDatabase
        private lateinit var calculationDate: LocalDate
        private lateinit var lead: LocalDateTime
        private lateinit var sale: LocalDateTime
        private lateinit var brokerCode: String
        private lateinit var broker: Broker
        private lateinit var configuration: VolumeTransactionCommission
        private lateinit var name: String
        private lateinit var percent: Percentage

        @BeforeEach
        fun setUp() {
            database = MockDatabase()
            calculationDate = randomDate().with(DayOfWeek.MONDAY)
            lead = calculationDate.atStartOfDay().minusMonths(1)
            sale = calculationDate.atStartOfDay()
            brokerCode = randomString()
            name = randomString()
            percent = randomPercentage()
            configuration = VolumeTransactionCommission(
                name = name,
                percent = percent
            )
        }

        @AfterEach
        fun tearDown() {
            database.clean()
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
                        volume = randomAmount(),
                        brokerCode = brokerCode,
                        status = Transaction.Status.SALE,
                        database = database
                    ))
                }

                // When
                val commissions = configuration.calculate(calculationDate, database)

                // Then
                assertEquals(1, commissions.size, "commission size does not match")
                val commission = commissions.first()
                val totalSum = transactions.sumOf { percent.calculate(it.volume) }
                assertEquals(totalSum, commission.sum, "sum does not match")
                assertEquals(transactions.size, commission.transactions.size, "transaction size does not match")
                commission.transactions.forEach { (t, v) ->
                    assertTrue(transactions.contains(t), "transaction does not match")
                    assertEquals(percent.calculate(t.volume), v.orElseThrow(), "transaction value does not match")
                }
            }

            @Test
            fun no_sale_happened() {
                // Given
                val transactions = mutableListOf<Transaction>()
                transactions.add(a_transaction(
                    lead = lead,
                    volume = randomAmount(),
                    brokerCode = brokerCode,
                    status = Transaction.Status.LEAD,
                    database = database
                ))

                // When
                val commissions = configuration.calculate(calculationDate, database)

                // Then
                assertEquals(0, commissions.size, "commission size does not match")
            }

            @Test
            fun wrong_broker_code() {
                // Given
                val transactions = mutableListOf<Transaction>()
                transactions.add(a_transaction(
                    lead = lead,
                    sale = sale,
                    volume = randomAmount(),
                    brokerCode = randomString(),
                    status = Transaction.Status.SALE,
                    database = database
                ))

                // When
                val commissions = configuration.calculate(calculationDate, database)

                // Then
                assertEquals(0, commissions.size, "commission size does not match")
            }

            @Nested
            inner class WasCalculatedBefore {

                @Test
                fun not_eligible_for_commission() {
                    // Given
                    val transaction = a_transaction(
                        lead = lead,
                        sale = sale,
                        volume = randomAmount(),
                        brokerCode = brokerCode,
                        status = Transaction.Status.SALE,
                        database = database
                    )
                    mark_calculated_before(
                        transaction = transaction,
                        commissionConfiguration = configuration,
                        database = database
                    )

                    // When
                    val commissions = configuration.calculate(calculationDate, database)

                    // Then
                    assertEquals(0, commissions.size, "commission size does not match")
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
            fun not_eligible_for_commission() {
                // Given
                a_transaction(
                    lead = lead,
                    sale = sale,
                    brokerCode = brokerCode,
                    status = Transaction.Status.SALE,
                    database = database
                )

                // When
                val commissions = configuration.calculate(calculationDate, database)

                // Then
                assertEquals(0, commissions.size, "commission size does not match")
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
            fun not_eligible_for_commission() {
                // Given
                a_transaction(
                    lead = lead,
                    sale = sale,
                    brokerCode = brokerCode,
                    volume = randomAmount(),
                    status = Transaction.Status.SALE,
                    database = database
                )

                // When
                val commissions = configuration.calculate(calculationDate, database)

                // Then
                assertEquals(0, commissions.size, "commission size does not match")
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
            fun not_eligible_for_commission() {
                // Given
                a_transaction(
                    lead = lead,
                    sale = sale,
                    volume = randomAmount(),
                    brokerCode = brokerCode,
                    status = Transaction.Status.SALE,
                    database = database
                )

                // When
                val commissions = configuration.calculate(calculationDate, database)

                // Then
                assertEquals(0, commissions.size, "commission size does not match")
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
            fun is_eligible_for_commission() {
                // Given
                val transaction = a_transaction(
                    lead = lead,
                    sale = sale,
                    volume = randomAmount(),
                    brokerCode = brokerCode,
                    status = Transaction.Status.SALE,
                    database = database
                )

                // When
                val commissions = configuration.calculate(calculationDate, database)

                // Then
                assertEquals(1, commissions.size, "commission size does not match")
                val commission = commissions.first()
                val amount = percent.calculate(transaction.volume)
                assertEquals(amount, commission.sum, "sum does not match")
                assertEquals(1, commission.transactions.size, "transaction size does not match")
                commission.transactions.forEach { (t, v) ->
                    assertEquals(transaction, t, "transaction does not match")
                    assertEquals(amount, v.orElseThrow(), "transaction value does not match")
                }
            }
        }
    }
}
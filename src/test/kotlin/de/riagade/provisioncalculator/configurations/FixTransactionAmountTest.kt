package de.riagade.provisioncalculator.configurations

import de.riagade.provisioncalculator.entities.Broker
import de.riagade.provisioncalculator.entities.Transaction
import de.riagade.provisioncalculator.infra.*
import org.junit.jupiter.api.*

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.math.BigDecimal
import java.time.DayOfWeek

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
            amount = amount,
            database = database
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

    @Test
    fun calculate_single_saleToday_leadMonthAgo() {
        // Given
        val calculationDate = randomDate().with(DayOfWeek.MONDAY)
        val lead = calculationDate.atStartOfDay().minusMonths(1)
        val sale = calculationDate.atStartOfDay()
        database.configurations.add(configuration)
        val brokerCode = randomString()
        Setup.a_broker(
            codes = listOf(brokerCode),
            statusHistory = mapOf(lead.toLocalDate() to Broker.Status.ACTIVE),
            database = database
        )
        val transaction = Setup.a_transaction(
            sale = sale,
            lead = lead,
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

    @Test
    fun calculate_multiple_saleToday_leadMonthAgo() {
        // Given
        val calculationDate = randomDate().with(DayOfWeek.MONDAY)
        val lead = calculationDate.atStartOfDay().minusMonths(1)
        val sale = calculationDate.atStartOfDay()
        database.configurations.add(configuration)
        val brokerCode = randomString()
        Setup.a_broker(
            codes = listOf(brokerCode),
            statusHistory = mapOf(lead.toLocalDate() to Broker.Status.ACTIVE),
            database = database
        )
        val transactions = mutableListOf<Transaction>()
        transactions.add(Setup.a_transaction(
            sale = sale,
            lead = lead,
            brokerCode = brokerCode,
            status = Transaction.Status.SALE,
            database = database
        ))
        transactions.add(Setup.a_transaction(
            sale = sale,
            lead = lead,
            brokerCode = brokerCode,
            status = Transaction.Status.SALE,
            database = database
        ))

        // When
        val provisions = configuration.calculate(calculationDate, database)

        // Then
        assertEquals(1, provisions.size, "provision size does not match")
        val provision = provisions.first()
        assertEquals(amount.multiply(BigDecimal.valueOf(transactions.size.toLong())), provision.sum, "sum does not match")
        assertEquals(transactions.size, provision.transactions.size, "transaction size does not match")
        provision.transactions.forEach { (t, v) ->
            assertTrue(transactions.contains(t), "transaction does not match")
            assertEquals(amount, v.orElseThrow(), "transaction value does not match")
        }
    }
}
package de.riagade.commissioncalculator.configurations

import de.riagade.commissioncalculator.entities.*
import de.riagade.commissioncalculator.infra.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class FixConversionCommissionTest {
    private lateinit var database: MockDatabase
    private lateinit var brokerCode: String
    private lateinit var amount: BigDecimal
    private lateinit var calculationDate: LocalDate

    @BeforeEach
    fun setup() {
        brokerCode = randomString()
        database = MockDatabase()
        amount = randomAmount()
        calculationDate = randomDate()
        a_broker(
            database = database,
            codes = listOf(brokerCode),
            statusHistory = mapOf(calculationDate to Broker.Status.ACTIVE)
        )
    }

    @Test
    fun test() {
        // Given
        val transactions = multiple_transactions(
            leadCount = 1,
            saleCount = 2,
            date = calculationDate,
            brokerCode = brokerCode,
            database = database
        )
        val configuration = FixConversionCommission(
            name = randomString(),
            crValues = mapOf(ConversionRate.of(0.5) to amount)
        )
        database.commissionConfigurations.add(configuration)

        // When
        val commissions = configuration.calculate(calculationDate, database)

        // Then
        assertEquals(1, commissions.size, "provision size does not match")
        val commission = commissions.first()
        assertEquals(amount, commission.sum, "sum does not match")
        assertEquals(transactions.size, commission.transactions.size, "transaction size does not match")
        commission.transactions.forEach { (t, v) ->
            assertTrue(transactions.contains(t), "transaction does not match")
            assertTrue(v.isEmpty, "transaction value does not match")
        }
    }
}
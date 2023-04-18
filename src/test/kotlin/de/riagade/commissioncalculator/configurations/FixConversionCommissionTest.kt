package de.riagade.commissioncalculator.configurations

import de.riagade.commissioncalculator.entities.*
import de.riagade.commissioncalculator.infra.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters.firstDayOfMonth

class FixConversionCommissionTest {
    @Test
    fun name_is_set() {
        // Given
        val name = randomString()

        // When
        val configuration = FixConversionCommission(
            name = name,
            crValues = emptyMap()
        )

        // Then
        assertEquals(name, configuration.name())
    }

    @Test
    fun do_calculate_on_first_of_month() {
        // Given
        val date = randomDate().with(firstDayOfMonth())
        val configuration = FixConversionCommission(
            name = randomString(),
            crValues = emptyMap()
        )

        // When
        val canBeCalculatedAt = configuration.canBeCalculated(date)

        // Then
        assertTrue(canBeCalculatedAt)
    }

    @Test
    fun dont_calculate_on_any_but_first_day_of_month() {
        // Given
        var date = randomDate()
        if(date.dayOfMonth == 1) {
            date = date.plusDays(1)
        }
        val configuration = FixConversionCommission(
            name = randomString(),
            crValues = emptyMap()
        )

        // When
        val canBeCalculatedAt = configuration.canBeCalculated(date)

        // Then
        assertFalse(canBeCalculatedAt)
    }

    @Nested
    inner class Calculate {
        private lateinit var database: MockDatabase
        private lateinit var brokerCode: String
        private lateinit var amount: BigDecimal
        private lateinit var calculationDate: LocalDate

        @BeforeEach
        fun setup() {
            brokerCode = randomString()
            database = MockDatabase()
            amount = randomAmount()
            calculationDate = randomDate().with(firstDayOfMonth())
            a_broker(
                database = database,
                codes = listOf(brokerCode),
                statusHistory = mapOf(calculationDate.minusYears(1) to Broker.Status.ACTIVE)
            )
        }

        @AfterEach
        fun tearDown() {
            database.clean()
        }

        @Test
        fun cr_successfully_achieved() {
            // Given
            val transactions = multiple_transactions(
                leadCount = 1,
                saleCount = 2,
                date = calculationDate.minusMonths(1),
                brokerCode = brokerCode,
                database = database
            )
            val configuration = FixConversionCommission(
                name = randomString(),
                crValues = mapOf(
                    ConversionRate.of(0.1) to amount.divide(BigDecimal.valueOf(2)),
                    ConversionRate.of(0.5) to amount,
                    ConversionRate.of(1.0) to amount.multiply(BigDecimal.valueOf(2))
                )
            )

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

        @Test
        fun cr_successfully_achieved_older_wont_count() {
            // Given
            multiple_transactions(
                leadCount = random().nextInt(100),
                saleCount = random().nextInt(100),
                date = calculationDate.minusMonths(2),
                brokerCode = brokerCode,
                database = database
            )
            val transactions = multiple_transactions(
                leadCount = 1,
                saleCount = 2,
                date = calculationDate.minusMonths(1),
                brokerCode = brokerCode,
                database = database
            )
            val configuration = FixConversionCommission(
                name = randomString(),
                crValues = mapOf(
                    ConversionRate.of(0.1) to amount.divide(BigDecimal.valueOf(2)),
                    ConversionRate.of(0.5) to amount,
                    ConversionRate.of(1.0) to amount.multiply(BigDecimal.valueOf(2))
                )
            )

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

        @Test
        fun cr_successfully_achieved_newer_wont_count() {
            // Given
            multiple_transactions(
                leadCount = random().nextInt(100),
                saleCount = random().nextInt(100),
                date = calculationDate,
                brokerCode = brokerCode,
                database = database
            )
            val transactions = multiple_transactions(
                leadCount = 1,
                saleCount = 2,
                date = calculationDate.minusMonths(1),
                brokerCode = brokerCode,
                database = database
            )
            val configuration = FixConversionCommission(
                name = randomString(),
                crValues = mapOf(
                    ConversionRate.of(0.1) to amount.divide(BigDecimal.valueOf(2)),
                    ConversionRate.of(0.5) to amount,
                    ConversionRate.of(1.0) to amount.multiply(BigDecimal.valueOf(2))
                )
            )

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

}

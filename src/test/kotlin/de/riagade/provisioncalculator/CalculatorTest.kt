package de.riagade.provisioncalculator

import de.riagade.provisioncalculator.entities.Broker
import de.riagade.provisioncalculator.entities.Transaction
import de.riagade.provisioncalculator.infra.SetupHelper
import de.riagade.provisioncalculator.infra.randomString
import de.riagade.provisioncalculator.mock.MockDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate

class CalculatorTest {
    private lateinit var database: MockDatabase
    private lateinit var calculator: Calculator
    private lateinit var setup: SetupHelper

    @BeforeEach
    fun setUp() {
        database = MockDatabase()
        calculator = Calculator(database)
        setup = SetupHelper(database)
    }

    @AfterEach
    fun tearDown() {
        database.clean()
    }

    @Nested
    inner class WithActiveBroker {
        private lateinit var broker: Broker
        private lateinit var brokerActiveDate: LocalDate

        @BeforeEach
        fun setUp() {
            brokerActiveDate = LocalDate.of(2018, 1, 1)
            broker = setup.broker(
                name = randomString(),
                brokerCodes = listOf(randomString()),
                statusHistory = mapOf(
                    brokerActiveDate to Broker.Status.ACTIVE
                )
            )
        }

        @Nested
        inner class WithTransaction {
            private lateinit var transaction: Transaction

            @BeforeEach
            fun setUp() {
                val transactionId = randomString()
                val leadDate = brokerActiveDate.plusDays(1)
                val saleDate = brokerActiveDate.plusDays(1)
                transaction = setup.transaction(
                    id = transactionId,
                    brokerCode = broker.codes.first(),
                    lead = leadDate,
                    sale = saleDate,
                    product = setup.product(
                        name = randomString(),
                        groupName = randomString()
                    )
                )
            }

            @Test
            fun fixProvision_is_calculated_on_mondays() {
                // Given
                val dateOfCalculation = brokerActiveDate.plusMonths(1).with(DayOfWeek.MONDAY)
                val provisionName = randomString()
                val provisionAmount = BigDecimal.valueOf(15)
                setup.fixProvision(
                    name = provisionName,
                    amount = provisionAmount
                )

                // When
                calculator.calculateConfigurations(dateOfCalculation)

                // Then
                assertEquals(1, database.savedProvisions.size)
                assertEquals(provisionName, database.savedProvisions.first().configurationName)
                assertEquals(provisionAmount, database.savedProvisions.first().sum)
                assertEquals(transaction.id, database.savedProvisions.first().transactions.keys.first().id)
            }

            @ParameterizedTest
            @EnumSource(DayOfWeek::class, names = ["TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"])
            fun fixProvision_is_not_calculated_on(dayOfWeek: DayOfWeek) {
                // Given
                val dateOfCalculation = brokerActiveDate.plusMonths(1).with(dayOfWeek)
                val provisionName = randomString()
                val provisionAmount = BigDecimal.valueOf(15)
                setup.fixProvision(
                    name = provisionName,
                    amount = provisionAmount
                )

                // When
                calculator.calculateConfigurations(dateOfCalculation)

                // Then
                assertEquals(0, database.savedProvisions.size)
            }

        }
    }
    @Nested
    inner class WithNotActiveBroker {
        private lateinit var broker: Broker
        private lateinit var brokerInActiveDate: LocalDate

        @BeforeEach
        fun setUp() {
            brokerInActiveDate = LocalDate.of(2018, 1, 1)
            broker = setup.broker(
                name = randomString(),
                brokerCodes = listOf(randomString()),
                statusHistory = mapOf(
                    brokerInActiveDate to Broker.Status.INACTIVE
                )
            )
        }

        @Nested
        inner class WithTransaction {
            private lateinit var transaction: Transaction

            @BeforeEach
            fun setUp() {
                val transactionId = randomString()
                val leadDate = brokerInActiveDate.plusDays(1)
                val saleDate = brokerInActiveDate.plusDays(1)
                transaction = setup.transaction(
                    id = transactionId,
                    brokerCode = broker.codes.first(),
                    lead = leadDate,
                    sale = saleDate,
                    product = setup.product(
                        name = randomString(),
                        groupName = randomString()
                    )
                )
            }

            @Test
            fun no_calculation() {
                // Given
                val dateOfCalculation = brokerInActiveDate.plusMonths(1).with(DayOfWeek.MONDAY)
                val provisionName = randomString()
                val provisionAmount = BigDecimal.valueOf(15)
                setup.fixProvision(
                    name = provisionName,
                    amount = provisionAmount
                )

                // When
                calculator.calculateConfigurations(dateOfCalculation)

                // Then
                assertEquals(0, database.savedProvisions.size)
            }
        }
    }

}
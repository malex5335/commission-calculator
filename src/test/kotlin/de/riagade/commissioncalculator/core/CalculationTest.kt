package de.riagade.commissioncalculator.core

import de.riagade.commissioncalculator.core.infra.MockDatabase
import de.riagade.commissioncalculator.core.infra.a_commission
import de.riagade.commissioncalculator.core.infra.a_configuration
import de.riagade.commissioncalculator.core.infra.randomDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CalculationTest {
    private lateinit var database: MockDatabase
    private lateinit var calculation: Calculation

    @BeforeEach
    fun setUp() {
        database = MockDatabase()
        calculation = Calculation(database)
    }

    @AfterEach
    fun tearDown() {
        database.clean()
    }

    @Test
    fun calculate_if_date_relevant() {
        // Given
        val commission = a_commission()
        a_configuration(
            canBeCalculatedAt = { true },
            calculate = { _, _ -> listOf(commission) },
            database = database
        )

        // When
        calculation.calculateConfigurations(randomDate())

        // Then
        assertEquals(1, database.savedCommissions.size)
        assertEquals(commission, database.savedCommissions[0])
    }

    @Test
    fun cant_calculate_date_is_not_yet() {
        // Given
        a_configuration(
            canBeCalculatedAt = { false },
            calculate = { _, _ -> listOf(a_commission()) },
            database = database
        )

        // When
        calculation.calculateConfigurations(randomDate())

        // Then
        assertEquals(0, database.savedCommissions.size)
    }

    @Test
    fun mixed_configurations_will_be_calculated() {
        // Given
        val willBeCalculated = mutableListOf(a_commission(), a_commission())
        val wontBeCalculated = mutableListOf(a_commission())
        a_configuration(
            canBeCalculatedAt = { true },
            calculate = { _, _ -> listOf(willBeCalculated[0]) },
            database = database
        )
        a_configuration(
            canBeCalculatedAt = { false },
            calculate = { _, _ -> listOf(wontBeCalculated[0]) },
            database = database
        )
        a_configuration(
            canBeCalculatedAt = { true },
            calculate = { _, _ -> listOf(willBeCalculated[1]) },
            database = database
        )

        // When
        calculation.calculateConfigurations(randomDate())

        // Then
        assertEquals(2, database.savedCommissions.size)
        assertTrue(database.savedCommissions.containsAll(willBeCalculated))
        assertFalse(database.savedCommissions.containsAll(wontBeCalculated))
    }
}
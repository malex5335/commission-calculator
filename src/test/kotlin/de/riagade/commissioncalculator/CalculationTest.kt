package de.riagade.commissioncalculator

import de.riagade.commissioncalculator.infra.*
import org.junit.jupiter.api.*

import org.junit.jupiter.api.Assertions.*

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
        val provision = a_provision()
        a_configuration(
            canBeCalculatedAt = { true },
            calculate = { _, _ -> listOf(provision) },
            database = database
        )

        // When
        calculation.calculateConfigurations(randomDate())

        // Then
        assertEquals(1, database.savedCommissions.size)
        assertEquals(provision, database.savedCommissions[0])
    }

    @Test
    fun cant_calculate_date_is_not_yet() {
        // Given
        a_configuration(
            canBeCalculatedAt = { false },
            calculate = { _, _ -> listOf(a_provision()) },
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
        val willBeCalculated = mutableListOf(a_provision(), a_provision())
        val wontBeCalculated = mutableListOf(a_provision())
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
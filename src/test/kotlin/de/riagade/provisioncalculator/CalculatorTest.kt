package de.riagade.provisioncalculator

import de.riagade.provisioncalculator.infra.*
import org.junit.jupiter.api.*

import org.junit.jupiter.api.Assertions.*

class CalculatorTest {
    private lateinit var database: MockDatabase
    private lateinit var calculator: Calculator

    @BeforeEach
    fun setUp() {
        database = MockDatabase()
        calculator = Calculator(database)
    }

    @AfterEach
    fun tearDown() {
        database.clean()
    }

    @Test
    fun calculate_if_date_relevant() {
        // Given
        val provision = Setup.a_provision()
        Setup.a_configuration(
            canBeCalculatedAt = { true },
            calculate = { _, _ -> listOf(provision) },
            database = database
        )

        // When
        calculator.calculateConfigurations(randomDate())

        // Then
        assertEquals(1, database.savedProvisions.size)
        assertEquals(provision, database.savedProvisions[0])
    }

    @Test
    fun cant_calculate_date_is_not_yet() {
        // Given
        val provision = Setup.a_provision()
        Setup.a_configuration(
            canBeCalculatedAt = { false },
            calculate = { _, _ -> listOf(provision) },
            database = database
        )

        // When
        calculator.calculateConfigurations(randomDate())

        // Then
        assertEquals(0, database.savedProvisions.size)
    }

    @Test
    fun multiple_configurations_will_be_calculated() {
        // Given
        val provision1 = Setup.a_provision()
        val provision2 = Setup.a_provision()
        val provision3 = Setup.a_provision()
        Setup.a_configuration(
            canBeCalculatedAt = { true },
            calculate = { _, _ -> listOf(provision1) },
            database = database
        )
        Setup.a_configuration(
            canBeCalculatedAt = { true },
            calculate = { _, _ -> listOf(provision2) },
            database = database
        )
        Setup.a_configuration(
            canBeCalculatedAt = { true },
            calculate = { _, _ -> listOf(provision3) },
            database = database
        )

        // When
        calculator.calculateConfigurations(randomDate())

        // Then
        assertEquals(3, database.savedProvisions.size)
        assertTrue(database.savedProvisions.containsAll(listOf(provision1, provision2, provision3)))
    }

    @Test
    fun mixed_configurations_will_be_calculated() {
        // Given
        val provision1 = Setup.a_provision()
        val provision2 = Setup.a_provision()
        val provision3 = Setup.a_provision()
        Setup.a_configuration(
            canBeCalculatedAt = { true },
            calculate = { _, _ -> listOf(provision1) },
            database = database
        )
        Setup.a_configuration(
            canBeCalculatedAt = { false },
            calculate = { _, _ -> listOf(provision2) },
            database = database
        )
        Setup.a_configuration(
            canBeCalculatedAt = { true },
            calculate = { _, _ -> listOf(provision3) },
            database = database
        )

        // When
        calculator.calculateConfigurations(randomDate())

        // Then
        assertEquals(2, database.savedProvisions.size)
        assertTrue(database.savedProvisions.containsAll(listOf(provision1, provision3)))
        assertFalse(database.savedProvisions.contains(provision2))
    }
}
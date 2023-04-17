package de.riagade.provisioncalculator

import java.time.LocalDate

class Calculation(
    private val database: Database
) {
    fun calculateConfigurations(now: LocalDate) {
        database.allConfigurations()
            .filter { it.canBeCalculated(now) }
            .map { it.calculate(now, database) }
            .forEach { database.saveProvisions(it) }
    }
}

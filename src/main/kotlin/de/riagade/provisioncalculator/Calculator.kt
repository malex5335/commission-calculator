package de.riagade.provisioncalculator

import java.time.LocalDate

class Calculator(
    private val database: Database
) {
    fun calculateConfigurations(now: LocalDate) {
        database.allConfigurations()
            .filter { it.canBeCalculatedAt(now) }
            .map { it.calculate(now, database) }
            .forEach { database.saveProvisions(it) }
    }
}

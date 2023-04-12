package de.riagade.provisioncalculator

import java.time.LocalDate

class Calculator(
    private val database: Database
) {
    fun calculateConfigurations(now: LocalDate) {
        database.allConfigurations()
            .filter { it.canBeCalculatedAt(now) }
            .forEach { configuration ->
                val timespan = configuration.relevantTimespanAround(now)
                val transactions = database.allTransactionsInTimespan(timespan)
                val provisions = configuration.calculate(transactions)
                database.saveProvisions(provisions)
            }
    }
}

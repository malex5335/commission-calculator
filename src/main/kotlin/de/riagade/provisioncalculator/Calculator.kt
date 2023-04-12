package de.riagade.provisioncalculator

class Calculator(
    private val database: Database
) {
    fun calculateConfigurations() {
        val configurations = database.allConfigurations()
        val relevantConfigurations = configurations
            .filter(Configuration::shouldBeCalculated)
        relevantConfigurations.forEach { configuration ->
            val timespan = configuration.relevantTimespan()
            val transactions = database.allTransactionsInTimespan(timespan)
            val provisions = configuration.calculate(transactions)
            database.saveProvisions(provisions)
        }
    }
}

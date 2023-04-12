package de.riagade.provisioncalculator

import de.riagade.provisioncalculator.Configuration.*
import de.riagade.provisioncalculator.Configuration.ConfigurationTimespanBasis.*
import java.time.LocalDate

class CalculatorRunner {
    fun calculateKonfigurations() {
        val konfigurations = allImplementedConfigurations()
        val relevantConfigurations = konfigurations
            .filter(Configuration::shouldBeCalculated)
        statusUpdate("%i relevante Konfigurationen für den heutigen Tag gefunden", relevantConfigurations.size)
        relevantConfigurations.forEach {
            configuration ->
            val name = configuration.name()
            statusUpdate("Beginne mit Konfiguration %s", name)
            val timespan = configuration.relevantTimespan()
            statusUpdate("Geschäfte im Zeitraum von %s bis %s auf Basis des %s sind für Konfiguration %s relevant", timespan.start, timespan.end, timespan.basis, name)
            val transactions = allTransactionsIn(timespan)
            statusUpdate("Es werden %i Geschäfte für Konfiguration %s werden berechnet", transactions.size, name)
            val provisions = configuration.calculate(transactions)
            val totalAmount = provisions.sumOf { it.sum }
            statusUpdate("Es wurden %i Provisionen in Höhe von insgesamt %i€ für Konfiguration %s berechnet", provisions.size, totalAmount, name)
            saveProvisions(provisions)
            statusUpdate("Provisionen für Konfiguration %s gespeichert",name)
        }
        if (relevantConfigurations.isNotEmpty()) {
            statusUpdate("alle Konfigurationen wurden berechnet")
        }
    }

    private fun saveProvisions(provisions: List<Provision>) {

    }

    private fun allTransactionsIn(timespan: ConfigurationTimespan): List<Transaction> {
        val allTransactions = listOf<Transaction>()
        return allTransactions
            .filter { fromBasis(it, timespan.basis) in timespan.start..timespan.end }
    }

    private fun fromBasis(transaction: Transaction, basis: ConfigurationTimespanBasis): LocalDate {
         return when (basis) {
            CREATED -> transaction.created
            UPDATED -> transaction.updated
            LEAD -> transaction.lead
            SALE -> transaction.sale
        }.toLocalDate()
    }

    private fun allImplementedConfigurations(): List<Configuration> {
        return listOf()
    }

    private fun statusUpdate(message: String, vararg args: Any) {
        println(message.format(args))
    }
}

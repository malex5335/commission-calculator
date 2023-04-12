package de.riagade.provisioncalculator

import de.riagade.provisioncalculator.entities.*

interface Database {
    fun allConfigurations(): List<Configuration>
    fun allTransactionsInTimespan(timespan: Configuration.Timespan): List<Transaction>
    fun saveProvisions(provisions: List<Provision>)
    fun brokerFromCode(brokerCode: String): Broker
    fun wasCalculatedBefore(transaction: Transaction, configuration: Configuration): Boolean
}

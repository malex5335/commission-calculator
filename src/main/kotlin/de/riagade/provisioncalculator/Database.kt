package de.riagade.provisioncalculator

import de.riagade.provisioncalculator.entities.*

interface Database {
    fun allConfigurations(): List<ProvisionConfiguration>
    fun allTransactionsInTimespan(timespan: ProvisionConfiguration.Timespan): List<Transaction>
    fun saveProvisions(provisions: List<Provision>)
    fun brokerFromCode(brokerCode: String): Broker?
    fun wasCalculatedBefore(transaction: Transaction, provisionConfiguration: ProvisionConfiguration): Boolean
}

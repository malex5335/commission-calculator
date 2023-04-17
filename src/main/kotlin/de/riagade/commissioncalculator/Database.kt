package de.riagade.commissioncalculator

import de.riagade.commissioncalculator.entities.*

interface Database {
    fun allConfigurations(): List<CommissionConfiguration>
    fun allTransactionsInTimespan(timespan: CommissionConfiguration.Timespan): List<Transaction>
    fun saveCommissions(commissions: List<Commission>)
    fun brokerFromCode(brokerCode: String): Broker?
    fun wasCalculatedBefore(transaction: Transaction, commissionConfiguration: CommissionConfiguration): Boolean
}
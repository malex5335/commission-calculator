package de.riagade.commissioncalculator.core

import de.riagade.commissioncalculator.core.entities.Broker
import de.riagade.commissioncalculator.core.entities.Commission
import de.riagade.commissioncalculator.core.entities.Timespan
import de.riagade.commissioncalculator.core.entities.Transaction

interface Database {
    fun allConfigurations(): List<CommissionConfiguration>
    fun allTransactionsInTimespan(timespan: Timespan): List<Transaction>
    fun saveCommissions(commissions: List<Commission>)
    fun brokerFromCode(brokerCode: String): Broker?
    fun wasCalculatedBefore(transaction: Transaction, commissionConfiguration: CommissionConfiguration): Boolean
    fun readCommissions(): List<Commission>
}

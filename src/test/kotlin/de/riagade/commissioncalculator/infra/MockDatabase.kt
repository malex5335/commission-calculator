package de.riagade.commissioncalculator.infra

import de.riagade.commissioncalculator.CommissionConfiguration
import de.riagade.commissioncalculator.Database
import de.riagade.commissioncalculator.entities.*

class MockDatabase(
    val transactions: MutableList<Transaction> = mutableListOf(),
    val commissionConfigurations: MutableList<CommissionConfiguration> = mutableListOf(),
    val commissions: MutableList<Commission> = mutableListOf(),
    val brokers: MutableList<Broker> = mutableListOf(),
    val bankDetails: MutableList<Broker.BankDetails> = mutableListOf(),
    val products: MutableList<Product> = mutableListOf(),
    val groups: MutableList<Product.Group> = mutableListOf(),
    val savedCommissions: MutableList<Commission> = mutableListOf(),
    val wasCalculatedBefore: MutableMap<Transaction, CommissionConfiguration> = mutableMapOf()
): Database {
    override fun allConfigurations(): List<CommissionConfiguration> {
        return commissionConfigurations
    }

    override fun allTransactionsInTimespan(timespan: CommissionConfiguration.Timespan): List<Transaction> {
        return transactions.filter {
            val baseDate = when(timespan.basis) {
                CommissionConfiguration.TimespanBasis.CREATED -> it.created
                CommissionConfiguration.TimespanBasis.SALE -> it.sale
                CommissionConfiguration.TimespanBasis.LEAD -> it.lead
                CommissionConfiguration.TimespanBasis.UPDATED -> it.updated
            }?.toLocalDate()
            if(baseDate == null) {
                false
            } else {
                !baseDate.isBefore(timespan.from) && !baseDate.isAfter(timespan.to)
            }
        }
    }

    override fun saveCommissions(commissions: List<Commission>) {
        savedCommissions.addAll(commissions)
    }

    override fun brokerFromCode(brokerCode: String): Broker? {
        return brokers.firstOrNull { it.codes.contains(brokerCode) }
    }

    override fun wasCalculatedBefore(transaction: Transaction, commissionConfiguration: CommissionConfiguration): Boolean {
        return wasCalculatedBefore[transaction] == commissionConfiguration
    }

    fun clean() {
        transactions.clear()
        commissionConfigurations.clear()
        commissions.clear()
        brokers.clear()
        bankDetails.clear()
        products.clear()
        groups.clear()
        savedCommissions.clear()
        wasCalculatedBefore.clear()
    }
}
package de.riagade.provisioncalculator.infra

import de.riagade.provisioncalculator.ProvisionConfiguration
import de.riagade.provisioncalculator.Database
import de.riagade.provisioncalculator.entities.*

class MockDatabase(
    val transactions: MutableList<Transaction> = mutableListOf(),
    val provisionConfigurations: MutableList<ProvisionConfiguration> = mutableListOf(),
    val provisions: MutableList<Provision> = mutableListOf(),
    val brokers: MutableList<Broker> = mutableListOf(),
    val bankDetails: MutableList<Broker.BankDetails> = mutableListOf(),
    val products: MutableList<Product> = mutableListOf(),
    val groups: MutableList<Product.Group> = mutableListOf(),
    val savedProvisions: MutableList<Provision> = mutableListOf(),
    val wasCalculatedBefore: MutableMap<Transaction, ProvisionConfiguration> = mutableMapOf()
): Database {
    override fun allConfigurations(): List<ProvisionConfiguration> {
        return provisionConfigurations
    }

    override fun allTransactionsInTimespan(timespan: ProvisionConfiguration.Timespan): List<Transaction> {
        return transactions.filter {
            val baseDate = when(timespan.basis) {
                ProvisionConfiguration.TimespanBasis.CREATED -> it.created
                ProvisionConfiguration.TimespanBasis.SALE -> it.sale
                ProvisionConfiguration.TimespanBasis.LEAD -> it.lead
                ProvisionConfiguration.TimespanBasis.UPDATED -> it.updated
            }.toLocalDate()
            !baseDate.isBefore(timespan.from) && !baseDate.isAfter(timespan.to)
        }
    }

    override fun saveProvisions(provisions: List<Provision>) {
        savedProvisions.addAll(provisions)
    }

    override fun brokerFromCode(brokerCode: String): Broker? {
        return brokers.firstOrNull { it.codes.contains(brokerCode) }
    }

    override fun wasCalculatedBefore(transaction: Transaction, provisionConfiguration: ProvisionConfiguration): Boolean {
        return wasCalculatedBefore[transaction] == provisionConfiguration
    }

    fun clean() {
        transactions.clear()
        provisionConfigurations.clear()
        provisions.clear()
        brokers.clear()
        bankDetails.clear()
        products.clear()
        groups.clear()
        savedProvisions.clear()
        wasCalculatedBefore.clear()
    }
}
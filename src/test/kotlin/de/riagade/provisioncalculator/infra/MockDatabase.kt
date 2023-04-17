package de.riagade.provisioncalculator.infra

import de.riagade.provisioncalculator.Configuration
import de.riagade.provisioncalculator.Database
import de.riagade.provisioncalculator.entities.*

class MockDatabase(
    val transactions: MutableList<Transaction> = mutableListOf(),
    val configurations: MutableList<Configuration> = mutableListOf(),
    val provisions: MutableList<Provision> = mutableListOf(),
    val brokers: MutableList<Broker> = mutableListOf(),
    val bankDetails: MutableList<Broker.BankDetails> = mutableListOf(),
    val products: MutableList<Product> = mutableListOf(),
    val groups: MutableList<Product.Group> = mutableListOf(),
    val savedProvisions: MutableList<Provision> = mutableListOf()
): Database {
    override fun allConfigurations(): List<Configuration> {
        return configurations
    }

    override fun allTransactionsInTimespan(timespan: Configuration.Timespan): List<Transaction> {
        return transactions.filter {
            val baseDate = when(timespan.basis) {
                Configuration.TimespanBasis.CREATED -> it.created
                Configuration.TimespanBasis.SALE -> it.sale
                Configuration.TimespanBasis.LEAD -> it.lead
                Configuration.TimespanBasis.UPDATED -> it.updated
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

    override fun wasCalculatedBefore(transaction: Transaction, configuration: Configuration): Boolean {
        return provisions.any { provision ->
            provision.transactions.keys.map { it.id }.contains(transaction.id)
                    && provision.configurationName == configuration.name()
        }
    }

    fun clean() {
        transactions.clear()
        configurations.clear()
        provisions.clear()
        brokers.clear()
        bankDetails.clear()
        products.clear()
        groups.clear()
        savedProvisions.clear()
    }
}
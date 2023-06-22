package de.riagade.commissioncalculator.spring

import de.riagade.commissioncalculator.core.CommissionConfiguration
import de.riagade.commissioncalculator.core.Database
import de.riagade.commissioncalculator.core.entities.Broker
import de.riagade.commissioncalculator.core.entities.Commission
import de.riagade.commissioncalculator.core.entities.Timespan
import de.riagade.commissioncalculator.core.entities.Transaction
import java.time.LocalDate

class MainDb: Database {
    override fun allConfigurations(): List<CommissionConfiguration> {
        TODO("Not yet implemented")
    }

    override fun allTransactionsInTimespan(timespan: Timespan): List<Transaction> {
        TODO("Not yet implemented")
    }

    override fun saveCommissions(commissions: List<Commission>) {
        TODO("Not yet implemented")
    }

    override fun brokerFromCode(brokerCode: String): Broker? {
        TODO("Not yet implemented")
    }

    override fun wasCalculatedBefore(
        transaction: Transaction,
        commissionConfiguration: CommissionConfiguration
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun readCommissions(): List<Commission> {
        TODO("Not yet implemented")
    }

    fun commissionsFromTriggerDate(date: LocalDate): List<Commission> {
        return readCommissions().filter { it.triggerDate == date }
    }

    fun commissionsForBroker(brokerId: String): List<Commission> {
        brokerFromCode(brokerId)?.let { broker ->
            return readCommissions().filter { it.broker == broker }
        }
        return emptyList()
    }

    fun commissionsFromScopeDate(date: LocalDate): List<Commission> {
        return readCommissions().filter { it.scopeDate == date }
    }

    fun transactionsForBroker(brokerId: String, timespan: Timespan): List<Transaction> {
        brokerFromCode(brokerId)?.let { broker ->
            return allTransactionsInTimespan(timespan).filter { brokerFromCode(it.brokerCode)!! == broker }
        }
        return emptyList()
    }

    fun transactionsForBrokerCode(brokerId: String, timespan: Timespan): List<Transaction> {
        return allTransactionsInTimespan(timespan).filter { it.brokerCode == brokerId }
    }
}
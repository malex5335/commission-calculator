package de.riagade.provisioncalculator.configurations

import de.riagade.provisioncalculator.*
import de.riagade.provisioncalculator.entities.*
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters.firstDayOfMonth
import java.time.temporal.TemporalAdjusters.lastDayOfMonth
import java.util.*

class FixTransactionAmount(
    private val name: String,
    private val amount: BigDecimal
): Configuration {

    override fun name(): String {
        return name
    }

    override fun canBeCalculatedAt(date: LocalDate): Boolean {
        return date.dayOfWeek.equals(DayOfWeek.MONDAY)
    }

    override fun calculate(date: LocalDate, database: Database): List<Provision> {
        val provisions = mutableListOf<Provision>()
        val relevantTimespan = Configuration.Timespan(
            from = date.with(firstDayOfMonth()),
            to = date.with(lastDayOfMonth()),
            basis = Configuration.TimespanBasis.SALE
        )
        val transactionsInTimespan = database.allTransactionsInTimespan(relevantTimespan)
        relevantTransactionsByBroker(transactionsInTimespan, database)
            .forEach { (broker, transactions) ->
                val transactionAmounts = transactions.map { it to Optional.of(amount) }
                if(transactionAmounts.isNotEmpty()) {
                    provisions.add(
                        Provision(
                            broker = broker,
                            sum = transactionAmounts.sumOf { it.second.orElse(BigDecimal.ZERO) },
                            transactions = transactionAmounts.toMap(),
                            configurationName = name(),
                            status = Provision.Status.CALCULATED
                        )
                    )
                }
            }
        return provisions
    }

    private fun relevantTransactionsByBroker(transactionsToFilter: List<Transaction>, database: Database): Map<Broker, List<Transaction>> {
        return transactionsToFilter
            .asSequence()
            .filter { !database.wasCalculatedBefore(it, this) }
            .filter { it.status == Transaction.Status.SALE }
            .groupBy { database.brokerFromCode(it.brokerCode) }
            .filter { it.key != null }
            .map { (broker, transactions) ->
                if (broker != null) {
                    broker to transactions
                        .filter { broker.wasActiveAt(it.lead.toLocalDate()) }
                        .filter { broker.wasActiveAt(it.sale.toLocalDate()) }
                } else {
                    throw IllegalStateException("Broker is not allowed to be null")
                }
            }.toMap()
    }
}
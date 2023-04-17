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
    private val database: Database,
    private val amount: BigDecimal,
    private val name: String
): Configuration {

    override fun name(): String {
        return name
    }

    override fun canBeCalculatedAt(date: LocalDate): Boolean {
        return date.dayOfWeek.equals(DayOfWeek.MONDAY)
    }

    override fun calculate(date: LocalDate, database: Database): List<Provision> {
        val provisions = mutableListOf<Provision>()
        val transactionsInTimespan = database.allTransactionsInTimespan(Configuration.Timespan(
            from = date.with(firstDayOfMonth()),
            to = date.with(lastDayOfMonth()),
            basis = Configuration.TimespanBasis.SALE
        ))
        soldTransactionsByBroker(transactionsInTimespan).forEach { (broker, transactions) ->
            val singleTransactionAmounts = transactions.map { it to Optional.of(amount) }
            if(singleTransactionAmounts.isNotEmpty()) {
                provisions.add(
                    Provision(
                        broker = broker,
                        sum = singleTransactionAmounts
                            .map { it.second.orElse(BigDecimal.ZERO) }
                            .fold(BigDecimal.ZERO, BigDecimal::add),
                        transactions = singleTransactionAmounts.toMap(),
                        configurationName = name(),
                        status = Provision.Status.CALCULATED
                    )
                )
            }
        }
        return provisions
    }

    private fun soldTransactionsByBroker(transactionsInTimespan: List<Transaction>): Map<Broker, List<Transaction>> {
        return transactionsInTimespan
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
                    throw IllegalStateException("Broker is null")
                }
            }
            .toMap()
    }
}
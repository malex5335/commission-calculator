package de.riagade.provisioncalculator.configurations

import de.riagade.provisioncalculator.*
import de.riagade.provisioncalculator.entities.*
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters.firstDayOfMonth
import java.time.temporal.TemporalAdjusters.lastDayOfMonth

fun mapToActiveBrokers(transactions: List<Transaction>, database: Database): Map<Broker, List<Transaction>> {
    return transactions.groupBy { database.brokerFromCode(it.brokerCode) }
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

fun newTransactionsSoldThisMonth(configuration: Configuration, date: LocalDate, database: Database): List<Transaction> {
    val relevantTimespan = Configuration.Timespan(
        from = date.with(firstDayOfMonth()),
        to = date.with(lastDayOfMonth()),
        basis = Configuration.TimespanBasis.SALE
    )
    return database.allTransactionsInTimespan(relevantTimespan)
        .filter { !database.wasCalculatedBefore(it, configuration) }
        .filter { it.status == Transaction.Status.SALE }
}

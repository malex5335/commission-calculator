package de.riagade.commissioncalculator.configurations

import de.riagade.commissioncalculator.*
import de.riagade.commissioncalculator.entities.*
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

fun newTransactionsSoldThisMonth(commissionConfiguration: CommissionConfiguration, date: LocalDate, database: Database): List<Transaction> {
    val relevantTimespan = CommissionConfiguration.Timespan(
        from = date.with(firstDayOfMonth()),
        to = date.with(lastDayOfMonth()),
        basis = CommissionConfiguration.TimespanBasis.SALE
    )
    return database.allTransactionsInTimespan(relevantTimespan)
        .filter { !database.wasCalculatedBefore(it, commissionConfiguration) }
        .filter { it.status == Transaction.Status.SALE }
}

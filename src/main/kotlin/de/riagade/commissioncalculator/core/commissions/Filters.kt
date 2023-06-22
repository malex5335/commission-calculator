package de.riagade.commissioncalculator.core.commissions

import de.riagade.commissioncalculator.core.CommissionConfiguration
import de.riagade.commissioncalculator.core.Database
import de.riagade.commissioncalculator.core.entities.Broker
import de.riagade.commissioncalculator.core.entities.Timespan
import de.riagade.commissioncalculator.core.entities.Transaction
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters.firstDayOfMonth
import java.time.temporal.TemporalAdjusters.lastDayOfMonth

fun validBrokerTransactions(transactions: List<Transaction>, database: Database): Map<Broker, List<Transaction>> {
    return transactions.groupBy { database.brokerFromCode(it.brokerCode) }
        .filter { (broker, _) -> broker != null }
        .map { (broker, transactions) ->
            broker!! to transactions
                .filter { broker.wasActiveAt(it.lead.toLocalDate()) }
                .filter {
                    if (it.status == Transaction.Status.SALE && it.sale != null)
                        return@filter broker.wasActiveAt(it.sale.toLocalDate())
                    return@filter broker.wasActiveAt(it.lead.toLocalDate())
                }
        }.filter { (_, transactions) -> transactions.isNotEmpty() }
        .toMap()
}

fun newTransactionsSoldThisMonth(commissionConfiguration: CommissionConfiguration, date: LocalDate, database: Database): List<Transaction> {
    val timespan = monthTimeSpanBasedOn(Timespan.Basis.SALE, date)
    return database.allTransactionsInTimespan(timespan)
        .filter { !database.wasCalculatedBefore(it, commissionConfiguration) }
        .filter { it.status == Transaction.Status.SALE }
}

fun newTransactionsLeadThisMonth(commissionConfiguration: CommissionConfiguration, date: LocalDate, database: Database): List<Transaction> {
    val timespan = monthTimeSpanBasedOn(Timespan.Basis.LEAD, date)
    return database.allTransactionsInTimespan(timespan)
        .filter { !database.wasCalculatedBefore(it, commissionConfiguration) }
}

fun monthTimeSpanBasedOn(basis: Timespan.Basis, date: LocalDate): Timespan {
    return Timespan(
        from = date.with(firstDayOfMonth()),
        to = date.with(lastDayOfMonth()),
        basis = basis
    )
}

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
            if (broker == null) {
                throw IllegalStateException("Broker is not allowed to be null")
            }
            broker to transactions
                .filter { broker.wasActiveAt(it.lead.toLocalDate()) }
                .filter {
                    if(it.status == Transaction.Status.SALE){
                        if(it.sale == null) {
                            throw IllegalStateException("Sale date is not allowed to be null")
                        }
                        broker.wasActiveAt(it.sale.toLocalDate())
                    } else {
                        true
                    }
                }
        }.toMap()
}

fun newTransactionsSoldThisMonth(commissionConfiguration: CommissionConfiguration, date: LocalDate, database: Database): List<Transaction> {
    val timespan = monthTimeSpanBasedOn(CommissionConfiguration.TimespanBasis.SALE, date)
    return database.allTransactionsInTimespan(timespan)
        .filter { !database.wasCalculatedBefore(it, commissionConfiguration) }
        .filter { it.status == Transaction.Status.SALE }
}

fun newTransactionsLeadThisMonth(commissionConfiguration: CommissionConfiguration, date: LocalDate, database: Database): List<Transaction> {
    val timespan = monthTimeSpanBasedOn(CommissionConfiguration.TimespanBasis.LEAD, date)
    return database.allTransactionsInTimespan(timespan)
        .filter { !database.wasCalculatedBefore(it, commissionConfiguration) }
}

fun monthTimeSpanBasedOn(basis: CommissionConfiguration.TimespanBasis, date: LocalDate): CommissionConfiguration.Timespan {
    return CommissionConfiguration.Timespan(
        from = date.with(firstDayOfMonth()),
        to = date.with(lastDayOfMonth()),
        basis = basis
    )
}

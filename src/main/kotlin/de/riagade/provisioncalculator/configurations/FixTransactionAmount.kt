package de.riagade.provisioncalculator.configurations

import de.riagade.provisioncalculator.*
import de.riagade.provisioncalculator.entities.*
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*

class FixTransactionAmount(
    private val database: Database,
    private val amount: BigDecimal,
    private val name: String,
    private val now: LocalDate = LocalDate.now()
): Configuration {

    override fun name(): String {
        return name
    }

    override fun shouldBeCalculated(): Boolean {
        return now.dayOfWeek.equals(DayOfWeek.MONDAY)
    }

    override fun relevantTimespan(): Configuration.Timespan {
        return Configuration.Timespan(
            start = now.withDayOfMonth(1),
            end = now.withDayOfMonth(now.month.length(now.isLeapYear)),
            basis = Configuration.TimespanBasis.SALE
        )
    }

    override fun calculate(transactionsInTimespan: List<Transaction>): List<Provision> {
        val provisions = mutableListOf<Provision>()
        transactionsByBroker(transactionsInTimespan).forEach { (broker, transactions) ->
            val singleTransactionAmounts = transactions.map { it to Optional.of(amount) }
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
        return provisions
    }

    private fun transactionsByBroker(transactionsInTimespan: List<Transaction>): Map<Broker, List<Transaction>> {
        return transactionsInTimespan
            .filter { !database.wasCalculatedBefore(it, this) }
            .filter { it.status == Transaction.Status.SALE }
            .groupBy { database.brokerFromCode(it.brokerCode) }
            .map { (broker, transactions) ->
                broker to transactions
                    .filter { broker.wasActiveAt(it.lead.toLocalDate()) }
                    .filter { broker.wasActiveAt(it.sale.toLocalDate()) }
            }.toMap()
    }
}
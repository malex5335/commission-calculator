package de.riagade.provisioncalculator.strategies

import de.riagade.provisioncalculator.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

class FixTransactionAmount(
    private val database: Database,
    private val amount: BigDecimal
): Configuration {

    override fun name(): String {
        val uuid = UUID.randomUUID().toString().subSequence(0, 5)
        return "FixAmountConfiguration-$uuid"
    }

    override fun shouldBeCalculated(): Boolean {
        return true
    }

    override fun relevantTimespan(): Configuration.Timespan {
        val now = LocalDate.now()
        return Configuration.Timespan(
            start = now.withDayOfMonth(1),
            end = now.withDayOfMonth(now.month.length(now.isLeapYear)),
            basis = Configuration.TimespanBasis.SALE
        )
    }

    override fun calculate(transactionsInTimespan: List<Transaction>): List<Provision> {
        val provisions = mutableListOf<Provision>()
        val transactionsForBroker = transactionsInTimespan
            .filter { !database.wasCalculatedBefore(it, this) }
            .filter { it.status == Transaction.Status.SALE }
            .groupBy { database.brokerFromCode(it.brokerCode) }
            .map { (broker, transactions) ->
                broker to transactions
                    .filter { broker.wasActiveAt(it.lead.toLocalDate()) }
                    .filter { broker.wasActiveAt(it.sale.toLocalDate()) }
            }
        transactionsForBroker.forEach { (broker, transactions) ->
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
}
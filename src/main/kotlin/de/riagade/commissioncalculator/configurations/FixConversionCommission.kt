package de.riagade.commissioncalculator.configurations

import de.riagade.commissioncalculator.CommissionConfiguration
import de.riagade.commissioncalculator.Database
import de.riagade.commissioncalculator.entities.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

/**
 * A configuration that calculates a fixed amount to a broker depending on the number of transactions sold the
 * previous month in relation to all transactions.
 */
class FixConversionCommission(
    val name: String,
    val crValues: Map<ConversionRate, BigDecimal>
) : CommissionConfiguration {
    override fun name(): String {
        return name
    }

    override fun canBeCalculated(date: LocalDate): Boolean {
        return date.dayOfMonth == 1
    }

    override fun calculate(date: LocalDate, database: Database): List<Commission> {
        val commissions = mutableListOf<Commission>()
        val relevantTransactions = newTransactionsLeadThisMonth(this, date.minusMonths(1), database)
        mapToActiveBrokers(relevantTransactions, database)
            .forEach { (broker, transactions) ->
                val transactionAmounts = transactions.map { it to Optional.empty<BigDecimal>() }
                if(transactionAmounts.isNotEmpty()) {
                    val sum = ConversionRate.of(
                        relevantCount = transactions.filter { it.status == Transaction.Status.SALE }.size,
                        notRelevantCount = transactions.filter { it.status != Transaction.Status.SALE }.size
                    ).calculate(crValues)
                    if(sum != BigDecimal.ZERO) {
                        commissions.add(
                            Commission(
                                broker = broker,
                                sum = sum,
                                transactions = transactionAmounts.toMap(),
                                configurationName = name(),
                                status = Commission.Status.CALCULATED
                            )
                        )
                    }
                }
            }
        return commissions
    }
}

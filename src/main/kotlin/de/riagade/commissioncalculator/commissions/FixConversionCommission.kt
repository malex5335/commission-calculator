package de.riagade.commissioncalculator.commissions

import de.riagade.commissioncalculator.CommissionConfiguration
import de.riagade.commissioncalculator.Database
import de.riagade.commissioncalculator.entities.Commission
import de.riagade.commissioncalculator.entities.ConversionRate
import de.riagade.commissioncalculator.entities.Transaction
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
        val dateFilteredTransactions = newTransactionsLeadThisMonth(this, date.minusMonths(1), database)
        return validBrokerTransactions(dateFilteredTransactions, database)
            .map { (broker, transactions) ->
                Commission(
                    broker = broker,
                    sum = ConversionRate.of(
                        relevantCount = transactions.filter { it.status == Transaction.Status.SALE }.size,
                        notRelevantCount = transactions.filter { it.status != Transaction.Status.SALE }.size
                    ).calculate(crValues),
                    transactions = transactions.associateWith { Optional.empty<BigDecimal>() },
                    configurationName = name(),
                    status = Commission.Status.CALCULATED
                )
            }.filter { it.sum.compareTo(BigDecimal.ZERO) != 0 }
    }
}

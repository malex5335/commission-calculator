package de.riagade.commissioncalculator.core.commissions

import de.riagade.commissioncalculator.core.CommissionConfiguration
import de.riagade.commissioncalculator.core.Database
import de.riagade.commissioncalculator.core.entities.Commission
import de.riagade.commissioncalculator.core.entities.Percentage
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*

/**
 * A configuration that calculates a percentage of the volume for each transaction sold this month.
 */
class VolumeTransactionCommission(
    val name: String,
    val percent: Percentage
): CommissionConfiguration {
    override fun name(): String {
        return name
    }

    override fun canBeCalculated(date: LocalDate): Boolean {
        return date.dayOfWeek.equals(DayOfWeek.MONDAY)
    }

    override fun calculate(date: LocalDate, database: Database): List<Commission> {
        return validBrokerTransactions(newTransactionsSoldThisMonth(this, date, database), database)
            .map { (broker, transactions) ->
                val transactionAmounts = transactions.map { it to Optional.of(percent.calculate(it.volume)) }
                Commission(
                    broker = broker,
                    sum = transactionAmounts.sumOf { it.second.orElse(BigDecimal.ZERO) },
                    transactions = transactionAmounts.toMap(),
                    configurationName = name(),
                    status = Commission.Status.CALCULATED,
                    scopeDate = date
                )
            }
    }
}

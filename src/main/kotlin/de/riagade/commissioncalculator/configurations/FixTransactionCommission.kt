package de.riagade.commissioncalculator.configurations

import de.riagade.commissioncalculator.*
import de.riagade.commissioncalculator.entities.*
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*

/**
 * A configuration that calculates a fixed amount for each transaction sold this month.
 */
class FixTransactionCommission(
    private val name: String,
    private val amount: BigDecimal
): CommissionConfiguration {
    override fun name(): String {
        return name
    }

    override fun canBeCalculated(date: LocalDate): Boolean {
        return date.dayOfWeek.equals(DayOfWeek.MONDAY)
    }

    override fun calculate(date: LocalDate, database: Database): List<Commission> {
        val commissions = mutableListOf<Commission>()
        val relevantTransactions = newTransactionsSoldThisMonth(this, date, database)
        mapToActiveBrokers(relevantTransactions, database)
            .forEach { (broker, transactions) ->
                val transactionAmounts = transactions.map { it to Optional.of(amount) }
                if(transactionAmounts.isNotEmpty()) {
                    commissions.add(
                        Commission(
                            broker = broker,
                            sum = transactionAmounts.sumOf { it.second.orElse(BigDecimal.ZERO) },
                            transactions = transactionAmounts.toMap(),
                            configurationName = name(),
                            status = Commission.Status.CALCULATED
                        )
                    )
                }
            }
        return commissions
    }
}

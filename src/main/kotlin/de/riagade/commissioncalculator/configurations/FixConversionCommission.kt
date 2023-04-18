package de.riagade.commissioncalculator.configurations

import de.riagade.commissioncalculator.CommissionConfiguration
import de.riagade.commissioncalculator.Database
import de.riagade.commissioncalculator.entities.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

class FixConversionCommission(
    val name: String,
    val crValues: Map<ConversionRate, BigDecimal>
) : CommissionConfiguration {
    override fun name(): String {
        return name
    }

    override fun canBeCalculated(date: LocalDate): Boolean {
        TODO("Not yet implemented")
    }

    override fun calculate(date: LocalDate, database: Database): List<Commission> {
        val commissions = mutableListOf<Commission>()
        val relevantTransactions = newTransactionsLeadInYear(this, date.year, database)
        mapToActiveBrokers(relevantTransactions, database)
            .forEach { (broker, transactions) ->
                val transactionAmounts = transactions.map { it to Optional.empty<BigDecimal>() }
                if(transactionAmounts.isNotEmpty()) {
                    val leadCount = transactions.filter { it.status == Transaction.Status.LEAD }.size
                    val saleCount = transactions.filter { it.status == Transaction.Status.SALE }.size
                    val sum = ConversionRate.of(leadCount, saleCount).calculate(crValues)
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

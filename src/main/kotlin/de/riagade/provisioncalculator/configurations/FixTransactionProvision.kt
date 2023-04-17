package de.riagade.provisioncalculator.configurations

import de.riagade.provisioncalculator.*
import de.riagade.provisioncalculator.entities.*
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*

/**
 * A configuration that calculates a fixed amount for each transaction sold this month.
 */
class FixTransactionProvision(
    private val name: String,
    private val amount: BigDecimal
): ProvisionConfiguration {

    override fun name(): String {
        return name
    }

    override fun canBeCalculated(date: LocalDate): Boolean {
        return date.dayOfWeek.equals(DayOfWeek.MONDAY)
    }

    override fun calculate(date: LocalDate, database: Database): List<Provision> {
        val provisions = mutableListOf<Provision>()
        val relevantTransactions = newTransactionsSoldThisMonth(this, date, database)
        mapToActiveBrokers(relevantTransactions, database)
            .forEach { (broker, transactions) ->
                val transactionAmounts = transactions.map { it to Optional.of(amount) }
                if(transactionAmounts.isNotEmpty()) {
                    provisions.add(
                        Provision(
                            broker = broker,
                            sum = transactionAmounts.sumOf { it.second.orElse(BigDecimal.ZERO) },
                            transactions = transactionAmounts.toMap(),
                            configurationName = name(),
                            status = Provision.Status.CALCULATED
                        )
                    )
                }
            }
        return provisions
    }
}
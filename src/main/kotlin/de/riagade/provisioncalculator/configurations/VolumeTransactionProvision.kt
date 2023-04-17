package de.riagade.provisioncalculator.configurations

import de.riagade.provisioncalculator.ProvisionConfiguration
import de.riagade.provisioncalculator.Database
import de.riagade.provisioncalculator.entities.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*

/**
 * A configuration that calculates a percentage of the volume for each transaction sold this month.
 */
class VolumeTransactionProvision(
    val name: String,
    val percent: Percentage
): ProvisionConfiguration {
    override fun name(): String {
        return name
    }

    override fun canBeCalculatedAt(date: LocalDate): Boolean {
        return date.dayOfWeek.equals(DayOfWeek.MONDAY)
    }

    override fun calculate(date: LocalDate, database: Database): List<Provision> {
        val provisions = mutableListOf<Provision>()
        val relevantTransactions = newTransactionsSoldThisMonth(this, date, database)
        mapToActiveBrokers(relevantTransactions, database)
            .forEach { (broker, transactions) ->
                val transactionAmounts = transactions.map { it to Optional.of(percent.calculate(it.volume)) }
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

    data class Percentage(
        private val value: Double
    ) {
        fun calculate(amount: BigDecimal): BigDecimal {
            return amount.multiply(BigDecimal(value)).divide(BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)
        }

        companion object {
            fun of(value: Double): Percentage {
                if(value !in 0.0..100.0) {
                    throw IllegalArgumentException("Percentage $value must be between 0 and 100")
                }
                val roundedValue = BigDecimal(value).setScale(2, RoundingMode.HALF_UP).toDouble()
                return Percentage(roundedValue)
            }
        }
    }
}

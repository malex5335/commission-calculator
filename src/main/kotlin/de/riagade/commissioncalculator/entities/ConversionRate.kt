package de.riagade.commissioncalculator.entities

import java.math.BigDecimal
import java.math.RoundingMode

data class ConversionRate(
    private val value : Double
) {

    fun calculate(crValues: Map<ConversionRate, BigDecimal>): BigDecimal {
        crValues.toSortedMap(compareByDescending { it.value })
            .forEach { (cr, value) ->
                if(this.value >= cr.value) {
                    return value
                }
            }
        return BigDecimal.ZERO
    }

    companion object {
        fun of(value: Double): ConversionRate {
            if(value !in 0.0..1.0) {
                throw IllegalArgumentException("Conversion rate must be between 0 and 1")
            }
            val roundedValue = BigDecimal(value).setScale(2, RoundingMode.HALF_UP).toDouble()
            return ConversionRate(roundedValue)
        }

        fun of(relevantCount: Int, notRelevantCount: Int): ConversionRate {
            val total = relevantCount + notRelevantCount
            return of(relevantCount.toDouble() / total.toDouble())
        }
    }
}
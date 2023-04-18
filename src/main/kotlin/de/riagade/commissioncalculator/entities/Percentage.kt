package de.riagade.commissioncalculator.entities

import java.math.BigDecimal
import java.math.RoundingMode

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

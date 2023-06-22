package de.riagade.commissioncalculator.core.entities

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToInt

data class Percentage(
    private val value: Float
) {
    fun calculate(amount: BigDecimal): BigDecimal {
        return amount.multiply(BigDecimal((value/100).toDouble())).setScale(2, RoundingMode.HALF_UP)
    }

    companion object {
        fun of(value: Float): Percentage {
            if(value !in 0.0..100.0) {
                throw IllegalArgumentException("Percentage $value must be between 0 and 100")
            }
            val roundedValue = (value*100).roundToInt() / 100f
            return Percentage(roundedValue)
        }
    }
}

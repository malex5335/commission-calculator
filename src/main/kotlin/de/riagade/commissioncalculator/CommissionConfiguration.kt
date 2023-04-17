package de.riagade.commissioncalculator

import de.riagade.commissioncalculator.entities.*
import java.time.LocalDate

interface CommissionConfiguration {
    fun name(): String
    fun canBeCalculated(date: LocalDate): Boolean
    fun calculate(date: LocalDate, database: Database): List<Commission>

    data class Timespan(
        val from: LocalDate,
        val to: LocalDate,
        val basis: TimespanBasis
    )

    enum class TimespanBasis {
        CREATED, UPDATED, LEAD, SALE
    }
}

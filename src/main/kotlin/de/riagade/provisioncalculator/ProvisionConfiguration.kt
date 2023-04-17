package de.riagade.provisioncalculator

import de.riagade.provisioncalculator.entities.*
import java.time.LocalDate

interface ProvisionConfiguration {
    fun name(): String
    fun canBeCalculated(date: LocalDate): Boolean
    fun calculate(date: LocalDate, database: Database): List<Provision>

    data class Timespan(
        val from: LocalDate,
        val to: LocalDate,
        val basis: TimespanBasis
    )

    enum class TimespanBasis {
        CREATED, UPDATED, LEAD, SALE
    }
}
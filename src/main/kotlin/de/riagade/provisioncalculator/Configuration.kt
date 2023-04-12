package de.riagade.provisioncalculator

import de.riagade.provisioncalculator.entities.*
import java.time.LocalDate

interface Configuration {
    fun name(): String
    fun shouldBeCalculated(): Boolean
    fun relevantTimespan(): Timespan
    fun calculate(transactionsInTimespan: List<Transaction>): List<Provision>

    data class Timespan(
        val start: LocalDate,
        val end: LocalDate,
        val basis: TimespanBasis
    )

    enum class TimespanBasis {
        CREATED, UPDATED, LEAD, SALE
    }
}
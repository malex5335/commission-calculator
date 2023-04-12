package de.riagade.provisioncalculator

import java.time.LocalDate

interface Configuration {
    fun name(): String
    fun shouldBeCalculated(): Boolean
    fun relevantTimespan(): ConfigurationTimespan
    fun calculate(transactionsInTimespan: List<Transaction>): List<Provision>

    data class ConfigurationTimespan(
        val start: LocalDate,
        val end: LocalDate,
        val basis: ConfigurationTimespanBasis
    )

    enum class ConfigurationTimespanBasis {
        CREATED, UPDATED, LEAD, SALE
    }
}
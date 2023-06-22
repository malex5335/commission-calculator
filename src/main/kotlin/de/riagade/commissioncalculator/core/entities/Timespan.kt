package de.riagade.commissioncalculator.core.entities

import java.time.LocalDate

data class Timespan(
    val from: LocalDate,
    val to: LocalDate,
    val basis: Basis
) {
    enum class Basis {
        CREATED, UPDATED, LEAD, SALE
    }
}

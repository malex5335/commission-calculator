package de.riagade.commissioncalculator.core

import de.riagade.commissioncalculator.core.entities.Commission
import java.time.LocalDate

interface CommissionConfiguration {
    fun name(): String
    fun canBeCalculated(date: LocalDate): Boolean
    fun calculate(date: LocalDate, database: Database): List<Commission>
}

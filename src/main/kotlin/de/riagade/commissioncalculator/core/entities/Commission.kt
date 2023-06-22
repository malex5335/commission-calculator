package de.riagade.commissioncalculator.core.entities

import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

data class Commission(
    val broker: Broker,
    val sum: BigDecimal,
    val transactions: Map<Transaction, Optional<BigDecimal>>,
    val configurationName: String,
    val status: Status,
    val scopeDate: LocalDate,
    val triggerDate: LocalDate = LocalDate.now()
) {
    enum class Status {
        CALCULATED, ACCEPTED, REJECTED
    }
}

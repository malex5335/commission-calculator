package de.riagade.provisioncalculator.entities

import java.math.BigDecimal
import java.util.*

data class Provision(
    val broker: Broker,
    val sum: BigDecimal,
    val transactions: Map<Transaction, Optional<BigDecimal>>,
    val configurationName: String,
    val status: Status
) {
    enum class Status {
        CALCULATED, ACCEPTED, REJECTED
    }
}

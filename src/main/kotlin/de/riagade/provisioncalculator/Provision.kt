package de.riagade.provisioncalculator

import java.math.BigDecimal
import java.util.*

data class Provision(
    val broker: Broker,
    val sum: BigDecimal,
    val transactions: Map<Transaction, Optional<BigDecimal>>,
    val configurationName: String,
    val status: ProvisionStatus
) {
    enum class ProvisionStatus {
        CALCULATED, ACCEPTED, REJECTED
    }
}

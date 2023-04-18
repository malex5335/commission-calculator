package de.riagade.commissioncalculator.entities

import java.math.BigDecimal
import java.time.LocalDateTime

data class Transaction(
    val id: String,
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val lead: LocalDateTime,
    val sale: LocalDateTime?,
    val status: Status,
    val product: Product,
    val volume: BigDecimal,
    val brokerCode: String,
    val additionalOptions: Map<String, String>
) {
    enum class Status {
        LEAD, SALE
    }
}

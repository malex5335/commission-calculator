package de.riagade.provisioncalculator

import java.math.BigDecimal
import java.time.LocalDateTime

data class Transaction(
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val lead: LocalDateTime,
    val sale: LocalDateTime,
    val status: TransactionStatus,
    val product: Product,
    val volume: BigDecimal,
    val brokerCode: String,
    val additionalOptions: Map<String, String>
) {
    enum class TransactionStatus {
        LEAD, SALE
    }
}

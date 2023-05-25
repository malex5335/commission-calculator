package de.riagade.commissioncalculator.entities

import java.time.LocalDate

data class Broker(
    val name: String,
    val statusHistory: Map<LocalDate, Status>,
    val codes: List<String>,
    val bankDetails: BankDetails
) {
    enum class Status {
        ACTIVE, INACTIVE
    }

    data class BankDetails(
        val bankName: String,
        val iban: String,
        val bic: String
    )

    fun wasActiveAt(date: LocalDate): Boolean {
        var beforeDate = LocalDate.MAX
        statusHistory.toSortedMap(Comparator.reverseOrder()).forEach { (dateOfStatus, status) ->
            if (date in dateOfStatus..beforeDate) {
                return status == Status.ACTIVE
            }
            beforeDate = dateOfStatus
        }
        return false
    }
}

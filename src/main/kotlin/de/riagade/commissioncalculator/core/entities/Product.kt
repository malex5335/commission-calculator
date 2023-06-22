package de.riagade.commissioncalculator.core.entities

data class Product(
    val name: String,
    val group: Group,
    val mandatoryOptions: List<String>
) {
    data class Group(
        val name: String,
        val mandatoryOptions: List<String>
    )
}

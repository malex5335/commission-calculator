package de.riagade.provisioncalculator.entities

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
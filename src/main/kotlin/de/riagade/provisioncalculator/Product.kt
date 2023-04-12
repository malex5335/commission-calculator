package de.riagade.provisioncalculator

data class Product(
    val name: String,
    val group: ProductGroup,
    val mandatoryOptions: List<String>
) {
    data class ProductGroup(
        val name: String,
        val mandatoryOptions: List<String>
    )
}
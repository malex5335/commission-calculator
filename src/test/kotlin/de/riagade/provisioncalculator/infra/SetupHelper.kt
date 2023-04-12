package de.riagade.provisioncalculator.infra

import de.riagade.provisioncalculator.configurations.FixTransactionAmount
import de.riagade.provisioncalculator.entities.Broker
import de.riagade.provisioncalculator.entities.Product
import de.riagade.provisioncalculator.entities.Transaction
import de.riagade.provisioncalculator.mock.MockDatabase
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

class SetupHelper(
    private val database: MockDatabase
) {
    fun transaction(id: String, brokerCode: String, lead: LocalDate, sale: LocalDate, product: Product): Transaction {
        val transaction = Transaction(
            id = id,
            brokerCode = brokerCode,
            lead = lead.atStartOfDay(),
            sale = sale.atStartOfDay(),
            product = product,
            created = lead.atStartOfDay(),
            updated = sale.atStartOfDay(),
            status = Transaction.Status.SALE,
            volume = BigDecimal.valueOf(100),
            additionalOptions = mapOf()
        )
        database.transactions.add(
            transaction
        )
        return transaction
    }

    fun fixProvision(name: String, amount: BigDecimal): FixTransactionAmount {
        val configuration = FixTransactionAmount(database, amount, name)
        database.configurations.add(configuration)
        return configuration
    }

    fun product(name: String, groupName: String): Product {
        return Product(
            name = name,
            group = Product.Group(
                name = groupName,
                mandatoryOptions = listOf()
            ),
            mandatoryOptions = listOf()
        )
    }

    fun broker(name: String, brokerCodes: List<String>, statusHistory: Map<LocalDate, Broker.Status>): Broker {
        val broker = Broker(
            name = name,
            statusHistory = statusHistory,
            codes = brokerCodes,
            bankDetails = Broker.BankDetails(
                bankName = randomString(),
                iban = randomString(),
                bic = randomString()
            )
        )
        database.brokers.add(broker)
        return broker
    }

}

fun randomString(): String {
    return UUID.randomUUID().toString().substring(0,5)
}
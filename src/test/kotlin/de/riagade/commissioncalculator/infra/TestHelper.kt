package de.riagade.commissioncalculator.infra

import de.riagade.commissioncalculator.*
import de.riagade.commissioncalculator.configurations.VolumeTransactionCommission
import de.riagade.commissioncalculator.entities.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.*
import java.util.*
import kotlin.random.asKotlinRandom

fun a_configuration(
    database: MockDatabase? = null,
    name: String = randomString(),
    canBeCalculatedAt: (LocalDate) -> Boolean = { true },
    calculate: (LocalDate, Database) -> List<Commission> = { _, _ -> emptyList() }
): CommissionConfiguration {
    val commissionConfiguration = object : CommissionConfiguration {
        override fun name(): String = name
        override fun canBeCalculated(date: LocalDate): Boolean = canBeCalculatedAt.invoke(date)
        override fun calculate(date: LocalDate, database: Database): List<Commission> = calculate.invoke(date, database)
    }
    database?.commissionConfigurations?.add(commissionConfiguration)
    return commissionConfiguration
}

fun a_provision(
    database: MockDatabase? = null,
    broker: Broker = a_broker(database = database),
    sum: BigDecimal = randomAmount(),
    transactions: Map<Transaction, Optional<BigDecimal>> = emptyMap(),
    configurationName: String = randomString(),
    status: Commission.Status = Commission.Status.CALCULATED
): Commission {
    val commission = Commission(
        broker = broker,
        sum = sum,
        transactions = transactions,
        configurationName = configurationName,
        status = status
    )
    database?.commissions?.add(commission)
    return commission
}

fun multiple_transactions(
    leadCount: Int,
    saleCount: Int,
    date: LocalDate,
    brokerCode: String,
    database: MockDatabase
): List<Transaction> {
    val transactions = mutableListOf<Transaction>()
    for (i in 1..leadCount + saleCount) {
        transactions.add(a_transaction(
            created = date.atStartOfDay(),
            updated = date.atStartOfDay(),
            lead = date.atStartOfDay(),
            sale = date.atStartOfDay(),
            status = if (i <= leadCount) Transaction.Status.LEAD else Transaction.Status.SALE,
            brokerCode = brokerCode,
            database = database
        ))
    }
    return transactions
}

fun a_transaction(
    database: MockDatabase? = null,
    id: String = randomString(),
    created: LocalDateTime = LocalDateTime.now(),
    updated: LocalDateTime = LocalDateTime.now(),
    lead: LocalDateTime = LocalDateTime.now(),
    sale: LocalDateTime = LocalDateTime.now(),
    status: Transaction.Status = Transaction.Status.LEAD,
    product: Product = a_product(database = database),
    volume: BigDecimal = randomAmount(),
    brokerCode: String = randomString(),
    additionalOptions: Map<String, String> = emptyMap()
): Transaction {
    val transaction = Transaction(
        id = id,
        created = created,
        updated = updated,
        lead = lead,
        sale = sale,
        status = status,
        product = product,
        volume = volume,
        brokerCode = brokerCode,
        additionalOptions = additionalOptions
    )
    database?.transactions?.add(transaction)
    return transaction
}

fun a_product(
    database: MockDatabase? = null,
    name: String = randomString(),
    group: Product.Group = a_group(database = database),
    mandatoryOptions: List<String> = emptyList()
): Product {
    val product = Product(
        name = name,
        group = group,
        mandatoryOptions = mandatoryOptions
    )
    database?.products?.add(product)
    return product
}

fun a_group(
    database: MockDatabase? = null,
    name: String = randomString(),
    mandatoryOptions: List<String> = emptyList()
): Product.Group {
    val group = Product.Group(
        name = name,
        mandatoryOptions = mandatoryOptions
    )
    database?.groups?.add(group)
    return group
}

fun a_broker(
    database: MockDatabase? = null,
    name: String = randomString(),
    statusHistory: Map<LocalDate, Broker.Status> = emptyMap(),
    codes: List<String> = emptyList(),
    bankDetails: Broker.BankDetails = some_bankDetails(database = database)
): Broker {
    val broker = Broker(
        name = name,
        statusHistory = statusHistory,
        codes = codes,
        bankDetails = bankDetails
    )
    database?.brokers?.add(broker)
    return broker
}

fun some_bankDetails(
    bankName: String = randomString(),
    iban: String = randomString(),
    bic: String = randomString(),
    database: MockDatabase? = null
): Broker.BankDetails {
    val bankDetails = Broker.BankDetails(
        bankName = bankName,
        iban = iban,
        bic = bic
    )
    database?.bankDetails?.add(bankDetails)
    return bankDetails
}

fun mark_calculated_before(
    transaction: Transaction,
    commissionConfiguration: CommissionConfiguration,
    database: MockDatabase
) {
    database.wasCalculatedBefore[transaction] = commissionConfiguration
}

fun randomString(): String {
    return UUID.randomUUID().toString().substring(0,5)
}

fun randomAmount(): BigDecimal {
    return BigDecimal(random().nextDouble(0.0, 100_000_000.0)).setScale(2, RoundingMode.HALF_EVEN)
}

fun randomPercentage(): Percentage {
    return Percentage.of(random().nextDouble(0.0, 100.0))
}

fun randomDate(): LocalDate {
    return LocalDate.ofEpochDay(random().nextLong(0, 365 * 100))
}

fun random(): kotlin.random.Random {
    return Random().asKotlinRandom()
}

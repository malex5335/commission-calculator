package de.riagade.provisioncalculator

import java.time.LocalDate
import de.riagade.provisioncalculator.Broker.*

class BrokerFinder {
    fun findBrokerForCode(brokerCode: String): Broker {
        return Broker(
            name = "Broker-$brokerCode",
            statusHistory = mapOf(LocalDate.now().minusMonths(2) to BrokerStatus.ACTIVE),
            codes = listOf(brokerCode),
            bankDetails = BankDetails(
                bankName = "Bank",
                iban = "DE1234567890",
                bic = "DEUTDEFFXXX"
            )
        )
    }
}
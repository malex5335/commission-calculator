package de.riagade.commissioncalculator.spring

import com.google.gson.Gson
import de.riagade.commissioncalculator.core.Calculation
import de.riagade.commissioncalculator.core.entities.Timespan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@SpringBootApplication
@RestController
open class App {
    fun main(args: Array<String>) {
        runApplication<App>(*args)
    }
    @GetMapping("/calculate")
    fun calculate(
        @RequestParam("date") date: LocalDate
    ): String {
        Calculation(database = MainDb()).calculateConfigurations(date)
        return "{\"result\": \"Calculated commissions for $date successfully.\"}"
    }

    @GetMapping("/commissions/broker")
    fun commissionsForBroker(
        @RequestParam("id") brokerId: String
    ) = Gson().toJson(MainDb().commissionsForBroker(brokerId))

    @GetMapping("/commissions/scoped")
    fun commissionsFromScopeDate(
        @RequestParam("date") date: LocalDate
    ) = Gson().toJson(MainDb().commissionsFromScopeDate(date))

    @GetMapping("/commissions/triggered")
    fun commissionsFromTriggerDate(
        @RequestParam("date") date: LocalDate
    ) = Gson().toJson(MainDb().commissionsFromTriggerDate(date))

    @GetMapping("/transactions/broker")
    fun transactionsForBroker(
        @RequestParam("id") brokerId: String,
        @RequestParam("span") timespan: Timespan
    ) = Gson().toJson(MainDb().transactionsForBroker(brokerId, timespan))

    @GetMapping("/transactions/brokerCode")
    fun transactionsForBrokerCode(
        @RequestParam("id") brokerId: String,
        @RequestParam("span") timespan: Timespan
    ) = Gson().toJson(MainDb().transactionsForBrokerCode(brokerId, timespan))

    @GetMapping("/transactions")
    fun transactionsForTimespan(
        @RequestParam("span") timespan: Timespan
    ) = Gson().toJson(MainDb().allTransactionsInTimespan(timespan))
}

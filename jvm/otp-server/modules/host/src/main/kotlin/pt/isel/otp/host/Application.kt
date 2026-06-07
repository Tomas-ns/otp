package pt.isel.otp.host

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["pt.isel.otp"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

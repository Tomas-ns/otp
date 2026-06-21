package pt.isel.otp.host

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["pt.isel.otp"])
@EnableJpaRepositories(basePackages = ["pt.isel.otp.repository"])
@EntityScan(basePackages = ["pt.isel.otp.domain.entity"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

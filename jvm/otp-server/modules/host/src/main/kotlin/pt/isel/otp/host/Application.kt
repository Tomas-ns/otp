package pt.isel.otp.host

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["pt.isel.otp"])
@EnableJpaRepositories(basePackages = ["pt.isel.otp.repository"])
@EntityScan(basePackages = ["pt.isel.otp.domain.entity"])
@EnableScheduling
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

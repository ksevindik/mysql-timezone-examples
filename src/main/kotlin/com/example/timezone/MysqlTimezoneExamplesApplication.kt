package com.example.timezone

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.TimeZone
import javax.annotation.PostConstruct


@SpringBootApplication
class MysqlTimezoneExamplesApplication {
	@PostConstruct
	fun init() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
	}
}

fun main(args: Array<String>) {
	runApplication<MysqlTimezoneExamplesApplication>(*args)
}

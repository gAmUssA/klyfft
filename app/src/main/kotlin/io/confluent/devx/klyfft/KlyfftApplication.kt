package io.confluent.devx.klyfft

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@SpringBootApplication
class KlyfftApplication

fun main(args: Array<String>) {
  runApplication<KlyfftApplication>(*args)
}

@Configuration
class Configuration {
  @Bean
  fun serializer() = jacksonObjectMapper()
}
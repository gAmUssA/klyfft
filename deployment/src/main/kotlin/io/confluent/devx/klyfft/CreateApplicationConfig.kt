package io.confluent.devx.klyfft

import com.fkorotkov.kubernetes.client.DefaultKafkaClient
import com.fkorotkov.kubernetes.metadata
import com.fkorotkov.kubernetes.newSecret
import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import java.io.File
import java.io.FileInputStream
import java.io.StringReader
import java.io.StringWriter
import java.util.*
import kotlin.system.exitProcess

fun main(args: Array<String>) {
  val config = Config.autoConfigure(null)
  val client = DefaultKubernetesClient(config).inNamespace("default")
  val kafkaClient = DefaultKafkaClient(config).inNamespace("operator")

  if (args.size != 1) {
    System.err.println("Expected one argument!")
    exitProcess(1)
  }

  val (configPath) = args

  val propertiesToOverride = kafkaClient.kafkaClusters().list().items.firstOrNull()?.let {
    extractSpringProperties(it.status.internalClient)
  } ?: emptyMap()

  val propertiesFileContent = createProperties(configPath, propertiesToOverride)

  val secretData = mapOf(
      "application.properties" to propertiesFileContent.toBase64()
  )

  client.secrets().createOrReplace(
      newSecret {
        type = "Opaque"
        metadata {
          name = "application-config"
        }
        data = secretData
      }
  )
}

private fun createProperties(secretValue: String, propertiesToMerge: Map<String, String>): String {
  val propertiesFile = File(secretValue)

  if (!propertiesFile.exists()) {
    System.err.println("No properties file provided!")
    exitProcess(1)
  }

  val properties = Properties().apply {
    load(FileInputStream(propertiesFile))
  }

  propertiesToMerge.forEach { name, value ->
    properties.setProperty(name, value)
  }

  val propertiesFileContent = StringWriter().also {
    properties.store(it, "application config")
  }.toString()
  return propertiesFileContent
}

private fun extractSpringProperties(internalClient: String): Map<String, String> {
  val internalClientProperties = Properties().apply {
    load(StringReader(internalClient))
  }

  return mapOf(
      "spring.kafka.bootstrap-servers" to internalClientProperties.getProperty("bootstrap.servers"),
      "spring.kafka.properties.sasl.jaas.config" to internalClientProperties.getProperty("sasl.jaas.config"),
      "spring.kafka.properties.sasl.mechanism" to internalClientProperties.getProperty("sasl.mechanism"),
      "spring.kafka.properties.security.protocol" to internalClientProperties.getProperty("security.protocol")
  )
}
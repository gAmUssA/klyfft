package io.confluent.devx.klyfft

import com.fkorotkov.kubernetes.client.DefaultKafkaClient
import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.DefaultKubernetesClient

fun main() {
  val config = Config.autoConfigure(null)
  val client = DefaultKubernetesClient(config).inNamespace("default")

  val kafkaClient = DefaultKafkaClient().inNamespace("operator")
}
package io.confluent.devx.klyfft

import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import kotlin.system.exitProcess

fun main() {
  val config = Config.autoConfigure(null)
  val client = DefaultKubernetesClient(config)

  val klyfftDeployment = client.apps().deployments().list().items.find { it.metadata.name == "klyfft" }

  if (klyfftDeployment == null) {
    println("No klyfft found!")
    exitProcess(1)
  }

  val secretNames = klyfftDeployment.spec.template.spec.volumes.mapNotNull { it.secret }.map { it.secretName }.toSet()
  val secretsToDelete = client.secrets().list().items.filter { secretNames.contains(it.metadata.name) }

  client.apps().deployments().delete(klyfftDeployment)
  client.secrets().delete(secretsToDelete)
}
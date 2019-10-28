package io.confluent.devx.klyfft


import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.DefaultKubernetesClient

fun main(args: Array<String>) {
  val lookupSecretName = args.firstOrNull() ?: throw IllegalStateException("Must provide secret name as an argument!")

  val config = Config.autoConfigure(null)
  val client = DefaultKubernetesClient(config).inNamespace("default")

  val deploymentsWithTheSecret = client.apps().deployments().list().items.filter { deployment ->
    deployment.spec.template.spec.volumes.any { volume ->
      val secret = volume.secret
      secret != null && secret.secretName == lookupSecretName
    }
  }

  println(deploymentsWithTheSecret.map { it.metadata.name })
}
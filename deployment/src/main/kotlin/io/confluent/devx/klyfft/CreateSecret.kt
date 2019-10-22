package io.confluent.devx.klyfft

import com.fkorotkov.kubernetes.metadata
import com.fkorotkov.kubernetes.newSecret
import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.DefaultKubernetesClient

fun main(args: Array<String>) {
  val config = Config.autoConfigure(null)
  val client = DefaultKubernetesClient(config).inNamespace("default")

  if (args.size != 2) {
    System.err.println("Expected two arguments! Secret name and value.")
  }

  val (secretName, secretValue) = args

  client.secrets().createOrReplace(
      newSecret {
        type = "Opaque"
        metadata {
          name = secretName
        }
        data = mapOf(
            "token" to secretValue.toBase64()
        )
      }
  )
}
package io.confluent.devx.klyfft

import com.fkorotkov.kubernetes.metadata
import com.fkorotkov.kubernetes.newSecret
import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
  val config = Config.autoConfigure(null)
  val client = DefaultKubernetesClient(config).inNamespace("default")

  if (args.size != 2) {
    System.err.println("Expected two arguments! Secret name and value.")
    exitProcess(1)
  }

  val (secretName, secretValue) = args

  val propertiesFile = File(secretValue)

  val secretData = if (propertiesFile.exists()) {
    mapOf(
        propertiesFile.name to propertiesFile.readText().toBase64()
    )
  } else {
    mapOf(
        "value" to secretValue.toBase64()
    )
  }

  client.secrets().createOrReplace(
      newSecret {
        type = "Opaque"
        metadata {
          name = secretName
        }
        data = secretData
      }
  )
}
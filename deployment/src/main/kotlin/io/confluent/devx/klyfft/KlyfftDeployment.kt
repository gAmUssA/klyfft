package io.confluent.devx.klyfft

import com.fkorotkov.kubernetes.*
import com.fkorotkov.kubernetes.client.DefaultKafkaClient
import io.confluent.devx.klyfft.base.BaseDeployment
import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.DefaultKubernetesClient

fun main() {
  val config = Config.autoConfigure(null)
  val client = DefaultKubernetesClient(config).inNamespace("default")
  val kafkaClient = DefaultKafkaClient().inNamespace("operator")

  val klyfftDeployment = BaseDeployment("klyfft").apply {
    spec.template.spec {
      containers = listOf(
          newContainer {
            name = serviceName
            image = "gamussa/klyfft:latest"
            volumeMounts = listOf(
                newVolumeMount {
                  name = "application-config"
                  mountPath = "/etc/credentials/application-config"
                  readOnly = true
                }
            )
            env = listOf(
                newEnvVar {
                  name = "SPRING_CONFIG_LOCATION"
                  value = "/etc/credentials/application-config/application.properties"
                }
            )
          }
      )

      volumes = listOf(
          newVolume {
            name = "application-config"
            secret {
              secretName = "application-config"
            }
          }
      )
    }
  }

  client.apps().deployments().createOrReplace(klyfftDeployment)
}
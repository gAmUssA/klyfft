package io.confluent.devx.klyfft

import com.fkorotkov.kubernetes.client.DefaultKafkaClient
import com.fkorotkov.kubernetes.newContainer
import com.fkorotkov.kubernetes.newEnvVar
import io.confluent.devx.klyfft.base.BaseDeployment
import io.fabric8.kubernetes.client.Config
import io.fabric8.kubernetes.client.DefaultKubernetesClient

fun main() {
  val config = Config.autoConfigure(null)
  val client = DefaultKubernetesClient(config).inNamespace("default")
  val kafkaClient = DefaultKafkaClient().inNamespace("operator")

  val klyfftDeployment = BaseDeployment("klyfft").apply {
    spec.template.spec.containers = listOf(
        newContainer {
          name = serviceName
          image = "gcr.io/cloud-private-dev/gamussa/klyfft:latest"
          env = listOf(
              newEnvVar {
                name = "JAVA_TOOL_OPTIONS"
                value = "-DLOGLEVEL=INFO"
              },
              newEnvVar {
                name = "SASL_JAAS_CONFIG"
                value = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"test\" password=\"test123\";"
              },
              newEnvVar {
                name = "SASL_MECHANISM"
                value = "PLAIN\"test\" password=\"test123\";"
              },
              newEnvVar {
                name = "SECURITY_PROTOCOL"
                value = "SASL_PLAINTEXT"
              }
          )
        }
    )
  }

  client.apps().deployments().createOrReplace(klyfftDeployment)
}
package io.confluent.devx.klyfft.base

import com.fkorotkov.kubernetes.client.DefaultKafkaClient
import com.fkorotkov.kubernetes.client.DefaultZookeeperClient
import com.fkorotkov.kubernetes.kafka.*
import com.fkorotkov.kubernetes.metadata
import com.fkorotkov.kubernetes.newSecret
import com.fkorotkov.kubernetes.storage.metadata
import com.fkorotkov.kubernetes.storage.newStorageClass
import com.fkorotkov.kubernetes.zookeeper.*
import io.confluent.devx.klyfft.toBase64
import io.fabric8.kubernetes.api.model.storage.StorageClass
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import com.fkorotkov.kubernetes.kafka.newInitContainer as newKafkaInitContainer
import com.fkorotkov.kubernetes.kafka.newStorage as newKafkaStorage
import com.fkorotkov.kubernetes.zookeeper.newInitContainer as newZkInitContainer
import com.fkorotkov.kubernetes.zookeeper.newStorage as newZkStorage

fun main() {
  // Kubernetes API client
  val client = DefaultKubernetesClient().inNamespace("operator")
  // Zookeeper API client
  val zkClient = DefaultZookeeperClient().inNamespace("operator")
  // Kafka API client
  val kafkaClient = DefaultKafkaClient().inNamespace("operator")

  // Zookeeper API keys to interact with Operator
  client.secrets().createOrReplace(newSecret {
    metadata {
      namespace = "operator"
      name = "zookeeper-apikeys"
    }
    type = "Opaque"
    data = mapOf("apikeys.json" to zkApiKey)
  })

  val zone = "us-east4-b"
  client.storage().storageClasses().createOrReplace(createStorageClassFor("zookeeper", zone))
  client.storage().storageClasses().createOrReplace(createStorageClassFor("kafka", zone))

  // Kafka API keys to interact with Operator
  client.secrets().createOrReplace(newSecret {
    metadata {
      namespace = "operator"
      name = "kafka-apikeys"
    }
    type = "Opaque"
    data = mapOf("apikeys.json" to kafkaApiKey,
        "global_sasl_plain_username" to "test".toBase64(),
        "global_sasl_plain_password" to "test123".toBase64())
  })

  val myZkCluster = newZookeeperCluster {
    metadata {
      labels = mapOf("component" to "zookeeper")
      name = "zookeeper"
      namespace = "operator"
    }
    spec {
      replicas = 3
      image = "docker.io/confluentinc/cp-zookeeper-operator:5.3.1.0"
      initContainers = listOf(newZkInitContainer {
        name = "init-container"
        image = "docker.io/confluentinc/cp-init-container-operator:5.3.1.0"
        command = listOf("/bin/sh", "-xc")
        args = listOf("until [ -f /mnt/config/pod/zookeeper/template.jsonnet ]; do echo \"file not found\"; sleep 10s; done; /opt/startup.sh")
      })

      terminationGracePeriodInSecond = 180
      jvmConfig {
        heapSize = "4G"
      }
      podSecurityContext {
        fsGroup = 1001
        runAsUser = 1001
        runAsNonRoot = true
      }

      resources {
        requests {
          cpu = "500m"
          memory = "4Gi"
          storage = listOf(
              newZkStorage {
                name = "data"
                capacity = "10Gi"
              },
              newZkStorage {
                name = "txnlog"
                capacity = "10Gi"
              }
          )
        }
      }
      zones = listOf(zone)
    }
  }

  val newKafkaCluster = newKafkaCluster {
    metadata {
      labels = mapOf("component" to "kafka")
      name = "kafka"
      namespace = "operator"
    }
    spec {
      replicas = 3
      image = "docker.io/confluentinc/cp-server-operator:5.3.1.0"
      initContainers = listOf(newKafkaInitContainer {
        name = "init-container"
        image = "docker.io/confluentinc/cp-init-container-operator:5.3.1.0"
        command = listOf(
            "/bin/sh",
            "-xc"
        )
        args = listOf("until [ -f /mnt/config/pod/kafka/template.jsonnet ]; do echo \"file not found\"; sleep 10s; done; /opt/startup.sh")
      })
      options {
        enterprise = true
      }
      jvmConfig {
        heapSize = "4G"
      }
      podSecurityContext {
        fsGroup = 1001
        runAsUser = 1001
        runAsNonRoot = true
      }
      resources {
        requests {
          cpu = "1"
          memory = "4Gi"
        }
        storage = listOf(newKafkaStorage {
          name = "data0"
          capacity = "10Gi"
        })
      }
      metricReporter {
        bootstrapEndpoint = "kafka:9071"
        publishMs = 30000
        replicationFactor = 3
        internal = false
        tls {
          enabled = false
        }

      }
      configOverrides {
        server = listOf("auto.create.topics.enabled = true")
      }
      zones = listOf(zone)
    }
  }

  zkClient.zookeeperClusters().createOrReplace(myZkCluster)
  kafkaClient.kafkaClusters().createOrReplace(newKafkaCluster)
}

//per Operator convention, storage class is created per component
private fun createStorageClassFor(
    component: String,
    zone: String
): StorageClass {
  return newStorageClass {
    metadata {
      labels = mapOf("component" to component)
      annotations = mapOf("storageclass.beta.kubernetes.io/is-default-class" to "false")
      name = "$component-standard-ssd-$zone"
      namespace = ""
    }
    parameters = mapOf("type" to "pd-ssd")
    provisioner = "kubernetes.io/gce-pd"
    reclaimPolicy = "Delete"
  }
}

//TODO
const val kafkaApiKey = "eyAia2V5cyI6IHsgICJ0ZXN0IjogeyAic2FzbF9tZWNoYW5pc20iOiAiUExBSU4iLCAiaGFzaGVkX3NlY3JldCI6ICJ0ZXN0MTIzIiwgImhhc2hfZnVuY3Rpb24iOiAibm9uZSIsICJsb2dpY2FsX2NsdXN0ZXJfaWQiOiAib3BlcmF0b3IiLCAidXNlcl9pZCI6ICJ0ZXN0In19fQ=="

//TODO
const val zkApiKey = "eyAia2V5cyI6IHsgInRlc3QiOiB7ICJzYXNsX21lY2hhbmlzbSI6ICJQTEFJTiIsICJoYXNoZWRfc2VjcmV0IjogInRlc3QxMjMiLCAiaGFzaF9mdW5jdGlvbiI6ICJub25lIiwgImxvZ2ljYWxfY2x1c3Rlcl9pZCI6ICJvcGVyYXRvciIsICJ1c2VyX2lkIjogInRlc3QiLCAic2VydmljZV9hY2NvdW50IjogZmFsc2V9fX0=" 
package io.confluent.devx.klyfft.base

import com.fkorotkov.kubernetes.*
import com.fkorotkov.kubernetes.apps.metadata
import com.fkorotkov.kubernetes.apps.selector
import com.fkorotkov.kubernetes.apps.spec
import com.fkorotkov.kubernetes.apps.template
import io.fabric8.kubernetes.api.model.apps.Deployment

class BaseDeployment(
    val serviceName: String,
    val defaultReplicas: Int = 1
) : Deployment() {
  init {
    val defaultLabels = mapOf("app" to serviceName)

    metadata {
      name = serviceName
      labels = defaultLabels
    }
    spec {
      replicas = defaultReplicas
      selector {
        matchLabels = defaultLabels
      }
      template {
        metadata {
          labels = defaultLabels
        }
//        spec {
//          affinity {
//            podAntiAffinity {
//              requiredDuringSchedulingIgnoredDuringExecution = listOf(
//                  newPodAffinityTerm {
//                    topologyKey = "kubernetes.io/hostname"
//                    labelSelector {
//                      matchExpressions = listOf(
//                          newLabelSelectorRequirement {
//                            key = "app"
//                            operator = "In"
//                            values = listOf(serviceName)
//                          }
//                      )
//                    }
//                  }
//              )
//            }
//          }
//        }
      }
    }
  }
}

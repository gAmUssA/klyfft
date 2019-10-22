package io.confluent.devx.klyfft.kafka

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka


@Configuration
@EnableKafka
class KafkaConfiguration {

  @Value("\${replication.factor}")
  lateinit var replicationFactor: String

  @Value("\${partitions.num}")
  lateinit var numberOfPartitions: String

  /**
   * Rider Topic
   */
  @Bean
  fun rider() = NewTopic("rider", numberOfPartitions.toInt(), replicationFactor.toShort())

  /**
   * Driver Topic
   */
  @Bean
  fun driver() = NewTopic("driver", numberOfPartitions.toInt(), replicationFactor.toShort())

}

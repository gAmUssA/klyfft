package io.confluent.devx.klyfft.kafka

import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import java.util.*
import java.util.function.Supplier


@Configuration
@EnableKafka
class KafkaConfiguration {

  @Value("\${bootstrap.servers}")
  lateinit var bootstrapServer: String

  @Value("\${sasl.jaas.config}")
  lateinit var jaasConfig: String

  @Value("\${sasl.mechanism}")
  lateinit var saslMechanism: String

  @Value("\${security.protocol}")
  lateinit var securityProtocol: String

  @Value("\${replication.factor}")
  lateinit var replicationFactor: String

  /*@Bean
  fun defaultConfigFromFile(configPath: String): Properties {
    var properties: Properties
    if (configPath.isNotEmpty()) {
      ConfigLoader.loadConfig(configPath)
    } else {

    }e
    r
  }*/
  fun commonConfig(): Map<String, String> {
    return mapOf(
            "sasl.jaas.config" to jaasConfig,
            "sasl.mechanism" to saslMechanism,
            "security.protocol" to securityProtocol
    )
  }

  /**
   * Kafka Producer
   */
  @Bean
  fun producerFactory(): ProducerFactory<String, String> {
    val config = mutableMapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServer,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.canonicalName,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.canonicalName
    )
    config.putAll(commonConfig())
    return DefaultKafkaProducerFactory(config.toMap())
  }

  @Bean
  fun kafkaTemplate(): KafkaTemplate<String, String> {
    return KafkaTemplate(producerFactory())
  }

  @Bean
  @Qualifier("kafka")
  fun kafkaConsumerProperties(): Properties {
    val conf = mutableMapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServer,
            ConsumerConfig.GROUP_ID_CONFIG to "group-klyfft",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java
    )
    conf.putAll(commonConfig())
    val p = Properties()
    p.putAll(conf)
    return p
  }

  /**
   * Kafka Consumer
   */
  @Bean
  fun kafkaConsumerSupplier(@Qualifier("kafka") config: Properties): Supplier<KafkaConsumer<String, String>> =
          Supplier { KafkaConsumer<String, String>(config) }


  /**
   * Configuring AdminClient
   */
  @Bean
  fun admin(): KafkaAdmin {
    val configs = mutableMapOf(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServer
    )
    configs.putAll(commonConfig())
    return KafkaAdmin(configs.toMap())
  }

  /**
   * Rider Topic
   */
  @Bean
  fun rider(): NewTopic {
    return NewTopic("rider", 1, replicationFactor.toShort())
  }

  /**
   * Driver Topic
   */
  @Bean
  fun driver(): NewTopic {
    return NewTopic("driver", 1, replicationFactor.toShort())
  }
}

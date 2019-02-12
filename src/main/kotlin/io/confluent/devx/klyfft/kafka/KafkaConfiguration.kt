package io.confluent.devx.klyfft.kafka

import io.confluent.devx.klyfft.domain.Message
import io.confluent.devx.klyfft.domain.MessageSerde
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
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import java.util.*
import java.util.function.Supplier

@Configuration
@EnableKafka
class KafkaConfiguration {
  
  @Value("\${kafka.bootstrapServers}")
  lateinit var bootstrapServer:String

  /**
   * Kafka Producer
   */
  @Bean
  fun producerFactory(): ProducerFactory<String, Message> {
    val config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServer,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.canonicalName,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to MessageSerde::class.java.canonicalName
    )
    return DefaultKafkaProducerFactory(config)
  }

  @Bean
  fun kafkaTemplate(): KafkaTemplate<String, Message> {
    return KafkaTemplate(producerFactory())
  }

  @Bean
  @Qualifier("kafka")
  fun kafkaConsumerProperties(): Properties {
    val conf = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServer,
            ConsumerConfig.GROUP_ID_CONFIG to "group-klyfft",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to MessageSerde::class.java
    )
    val p = Properties()
    p.putAll(conf)
    return p
  }

  /**
   * Kafka Consumer
   */
  @Bean
  fun kafkaConsumerSupplier(@Qualifier("kafka") config: Properties): Supplier<KafkaConsumer<String, Message>> =
          Supplier { KafkaConsumer<String, Message>(config) }
}

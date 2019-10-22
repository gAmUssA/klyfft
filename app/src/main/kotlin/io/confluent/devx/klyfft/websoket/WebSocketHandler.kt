package io.confluent.devx.klyfft.websoket

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.time.Duration

@Component
class WebSocketHandler(@Autowired val mapper: ObjectMapper,
                       @Autowired val kafkaProducer: KafkaTemplate<String, String>,
                       @Autowired val kafkaConsumerFactory: ConsumerFactory<String, String>) : TextWebSocketHandler() {

  private fun WebSocketSession.createKafkaConsumer(topic: String): KafkaConsumer<*, *> {
    val attributes = this.attributes
    return attributes.computeIfAbsent("consumer") {
      kafkaConsumerFactory.createConsumer().apply {
        subscribe(listOf(topic))
      }
    } as KafkaConsumer<*, *>
  }


  override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
    val consumer = session.attributes["consumer"]
    if (consumer is KafkaConsumer<*, *>) with(consumer) {
      unsubscribe()
      close()
    }
  }

  override fun handleTextMessage(session: WebSocketSession, textMessage: TextMessage) {

    try {
      val driver = session.uri!!.path.contains("driver-ws")

      val sink = if (driver) "driver" else "rider"
      val source = if (driver) "rider" else "driver"

      val kafkaConsumer = session.createKafkaConsumer(source)

      val payload = textMessage.payload
      val message = mapper.readTree(payload)
      //val timestamp: String = message.get("timestamp").asText()
      val key: String = if ("driver" == sink) message.get("driver").asText() else message.get("rider").asText()

      kafkaProducer.send(ProducerRecord<String, String>(sink, key, payload))

      kafkaConsumer.poll(Duration.ofMillis(100))
              .map {
                it.value() as String
              }
              .forEach {
                session.sendMessage(TextMessage(it))
              }
    } catch (e: Exception) {
      e.printStackTrace()
      throw e
    }
  }
}



package io.confluent.devx.klyfft.websoket

import com.fasterxml.jackson.databind.ObjectMapper
import io.confluent.devx.klyfft.domain.Message
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.time.Duration
import java.util.function.Supplier

@Component
class WebSocketHandler(@Autowired val mapper: ObjectMapper,
                       @Autowired val kafkaProducer: KafkaTemplate<String, Message>,
                       @Autowired val kafkaConsumerSupplier: Supplier<KafkaConsumer<String, Message>>) : TextWebSocketHandler() {

  private fun WebSocketSession.createKafkaConsumer(topic: String): KafkaConsumer<*, *> {
    val attributes = this.attributes
    return attributes.computeIfAbsent("consumer") {
      kafkaConsumerSupplier.get().apply {
        subscribe(listOf(topic))
      }
    } as KafkaConsumer<*, *>
  }

  override fun handleTextMessage(session: WebSocketSession, textMessage: TextMessage) {

    try {
      val driver = session.uri!!.path.contains("driver-ws")
      
      val sink = if (driver) "driver" else "rider"
      val source = if (driver) "rider" else "driver"

      val kafkaConsumer = session.createKafkaConsumer(source)

      val payload = textMessage.payload
      val message = mapper.readValue(payload, Message::class.java)

      kafkaProducer.send(ProducerRecord<String, Message>(sink, message.timestamp.toString(), message))

      kafkaConsumer.poll(Duration.ofMillis(100))
              .map {
                mapper.writeValueAsString(it.value())
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



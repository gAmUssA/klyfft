package io.confluent.devx.klyfft.domain

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer

class MessageSerde : Serializer<Message>, Deserializer<Message> {
  override fun serialize(topic: String?, data: Message?): ByteArray {
    val mapper = jacksonObjectMapper()
    return mapper.writeValueAsBytes(data)
  }

  override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {

  }

  override fun deserialize(topic: String, data: ByteArray): Message {
    val mapper = jacksonObjectMapper()
    return mapper.readValue(data)
  }

  override fun close() {

  }

}

//TODO: move to unit test
fun main() {
  val dataFooJsonFile = MessageSerde::class.java.getResource("/foo.json").readText()
  val instance = MessageSerde()

  val deserialize = instance.deserialize("test", dataFooJsonFile.toByteArray())
  println(deserialize)

  val serialize = instance.serialize("test", deserialize)
  println(serialize)
}
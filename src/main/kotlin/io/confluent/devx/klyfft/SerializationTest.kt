package io.confluent.devx.klyfft

import io.confluent.devx.klyfft.domain.MessageSerde
import org.apache.avro.io.DatumReader
import org.apache.avro.io.DecoderFactory
import org.apache.avro.specific.SpecificDatumReader
import java.util.*

fun main() {
  val dataFooJsonFile = MessageSerde::class.java.getResource("/foo.json").readText()

  val reader:DatumReader<MessageAvro> = SpecificDatumReader(MessageAvro::class.java)
  val jsonDecoder = DecoderFactory.get().jsonDecoder(MessageAvro.getClassSchema(), dataFooJsonFile)
  val read = reader.read(MessageAvro(), jsonDecoder)
  println(read.toString())

}
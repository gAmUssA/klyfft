package io.confluent.devx.klyfft

import java.util.*

fun String.toBase64(): String {
  return Base64.getEncoder().encodeToString(this.toByteArray()).toString()
}
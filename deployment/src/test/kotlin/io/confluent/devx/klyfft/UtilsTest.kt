package io.confluent.devx.klyfft

import org.junit.Assert.*
import org.junit.Test

class UtilsTest {
  @Test
  fun base64() {
    assertEquals("YWRtaW4=", "admin".toBase64())
  }
}
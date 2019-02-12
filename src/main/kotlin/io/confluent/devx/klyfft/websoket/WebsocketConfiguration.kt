package io.confluent.devx.klyfft.websoket

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebsocketConfiguration(@Autowired val webSocketHandler: WebSocketHandler) : WebSocketConfigurer {
  override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {

    registry.addHandler(webSocketHandler, "rider-ws")
    registry.addHandler(webSocketHandler, "driver-ws")
  }

}
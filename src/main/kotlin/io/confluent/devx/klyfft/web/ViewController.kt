package io.confluent.devx.klyfft.web

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import java.net.InetAddress

@Controller
class ViewController {

  @Value("\${access.token}")
  lateinit var accessToken: String

  @LocalServerPort
  lateinit var serverPort: String

  //TODO
  var server: String = "localhost"

  @RequestMapping("/rider")
  fun riderU(model: Model): String {
    model.addAttribute("accesstoken", accessToken)
    model.addAttribute("wsurl", "ws://$server:$serverPort/rider-ws")
    return "rider"
  }

  @RequestMapping("/driver")
  fun driverUi(model: Model): String {
    model.addAttribute("accesstoken", accessToken)
    model.addAttribute("wsurl", "ws://$server:$serverPort/driver-ws")
    return "driver"
  }

}
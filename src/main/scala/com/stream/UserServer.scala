package com.stream

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.stream.Service.UserService
import com.typesafe.config.ConfigFactory

import scala.io.StdIn

object UserServer {

  object Config {
    val config = ConfigFactory.defaultApplication()
    val port = config.getInt("server.port")
    val host = config.getString("server.host")
  }

  def main(args: Array[String]) {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val userService = new UserService()
    val bindingFuture = Http().bindAndHandle(userService.websocketRoute, Config.host,Config.port)
    println(s"Server online at ${Config.host}:${Config.port}")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}

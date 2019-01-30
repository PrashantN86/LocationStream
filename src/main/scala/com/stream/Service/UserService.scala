package com.stream.Service

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.{ActorMaterializer, FlowShape, OverflowStrategy}
import com.stream.Actor.{UserManager, _}
import com.stream.domain.Location

class UserService(implicit val actorSystem : ActorSystem, implicit  val actorMaterializer: ActorMaterializer) extends Directives {

  import spray.json._
  import DefaultJsonProtocol._
  implicit val UserFormat = jsonFormat1(User)
  implicit val LocationFormat = jsonFormat2(Location)
  implicit val UserWithLocationFormat = jsonFormat2(UserWithLocation)

  val websocketRoute = (get & parameter("name")){ UserName =>
    handleWebSocketMessages(flow(UserName))
  }

  val userManager = actorSystem.actorOf(Props(new UserManager()))
  val UserActorSource = Source.actorRef[UserEvent](5,OverflowStrategy.fail)
  def flow(name: String): Flow[Message, Message, Any] = Flow.fromGraph(GraphDSL.create(UserActorSource){ implicit builder => UserActor =>
    import GraphDSL.Implicits._

    val materialization = builder.materializedValue.map(UserActorRef => UserJoined(User(name),UserActorRef))
    val merge = builder.add(Merge[UserEvent](2))

    val messagesToUserEventsFlow = builder.add(Flow[Message].collect {

      case TextMessage.Strict(jsContent) => {
        UserMoveRequest(name,LocationFormat.read(jsContent.parseJson))
      }
    })

    val UserEventsToMessagesFlow = builder.add(Flow[UserEvent].map {

      case UsersChanged(users) => {
        TextMessage(users.toList.toJson.toString)
      }

      case UserMoveRequest(user,location) => {
        TextMessage(UserWithLocation(User(user),location).toJson.toString)
      }

    })

    val UserManagerSink = Sink.actorRef[UserEvent](userManager,UserLeft(name))

    materialization ~> merge ~> UserManagerSink
    messagesToUserEventsFlow ~> merge

    UserActor ~> UserEventsToMessagesFlow

    FlowShape(messagesToUserEventsFlow.in,UserEventsToMessagesFlow.out)
  })
}

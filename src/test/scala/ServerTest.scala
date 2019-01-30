import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import com.stream.Service.UserService
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

class ServerTest extends FunSuite with Matchers with ScalatestRouteTest with BeforeAndAfterAll {

  override def afterAll(): Unit = cleanUp()

  test("should create UserService") {
    new UserService()
  }

  test("should be able to connect to the UserService websocket") {
    assertWebsocket("John") { wsClient =>
        isWebSocketUpgrade shouldEqual true
      }
  }

  test("should register user") {
    assertWebsocket("John"){ wsClient =>
      wsClient.expectMessage("[{\"name\":\"John\"}]")
    }
  }

  test("should register multiple users") {
    val userService = new UserService()
    val client1 = WSProbe()
    val client2 = WSProbe()

    WS(s"/?name=user1", client1.flow) ~> userService.websocketRoute ~> check {
      client1.expectMessage("[{\"name\":\"user1\"}]")
    }
    WS(s"/?name=user2", client2.flow) ~> userService.websocketRoute ~> check {
      client2.expectMessage("[{\"name\":\"user1\"},{\"name\":\"user2\"}]")
    }
  }

  test("should be able to send Location") {
    assertWebsocket("John"){ wsClient =>
      wsClient.expectMessage("[{\"name\":\"John\"}]")
      wsClient.sendMessage("{\"lat\": 1.2,\"lng\": 1.3}")
      wsClient.expectMessage("{\"location\":{\"lat\":1.2,\"lng\":1.3},\"user\":{\"name\":\"John\"}}")
    }
  }

  def assertWebsocket(name: String)(assertions:(WSProbe) => Unit) : Unit = {
    val userService = new UserService()
    val wsClient = WSProbe()
    WS(s"/?name=$name", wsClient.flow) ~> userService.websocketRoute ~> check(assertions(wsClient))
  }
}

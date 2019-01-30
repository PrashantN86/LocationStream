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
    val johnClient = WSProbe()
    val andrewClient = WSProbe()

    WS(s"/?name=John", johnClient.flow) ~> userService.websocketRoute ~> check {
      johnClient.expectMessage("[{\"name\":\"John\"}]")
    }
    WS(s"/?name=Andrew", andrewClient.flow) ~> userService.websocketRoute ~> check {
      andrewClient.expectMessage("[{\"name\":\"John\"},{\"name\":\"Andrew\"}]")
    }
  }


  def assertWebsocket(name: String)(assertions:(WSProbe) => Unit) : Unit = {
    val userService = new UserService()
    val wsClient = WSProbe()
    WS(s"/?name=$name", wsClient.flow) ~> userService.websocketRoute ~> check(assertions(wsClient))
  }
}

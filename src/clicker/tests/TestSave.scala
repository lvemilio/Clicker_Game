package clicker.tests

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import clicker.{ClickGold, GameState, SaveGame, Update}
import clicker.database.DatabaseActor
import clicker.model.GameActor
import org.scalatest._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.Json

import scala.concurrent.duration._

class TestSave extends TestKit(ActorSystem("TestSave"))
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "A Clicker Game" must {
    "save and load properly" in {
      val database = system.actorOf(Props(classOf[DatabaseActor], "test"))
      val gameActor = system.actorOf(Props(classOf[GameActor], "username", database))

      for (i <- 1 to 20) {
        gameActor ! ClickGold
      }
      expectNoMessage(50.millis)
      gameActor ! Update

      val gs: GameState = expectMsgType[GameState](300.millis)
      val gameStateJSON: String = gs.gameState
      val gameState = Json.parse(gameStateJSON)
      val currentGold:Double = (gameState \ "gold" ).as[Double]
      assert(currentGold == 20)

      gameActor ! SaveGame("emilio",gameStateJSON)



    }
  }


}

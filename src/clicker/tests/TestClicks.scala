package clicker.tests

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import clicker._
import clicker.database.DatabaseActor
import clicker.model.GameActor
import org.scalatest.BeforeAndAfterAll
import play.api.libs.json.Json
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import org.scalatest.wordspec.AnyWordSpecLike


class TestClicks extends TestKit(ActorSystem("TestClicks"))
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "A Clicker Game" must {
    "react to user clicks with shovels appropriately" in {

      val database = system.actorOf(Props(classOf[DatabaseActor], "test"))
      val gameActor = system.actorOf(Props(classOf[GameActor], "username", database))
      ///////////////////////////////Base test, ClickGold works
      //////////////////////////////
      gameActor ! ClickGold
      gameActor ! ClickGold


      expectNoMessage(50.millis)
      gameActor ! Update


      val gs: GameState = expectMsgType[GameState](300.millis)
      val gameStateJSON: String = gs.gameState
      val gameState = Json.parse(gameStateJSON)
      val currentGold:Double = (gameState \ "gold" ).as[Double]
      assert(currentGold == 2)



      /////////////////////////////////////////////It is possible to buy equipment, gold is subtracted
      /////////////////////////////////////////////
      gameActor ! ClickGold
      gameActor ! ClickGold
      gameActor ! ClickGold
      gameActor ! ClickGold
      gameActor ! ClickGold
      gameActor ! ClickGold
      gameActor ! ClickGold
      gameActor ! ClickGold
      gameActor ! ClickGold


      expectNoMessage(50.millis)
      gameActor ! BuyEquipment("shovel")
      expectNoMessage(50.millis)
      gameActor ! Update


      val gs1: GameState = expectMsgType[GameState](300.millis)
      val gameState1JSON: String = gs1.gameState
      val gameState1 = Json.parse(gameState1JSON)
      val currentGold1:Double = (gameState1 \ "gold" ).as[Double]
      assert(currentGold1 == 1)
      //////////////////////////////////////Clicking gold with shovel generates more gold
      //////////////////////////////////////
      gameActor ! ClickGold
      gameActor ! ClickGold

      expectNoMessage(50.millis)

      gameActor ! Update

      val gs2: GameState = expectMsgType[GameState](300.millis)
      val gameState2JSON: String = gs2.gameState
      val gameState2 = Json.parse(gameState2JSON)
      val currentGold2:Double = (gameState2 \ "gold" ).as[Double]
      assert(currentGold2 == 5)
      ////////////////////////////////
      //////////////////////////////// More shovels increase cost accordingly
      gameActor ! ClickGold
      gameActor ! ClickGold
      gameActor ! ClickGold

      expectNoMessage(50.millis)

      gameActor ! BuyEquipment("shovel")

      expectNoMessage(50.millis)

      gameActor ! Update
      val gs3: GameState = expectMsgType[GameState](300.millis)
      val gameState3JSON: String = gs3.gameState
      val gameState3 = Json.parse(gameState3JSON)
      val currentGold3:Double = (gameState3 \ "gold" ).as[Double]
      assert(currentGold3 == 0.5)
      ////////////////////////////////
      //////////////////////////////// More shovels generate even more gold
      gameActor ! ClickGold
      gameActor ! ClickGold

      gameActor ! Update

      val gs4: GameState = expectMsgType[GameState](300.millis)
      val gameState4JSON: String = gs4.gameState
      val gameState4 = Json.parse(gameState4JSON)
      val currentGold4:Double = (gameState4 \ "gold" ).as[Double]
      assert(currentGold4 == 6.5)

    }
  }

}

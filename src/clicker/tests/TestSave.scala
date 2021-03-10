package clicker.tests

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import clicker.{BuyEquipment, ClickGold, GameState, Save, SaveGame, Update}
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
      expectNoMessage(100.millis)
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
      expectNoMessage(300.millis)

      gameActor ! Save ////////////////////////////////////Saving the game

      expectNoMessage(300.millis)
      val gameActor2 = system.actorOf(Props(classOf[GameActor], "username", database))
      ////////////////////////////////////Another actor with the same username loads the game, the data is received and replaced
      expectNoMessage(300.millis)
      gameActor2 ! Update

      val gs2: GameState = expectMsgType[GameState](300.millis)
      val gameStateJSON2: String = gs2.gameState
      val gameState2 = Json.parse(gameStateJSON2)
      val currentGold2:Double = (gameState2 \ "gold" ).as[Double]
      assert(currentGold2 == 20)

      for (i <- 1 until 1000){
        gameActor2 ! ClickGold
        gameActor2 ! BuyEquipment("shovel")
        gameActor2 ! BuyEquipment("excavator")
        gameActor2 ! BuyEquipment("mine")
      }

      expectNoMessage(300.millis)

      gameActor2 ! Update

      val gs3: GameState = expectMsgType[GameState](300.millis)
      val gameState3 = Json.parse(gs3.gameState)
      val currentGold3:Double = (gameState3 \ "gold" ).as[Double]
      val currentShovels:Double = (gameState3 \ "equipment" \ "shovel" \ "numberOwned").as[Double]
      val currentShovelCost:Double = (gameState3 \ "equipment" \ "shovel" \ "cost").as[Double]
      val currentExc:Double = (gameState3 \ "equipment" \ "excavator" \ "numberOwned").as[Double]
      val currentExcCost:Double = (gameState3 \ "equipment" \ "excavator" \ "cost").as[Double]
      val currentMines:Double = (gameState3 \ "equipment" \ "mine" \ "numberOwned").as[Double]
      val currentMineCost:Double = (gameState3 \ "equipment" \ "mine" \ "cost").as[Double]
      println(currentGold3,currentShovels,currentShovelCost,currentExc,currentExcCost,currentMines,currentMineCost)
      gameActor2 ! Save

      expectNoMessage(300.millis)

      val gameActor3 = system.actorOf(Props(classOf[GameActor], "username", database))

      expectNoMessage(300.millis)
      gameActor3 ! Update

      val gs4: GameState = expectMsgType[GameState](300.millis)
      val gameState4 = Json.parse(gs4.gameState)
      val currentGold4:Double = (gameState4 \ "gold" ).as[Double]
      val currentShovels2:Double = (gameState4 \ "equipment" \ "shovel" \ "numberOwned").as[Double]
      val currentShovelCost2:Double = (gameState4 \ "equipment" \ "shovel" \ "cost").as[Double]
      val currentExc2:Double = (gameState4 \ "equipment" \ "excavator" \ "numberOwned").as[Double]
      val currentExcCost2:Double = (gameState4 \ "equipment" \ "excavator" \ "cost").as[Double]
      val currentMines2:Double = (gameState4 \ "equipment" \ "mine" \ "numberOwned").as[Double]
      val currentMineCost2:Double = (gameState4 \ "equipment" \ "mine" \ "cost").as[Double]
      println(currentGold4,currentShovels2,currentShovelCost2,currentExc2,currentExcCost2,currentMines2,currentMineCost2) //////Correct data set is printed

      assert(currentGold4 >4500)
    }
  }


}

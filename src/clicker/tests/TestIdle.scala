package clicker.tests

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import clicker.GameState
import clicker.database.DatabaseActor
import clicker.model.GameActor
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.BeforeAndAfterAll
import play.api.libs.json.Json
import clicker.{BuyEquipment, ClickGold, GameState, Update}
import clicker.model.GameModel

import scala.concurrent.duration._

class TestIdle extends TestKit(ActorSystem("TestIdle"))
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "A Clicker Game" must {
    "earn the correct idle income" in {
      val database = system.actorOf(Props(classOf[DatabaseActor], "test"))
      val gameActor = system.actorOf(Props(classOf[GameActor], "username", database))
      ///////////////////////////////Recieved properly formatted JSON
      ///////////////////////////////
      gameActor ! Update
      val gs: GameState = expectMsgType[GameState](300.millis)
      val gameState = Json.parse(gs.gameState)
      val currentGold:Double = (gameState \ "gold" ).as[Double]
      assert(currentGold == 0)
      ///////////////////////////////Shovel test
      ///////////////////////////////
      for (i <- 0 to 40){
        gameActor ! ClickGold
        gameActor ! BuyEquipment("shovel")///////////This suite also tests whether the user can buy items if they don't have enough gold
      }
      expectNoMessage(50.millis)
      gameActor ! Update
      val gs1: GameState = expectMsgType[GameState](300.millis)
      val gameState1 = Json.parse(gs1.gameState)
      val currentGold1:Double = (gameState1 \ "gold" ).as[Double]
      val currentShovels:Double = (gameState1 \ "equipment" \ "shovel" \ "numberOwned").as[Double]
      val currentShovelCost:Double = (gameState1 \ "equipment" \ "shovel" \ "cost").as[Double]
      assert(currentGold1 == 15.013680112120525 && currentShovelCost == 19.799315994393975 && currentShovels == 14)
      ////////////////////////////////////////Excavator Test
      ////////////////////////////////////////
      for (i <- 0 to 30){
        gameActor ! ClickGold
        gameActor ! BuyEquipment("excavator")
      }
      expectNoMessage(2000.millis)
      gameActor ! Update

      val gs2: GameState = expectMsgType[GameState](300.millis)
      val gameState2 = Json.parse(gs2.gameState)
      val currentGold2:Double = (gameState2 \ "gold" ).as[Double]
      val currentExcavators:Double = (gameState2 \ "equipment" \ "excavator" \ "numberOwned").as[Double]
      val currentExcCost:Double = (gameState2 \ "equipment" \ "excavator" \ "cost").as[Double]
      assert(currentGold2 > 220 && currentExcavators == 2, currentExcCost == 242)
      println(currentGold2, currentExcavators, currentExcCost) //////If you look at the gold amount after the last click and compare it to currentGold2
                                                                //// it can be seen that the difference is exatcly 20 (since there were 2 excavators and 2 seconds passed)

      /////////////////////////////////////Gold mine test
      /////////////////////////////////////

      for (i <- 0 to 40){
        gameActor ! ClickGold
        gameActor ! BuyEquipment("mine")
      }
      expectNoMessage(2000.millis)
      gameActor ! Update
      val gs3: GameState = expectMsgType[GameState](300.millis)
      val gameState3 = Json.parse(gs3.gameState)
      val currentGold3:Double = (gameState3 \ "gold" ).as[Double]
      val currentMines:Double = (gameState3 \ "equipment" \ "mine" \ "numberOwned").as[Double]
      val currentMineCost:Double = (gameState3 \ "equipment" \ "mine" \ "cost").as[Double]
      assert(currentGold3 > 480 && currentMines == 1, currentMineCost == 1100)
      println(currentGold3,currentMines,currentMineCost) //Here one can also tell that the Idle gold income is generating properly from comparing the print statements
    }
  }


}

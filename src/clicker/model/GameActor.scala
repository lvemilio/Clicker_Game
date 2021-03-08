package clicker.model

import akka.actor.{Actor, ActorRef}
import clicker._
import play.api.libs.json.Json



class GameActor(username: String, database: ActorRef) extends Actor {
  val currentGame:GameModel = new GameModel
  database ! StartedGame(username)

  override def receive: Receive = {
    case ClickGold =>
      currentGame.gold += 1 + currentGame.shovels + (currentGame.excavators * 5)
      println("Gold clicked, current is: " + currentGame.gold)


    case received:BuyEquipment =>
      if (received.equipmentId == "shovel") {
        if (currentGame.gold >= currentGame.shovelCost) {
          currentGame.shovels += 1
          currentGame.gold -= currentGame.shovelCost
          currentGame.shovelCost += currentGame.shovelCost * 0.05
          println("Shovel bought, cost:" + currentGame.shovelCost )
        }
      }
      else if (received.equipmentId == "excavator"){
        if (currentGame.gold >= currentGame.excavatorCost){
          currentGame.excavators+=1
          currentGame.gold -= currentGame.excavatorCost
          currentGame.excavatorCost+= currentGame.excavatorCost * 0.10
          println("Excavator bought, cost:" + currentGame.excavatorCost )
          }
        }
      else if(received.equipmentId == "mine"){
        if (currentGame.gold >= currentGame.goldMineCost){
          currentGame.goldMines +=1
          currentGame.gold -= currentGame.goldMineCost
          currentGame.goldMineCost+= currentGame.goldMineCost * 0.10
          println("Gold mine bought, cost:" + currentGame.goldMineCost )
        }
      }



    case Update =>
      val timeSinceLUpdate:Long = (System.nanoTime() - currentGame.lastUpdateTime)/1000000000
      println("Time since last update:" + timeSinceLUpdate)
      currentGame.lastUpdateTime = System.nanoTime()
      val excavatorIdle:Double = timeSinceLUpdate * 10
      val goldMineIdle:Double = timeSinceLUpdate * 100
      currentGame.gold+= (excavatorIdle * currentGame.excavators) + (goldMineIdle*currentGame.goldMines)
      val gameState: String = currentGame.getGameState()
      sender() ! GameState(gameState)

    case Save=>
      val gameState: String = currentGame.getGameState()
      database ! SaveGame(username,gameState)


    case received: GameState =>
      val gameStateString:String = received.gameState
      val gameState = Json.parse(gameStateString)
      val lastUpdate:Long = (gameState \ "lastUpdateTime").as[Long]
      val currentGold:Double = (gameState \ "gold" ).as[Double]
      val currentShovels:Double = (gameState \ "equipment" \ "shovel" \ "numberOwned").as[Double]
      val currentShovelCost:Double = (gameState \ "equipment" \ "shovel" \ "cost").as[Double]
      val currentExcavators:Double = (gameState \ "equipment" \ "excavator" \ "numberOwned").as[Double]
      val currentExcCost:Double = (gameState \ "equipment" \ "excavator" \ "cost").as[Double]
      val currentMines:Double = (gameState \ "equipment" \ "mine" \ "numberOwned").as[Double]
      val currentMineCost:Double = (gameState \ "equipment" \ "mine" \ "cost").as[Double]

      currentGame.lastUpdateTime = lastUpdate
      currentGame.gold = currentGold
      currentGame.shovels = currentShovels
      currentGame.shovelCost = currentShovelCost
      currentGame.excavators = currentExcavators
      currentGame.excavatorCost = currentExcCost
      currentGame.goldMines = currentMines
      currentGame.goldMineCost = currentMineCost
  }
}


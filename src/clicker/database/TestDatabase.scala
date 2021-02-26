package clicker.database

import play.api.libs.json.{JsValue, Json}

import scala.collection.mutable

class TestDatabase extends Database {
  var players: mutable.Map[String, String] = mutable.Map()

  override def playerExists(username: String): Boolean = {
    if (players.contains(username)) {
      true
    } else {
      println("Player " + username + " doesn't exist")
      false
    }
  }

  override def createPlayer(username: String): Unit = {
    val lastUpdateTime: Long = System.nanoTime()
    var newPlayer: String = "{\"username\": \"" + username + "\",\"gold\":0, \"lastUpdateTime\":" + lastUpdateTime + ",\"equipment\":{\"shovel\": {\"id\": \"shovel\", \"name\":\"Shovel\", \"numberOwned\": 0, \"cost\": 10}," +
      "\"excavator\": {\"id\": \"excavator\", \"name\":\"Excavator\", \"numberOwned\": 0, \"cost\": 200}," +
      "\"mine\": {\"id\": \"mine\", \"name\":\"Gold Mine\", \"numberOwned\": 0, \"cost\": 1000}}}"
    players += (username -> newPlayer)
    println("Created player: " + players)
    val parsed: JsValue = Json.parse(players(username))
    val message: String = parsed("lastUpdateTime").toString
    println("PlayerExists: " + message)
  }

  override def saveGameState(username: String, gameState: String): Unit = {
    println("Saving game state")
    println(gameState)
    players(username) = gameState
    println(players)
  }

  override def loadGameState(username: String): String = {
    println("Loading GameState")
    println("LOADGAME: " + players)
    players(username)
  }
}
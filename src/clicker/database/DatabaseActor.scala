package clicker.database

import akka.actor.Actor
import clicker.{GameState, SaveGame, StartedGame}
import clicker.database.MySQLDatabase
/***
  * @param dbType Indicates the type of database to be used. Use "mySQL" to connect to a MySQL server, or "test" to
  *               use data structures in a new class that extends the Database trait.
  */
class DatabaseActor(dbType: String) extends Actor {

  val database: Database = dbType match {
    case "mySQL" => new MySQLDatabase()
    case "test" => new TestDatabase()
  }

  override def receive: Receive = {
    case saving: SaveGame =>
      database.saveGameState(saving.username,saving.gameState)

    case startGame: StartedGame =>
      if (database.playerExists(startGame.username)){
        val gameState = database.loadGameState(startGame.username)
        sender() ! GameState(gameState)
      }
      else{
        database.createPlayer(startGame.username)
      }
  }

}

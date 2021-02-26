package clicker.model

import akka.actor.{Actor, ActorRef}
import clicker.ClickGold

class GameActor(username: String, database: ActorRef) extends Actor {
  var gold: Double = 0
  // TODO add other variables to track equipment and costs

  override def receive: Receive = {
    case ClickGold =>
    // TODO Actually add gold from all equipment
     println("Gold clicked, current is: " + gold)

    // TODO rest of cases
  }

}

package clicker.server

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import clicker.database.DatabaseActor
import clicker.model.GameActor
import clicker.{ClickGold, SaveGames, UpdateGames}
import com.corundumstudio.socketio.listener.{DataListener, DisconnectListener}
import com.corundumstudio.socketio.{AckRequest, Configuration, SocketIOClient, SocketIOServer}

/** *
  * @param database      Reference to the database actor
  * @param configuration Custom configuration of the game (Used in Bonus Objective. Pass empty string before bonus)
  */
class ClickerServer(val database: ActorRef, configuration: String) extends Actor {

  var actorRefToSocket: Map[ActorRef, SocketIOClient] = Map()
  var socketToActorRef: Map[SocketIOClient, ActorRef] = Map()

  val config: Configuration = new Configuration {
    setHostname("localhost")
    setPort(8080)
  }
  val server: SocketIOServer = new SocketIOServer(config)

  server.addDisconnectListener(new DisconnectionListener(this))
  server.addEventListener("register", classOf[String], new RegisterListener(this))
  server.addEventListener("clickGold", classOf[Nothing], new GoldClicked(this))
  server.addEventListener("buy", classOf[String], new BuyEquipmentClicked(this))
  server.start()


  override def receive: Receive = {
    // These are the cases server has to handle
    case SaveGames  =>
      // TODO actual processing
      // println("Saving")

    case UpdateGames =>
      // TODO actual processing
      // println("Updating")

      // TODO more cases
  }

  class RegisterListener(server: ClickerServer) extends DataListener[String]{
    override def onData(socket: SocketIOClient, username: String, ackRequest: AckRequest): Unit = {
      println(username + " registered to the game with socket " + socket)
      val system = ActorSystem("ClickerSystem")
      val newActor: ActorRef = system.actorOf(Props(classOf[GameActor], username, database))

      server.socketToActorRef += (socket -> newActor)
      server.actorRefToSocket += (newActor -> socket)
    }
  }

  class GoldClicked(server: ClickerServer) extends DataListener[Nothing]{
    override def onData(client: SocketIOClient, data: Nothing, ackSender: AckRequest): Unit = {
      println(client + " clicked gold")
      val actorReference: ActorRef = server.socketToActorRef(client)
      actorReference ! ClickGold
    }
  }

  class BuyEquipmentClicked(server: ClickerServer) extends DataListener[String]{
    override def onData(client: SocketIOClient, data: String, ackSender: AckRequest): Unit = {
      // TODO process buying equipment message and perform necessary actions
    }
  }


  class DisconnectionListener(server: ClickerServer) extends DisconnectListener {
    override def onDisconnect(socket: SocketIOClient): Unit = {
      if(server.socketToActorRef.contains(socket)){
        server.socketToActorRef(socket) ! PoisonPill
        val username = server.socketToActorRef(socket)
        server.socketToActorRef -= socket
        server.actorRefToSocket -= username
        println(username + " Disconnected")
      }
    }
  }

  // Comment in server.stop() to stop your web socket server when the actor system shuts down. This will free
  // the port and allow to to test again immediately. Note that this doesn't work if you stop your server through
  // IntelliJ. If you use IntelliJ's stop button you will have to wait for the port to be freed before restarting
  // your server. By using the TestServer test suite and this method to stop the server you can avoid having to
  // wait before restarting while testing
  override def postStop(): Unit = {
    println("stopping server")
//    server.stop()
  }

}


object ClickerServer {

  def main(args: Array[String]): Unit = {
    val actorSystem = ActorSystem()

    import actorSystem.dispatcher
    import scala.concurrent.duration._

//    val db = actorSystem.actorOf(Props(classOf[DatabaseActor], "mySQL"))
    val db = actorSystem.actorOf(Props(classOf[DatabaseActor], "test"))
    val server = actorSystem.actorOf(Props(classOf[ClickerServer], db, ""))

    actorSystem.scheduler.schedule(0.milliseconds, 100.milliseconds, server, UpdateGames)
    actorSystem.scheduler.schedule(0.milliseconds, 1000.milliseconds, server, SaveGames)
  }

}

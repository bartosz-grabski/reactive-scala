package myActorTest

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.TestProbe
import scala.concurrent.duration._
import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import akka.event.LoggingReceive
import akka.event.Logging

class Toggle extends Actor {
  
  def happy: Receive = LoggingReceive {
    case "How are you?" =>
      sender ! "happy"
      context become sad
      
    case "Done" =>
      sender ! "Done"
     context.stop(self)
  }
  
  def sad: Receive = LoggingReceive {
    case "How are you?" =>
      sender ! "sad"
      context become happy
      
    case "Done" =>
       sender ! "Done"
   
  }
  def receive = happy
}
class ToggleMain extends Actor {
 
  def startToggle = {
	  val toggle = context.actorOf(Props[Toggle], "toggle")
 
	  toggle ! "How are you?"
      toggle ! "How are you?"
      toggle ! "How are you?"
      toggle ! "Done"
  }
  
  def receive = LoggingReceive{
    case "Start"=>
      startToggle
    case "Done" =>
      context.system.shutdown
    case msg: String =>
      println(s" received: $msg" )
     
  }
}
object ToggleMain {
  
  val system = ActorSystem()
  
  val log = Logging(system, ToggleMain.getClass().getName())
  
  def main(args: Array[String]): Unit = run()
  
  def run() = {
    log.debug("Starting toggle example.")
    val toggleMain = system.actorOf(Props[ToggleMain], "toggleMain")
    
 
    toggleMain ! "Start"
 
  }
}

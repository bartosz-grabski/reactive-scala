package reactive5

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.LoggingReceive
import akka.event.Logging
import akka.persistence._

// states
sealed trait MoodState
case object Happy extends MoodState
case object Sad extends MoodState

case class MoodChangeEvent(state: MoodState)

class PersistentToggle extends PersistentActor {

  override def persistenceId = "persistent-toggle-id-1"

  def updateState(event: MoodChangeEvent): Unit =
    context.become(
      event.state match {
        case Happy => happy
        case Sad => sad
      })

  def happy: Receive = LoggingReceive {
    case "How are you?" =>
      persist(MoodChangeEvent(Sad)) {
        event =>
          updateState(event)
          sender ! "happy"
      }
    case "Done" =>
      sender ! "Done"
      context.stop(self)
  }

  def sad: Receive = LoggingReceive {
    case "How are you?" =>
      persist(MoodChangeEvent(Happy)) {
        event =>
          updateState(event)
          sender ! "sad"
      }

    case "Done" =>
      sender ! "Done"

  }
  def receiveCommand = happy

  val receiveRecover: Receive = {
    case evt: MoodChangeEvent => updateState(evt)
  }
}

class ToggleMain extends Actor {

  val toggle = context.actorOf(Props[PersistentToggle], "toggle")

  toggle ! "How are you?"
  toggle ! "How are you?"
  toggle ! "How are you?"
  toggle ! "Done"

  def receive = LoggingReceive {

    case "Done" =>
      context.stop(self)

    case msg: String =>
      println(s" received: $msg")

  }
}
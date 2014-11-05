package myActorTest
import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.WordSpecLike
import scala.concurrent.Future
import java.util.concurrent.Executor
import org.scalatest.BeforeAndAfterAll
import akka.testkit.ImplicitSender
import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorRef


class ToggleSpec extends TestKit(ActorSystem("ToggleSpec"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender {
  
  
  override def afterAll(): Unit = {
    system.shutdown()
  }

  "A Toggle" must {

    "start in a happy mood" in {
      val toggle = system.actorOf(Props[Toggle])
    
      toggle ! "How are you?"
      expectMsg("happy")
    }
    
    "change its mood" in {
      val toggle = system.actorOf(Props[Toggle])
      for (i <- 1 to 5){
        toggle ! "How are you?"
        expectMsg("happy")
        toggle ! "How are you?"
        expectMsg("sad")
      }
    } 
    
    "finish when done" in{
      val toggle = system.actorOf(Props[Toggle])
      toggle ! "Done"
      expectMsg("Done")
    }
 
  }

}


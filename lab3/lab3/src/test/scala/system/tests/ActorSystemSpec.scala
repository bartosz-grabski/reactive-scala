package system.tests

import akka.actor.ActorSystem
import akka.testkit.{ TestKit, ImplicitSender }
import org.scalatest.WordSpecLike
import org.scalatest.matchers.Matchers
import org.scalatest.BeforeAndAfterAll
import akka.actor.ActorRef
import actors.AuctionActor
import akka.actor.Props
import actors.AuctionSearch
import actors.BuyerActor
import actors.SellerActor
import scala.concurrent.duration._
import akka.testkit.TestProbe
import akka.actor.Actor
import messages.registerAuction
import messages.registerAuction
import messages.startRegistering
import messages.startRegistering
import messages.startRegistering
import messages.registerAuction
import messages.registerAuction
import messages.registerAuction
import messages.notifyWinner
import messages.notifyWinner
import akka.testkit.TestFSMRef
import akka.testkit.TestActorRef
import com.typesafe.config.ConfigFactory
import akka.testkit.EventFilter

/**
 * Is this the proper way to do it ?
 */
class ForwardActor(x:ActorRef) extends Actor {
  def receive = {
    case msg => x.forward(msg)
  }
}

class ActorSystemSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
 
  val seller1Auctions = Array("Auction1","Auction2")
  val seller2Auctions = Array("Auc3","Auct4")
  val buyerAuctions = Array("Auction1")
  val MAX_WAIT = 10000
  
  def this() = this(ActorSystem("MySpec", ConfigFactory.parseString("""
		  akka.test.filter-leeway = 50s
		  akka.loggers = ["akka.testkit.TestEventListener"]
  """)))
  
  
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
  
  "a system" should {
    "work ;p" in {
      val auctionSearch = system.actorOf(Props(new AuctionSearch()),"auctionSearch")
      val seller = system.actorOf(Props(new SellerActor("seller1",seller1Auctions)),"seller")
      val buyer = TestActorRef(Props(new BuyerActor(100,buyerAuctions)))
      val filter = EventFilter.info(pattern = "Winner of auction Auction1*",occurrences = 1)
      filter intercept {
        
      }
    }
  }
}
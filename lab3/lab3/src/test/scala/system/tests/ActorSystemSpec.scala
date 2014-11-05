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
 
  val seller1Auctions = List("Auction1","Auction2")
  val seller2Auctions = List("Auc3","Auct4")
  val MAX_WAIT = 10000
  
  val auctionProbe = TestProbe()
  val auctionSearch : ActorRef = system.actorOf(Props(new ForwardActor(auctionProbe.ref)),"auctionSearch")
  
  def this() = this(ActorSystem("ActorSystemSpec"))
  
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
  
  "a seller" should {
    "should register it's auctions" in {
      val seller1 = system.actorOf(Props(classOf[SellerActor],seller1Auctions),"Seller1")
      seller1 ! startRegistering()
      
    }
  }
}
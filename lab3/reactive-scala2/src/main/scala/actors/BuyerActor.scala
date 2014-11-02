package actors

import akka.actor.Actor
import akka.actor.FSM
import akka.actor.ActorLogging
import akka.actor.ActorRef
import messages.bid
import messages.startBidding
import scala.util.Random
import messages.notifyWinner
import scala.concurrent.duration._

class BuyerActor(auctions : List[ActorRef]) extends Actor with ActorLogging {
 
  val bidTimes = 10
  val system = akka.actor.ActorSystem("system")
  import system.dispatcher
 
  system.scheduler.scheduleOnce(2000 milliseconds, self, startBidding())
  
  def receive = {
    case startBidding() => {
      
      val bidValue = Random.nextInt(2000) % 200
      val auctionIndex = Random.nextInt(auctions.length)
      
      log.info(s"Starting bidding auction $auctionIndex with price $bidValue");
      auctions(auctionIndex) ! bid(bidValue,self)
      system.scheduler.scheduleOnce(2000 milliseconds, self, startBidding())
      
    }
    case notifyWinner(auctionId, bidPrice) => {
      log.info("Winner of auction "+auctionId+" with "+bidPrice)
    }
  }
}
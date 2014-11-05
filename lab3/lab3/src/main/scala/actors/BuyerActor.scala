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
import messages.auctionFound
import messages.auctionNotFound
import messages.startBidding
import messages.findAuction
import messages.bidRandom
import scala.collection.mutable.LinkedHashSet
import messages.notifyTopAnOffer
import messages.stopBidding

class BuyerActor(maxBid:Int,nameElements:Array[String]) extends Actor with ActorLogging {
 
  val bidTimes = 10
  val system = context.system
  val auctionSearch = system.actorSelection("/*/auctionSearch")
  var auctions = LinkedHashSet[ActorRef]()
  
  private val SEEK_TIME = 10000
  private val BID_TIME = 1000
  import system.dispatcher
 
  system.scheduler.scheduleOnce(SEEK_TIME milliseconds, self, startBidding())
  
  def receive = {
    case startBidding() => {
      
      
      val auctionIndex = Random.nextInt(nameElements.length)
      val auctionNameOrPartialName = nameElements(auctionIndex)
      
      log.info(s"Find auction $auctionNameOrPartialName");
      auctionSearch ! findAuction(auctionNameOrPartialName)
      system.scheduler.scheduleOnce(SEEK_TIME milliseconds, self, startBidding())
      system.scheduler.scheduleOnce(BID_TIME milliseconds, self, bidRandom())
      
    }
    case notifyWinner(auctionId, bidPrice) => {
      log.info("Winner of auction "+auctionId+" with "+bidPrice)
    }
    case auctionFound(auction, nameElement) => {
      log.info(s"Found auction for $nameElement")
      auctions = auctions + auction
    }
    case auctionNotFound(nameElement) => {
      log.info(s"Auction not found for $nameElement")
    }
    case bidRandom() => {
      val bidValue = Random.nextInt(2000) % maxBid
      if (auctions.size > 0) {
    	  auctions.toList(Random.nextInt(auctions.size)) ! bid(bidValue,self)
      }
      system.scheduler.scheduleOnce(BID_TIME milliseconds, self, bidRandom())
    }
    case notifyTopAnOffer(value,who,auction) => {
      val actorName = who.path.name;
      log.info(s"An offer was topped by $actorName. Price is now $value")
      if (value < maxBid+1) {
        auction ! bid(maxBid+1,self)
      }
    }
    case stopBidding(auction) => {
      log.info(s"Removing auction $auction.path.name from buyer $self.path.name")
      auctions.remove(auction)
    }
  }
}
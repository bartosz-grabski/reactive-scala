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
import scala.compat.Platform
import messages.perfInfo
import helpers.Conf

class BuyerActor(maxBid:Int,nameElements:Array[String]) extends Actor with ActorLogging {
 
  val bidTimes = 10
  val system = context.system
  val auctionSearch = system.actorSelection("/*/auctionMasterSearch")
  val performanceActor = system.actorSelection("/*/performanceActor");
  var auctions = LinkedHashSet[ActorRef]()
  
  private final val BID_TIME = 1500
  private val auctionTimes = collection.mutable.Map[String,Long](); //perhaps immutable ? :)
  import system.dispatcher
 
  system.scheduler.scheduleOnce(Conf.STARTUP_BUYER_MILL milliseconds, self, startBidding())
  
  def receive = {
    case startBidding() => {
      
      for (i <- 0 until nameElements.length) {
        val auctionNameOrPartialName = nameElements(i)
        log.info(s"Find auction $auctionNameOrPartialName");
        auctionTimes(auctionNameOrPartialName) = Platform.currentTime
        auctionSearch ! findAuction(auctionNameOrPartialName) 
      }
      

      system.scheduler.scheduleOnce(BID_TIME milliseconds, self, bidRandom())
      
    }
    case notifyWinner(auctionId, bidPrice) => {
      log.info("Winner of auction "+auctionId+" with "+bidPrice)
    }
    case auctionFound(auction, nameElement) => {
      log.info(s"Found auction for $nameElement")
      performanceActor ! perfInfo(Platform.currentTime - auctionTimes.get(nameElement).get)
      auctionTimes.remove(nameElement);
      auctions = auctions + auction
      
    }
    case auctionNotFound(nameElement) => {
      log.info(s"Auction not found for $nameElement")
    }
    case bidRandom() => {
      val bidValue = Random.nextInt(2000) % maxBid
      if (auctions.size > 0) {
          val auction = auctions.toList(Random.nextInt(auctions.size))
          log.info(s"Bidding auction "+auction.path.name);
    	  auction ! bid(bidValue,self)
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
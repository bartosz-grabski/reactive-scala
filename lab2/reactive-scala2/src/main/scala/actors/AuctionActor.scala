package actors

import akka.actor.Actor
import akka.actor.FSM
import akka.actor.ActorLogging
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import messages.bidTimerExpired
import messages.deleteTimerExpired
import messages.startAuction
import messages.bid
import messages.bidTimerExpired
import akka.actor.ActorRef
import messages.bidTimerExpired
import messages.notifyWinner
import messages.deleteTimerExpired

class AuctionActor(auctionId:Int) extends Actor with FSM[AuctionState,AuctionData] with ActorLogging {
  
  private val DELETE_TIMER_STARTED = "Delete timer started"
  private val BID_TIMER_STARTED = "Bid timer started"
  private val BID_TIMER_EXPIRED = "Bid timer expired"
  private val DELETE_TIMER_EXPIRED = "Delete timer expired"
    
  private val AUCTION_START = "Auction is about to start"
  private val AUCTION_ACTIVATE = "Auction received its first bid"
  private val AUCTION_EXPIRED = "Auction expires"
    
  val system = akka.actor.ActorSystem("system")
  import system.dispatcher  
    
  private def startBidTimer(bidTimer:Int) {
    log.info(BID_TIMER_STARTED)
    system.scheduler.scheduleOnce(Duration.create(bidTimer, TimeUnit.SECONDS), new Runnable {
      override def run = {
        log.info(BID_TIMER_EXPIRED)
		self ! bidTimerExpired()
      } 
    })
  }
  
  private def startDeleteTimer(deleteTimer:Int) {
    log.info(DELETE_TIMER_STARTED)
    system.scheduler.scheduleOnce(Duration.create(deleteTimer, TimeUnit.SECONDS), new Runnable {
      override def run = {
        self ! deleteTimerExpired()
        log.info(DELETE_TIMER_EXPIRED)
      } 
    })
  }
  
  startWith(Undefined, Uninitialized);
  
  when(Undefined) {
    case Event(startAuction(value,bidTime,deleteTime),Uninitialized) => {
      log.info(AUCTION_START)
      goto(Created) using AuctionDataBundle(value,bidTime,deleteTime)
    }
  }
  
  when(Created) {
    case Event(bidTimerExpired(),_) => {
      log.info(AUCTION_EXPIRED)
      goto(Ignored) using stateData
    }
    case Event(bid(price,buyer),AuctionDataBundle(value,bidTime,deleteTime)) => {
      if (price > value && price > 0) {
        log.info(AUCTION_ACTIVATE)
    	goto(Activated) using AuctionBuyerBundle(buyer,price,deleteTime)
      } else {
        stay
      }
    }
  }
  
  when(Ignored) {
    case Event(deleteTimerExpired(),_) => {
      log.info(DELETE_TIMER_EXPIRED)
      stay
    }
  }
  
  when(Activated) {
    case Event(bid(newBid,newBuyer),AuctionBuyerBundle(buyer,bid,deleteTime)) => {
      if (newBid > bid && newBid > 0) {
        log.info(s"${newBuyer.toString()} has beaten a bid with $newBid for auction $auctionId")
    	stay using AuctionBuyerBundle(newBuyer,newBid,deleteTime)
      } else {
        stay
      }
    }
    case Event(bidTimerExpired(),AuctionBuyerBundle(buyer,price,deleteTime)) => {
    	buyer ! notifyWinner(auctionId,price)
    	startDeleteTimer(deleteTime)
    	stay
    }
    case Event(deleteTimerExpired(),_) => {
    	goto(Sold)
    }
  }
  
  when(Sold) {
    case Event(e,s) => {
      //just ignore this event
      stay
    }
  }
  
  onTransition {
    case Undefined -> Created => {
      nextStateData match {
        case AuctionDataBundle(value,bidTime,deleteTime) => {
        	startBidTimer(bidTime)
        }
        
      }
    	
    }
    case Created -> Ignored => {
    	nextStateData match {
    	  case AuctionDataBundle(value,bidTime,deleteTime) => {
    		startDeleteTimer(deleteTime)
    	  }
    	}
    	
    }
    case Activated -> Sold => {
    	nextStateData match {
    	  case AuctionBuyerBundle(buyer,bid,deleteTime) => {
    		startDeleteTimer(deleteTime)
    	  }
    	}
    }
  }
  
}


sealed trait AuctionState
case object Undefined extends AuctionState
case object Created extends AuctionState
case object Ignored extends AuctionState
case object Activated extends AuctionState
case object Sold extends AuctionState

sealed trait AuctionData
case object Uninitialized extends AuctionData
case class AuctionDataBundle(value:Int,bidTime:Int,deleteTime:Int) extends AuctionData
case class AuctionBuyerBundle(buyer:ActorRef,bid:Int,deleteTime:Int) extends AuctionData
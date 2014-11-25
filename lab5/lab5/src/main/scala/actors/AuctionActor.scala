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
import messages.notifySeller
import messages.stopBidding
import messages.notifyTopAnOffer
import scala.concurrent.duration._
import messages.stopBidding
import messages.registerAuction
import messages.StateChangeEvent
import akka.event.LoggingReceive
import messages.bidTimerExpired
import messages.StateChangeEvent
import messages.StateChangeEvent
import messages.deleteTimerExpired
import messages.bidTimerExpired
import messages.StateChangeEvent
import messages.StateChangeEvent
import akka.persistence.PersistentActor
import messages.bidTimeCountdown
import messages.bidTimeCountdown
import akka.actor.Cancellable
import messages.StateChangeEvent
import messages.bidTimeCountdown
import akka.persistence.RecoveryCompleted
import messages.stopBidding

class AuctionActor(dataBundle:AuctionStartupData) extends PersistentActor with ActorLogging {
  
  override def persistenceId = dataBundle.name;
  
  private val DELETE_TIMER_STARTED = "Delete timer started"
  private val BID_TIMER_STARTED = "Bid timer started"
  private val BID_TIMER_EXPIRED = "Bid timer expired"
  private val DELETE_TIMER_EXPIRED = "Delete timer expired"
    
  private val AUCTION_START = "Auction is about to start"
  private val AUCTION_ACTIVATE = "Auction received its first bid"
  private val AUCTION_EXPIRED = "Auction expires"
    
    
  val system = context.system
  val auctionSearch = context.system.actorSelection("/*/auctionMasterSearch")
  var auctionState:AuctionData = AuctionDataBundle(dataBundle.value,dataBundle.bidTime,dataBundle.deleteTime);
  var auctionTimer = dataBundle.bidTime;
  var bidScheduler:Cancellable = _
  var deleteScheduler:Cancellable = _
  
  auctionSearch ! registerAuction(self,dataBundle.seller,dataBundle.name)
  
  import system.dispatcher  
    
  private def startBidTimer(bidTimer:Int) {
    log.info(BID_TIMER_STARTED)
    if (bidTimer > 0)
    	bidScheduler = system.scheduler.schedule(0 seconds, 1 seconds, self, bidTimeCountdown())
  }
  
  private def startDeleteTimer(deleteTimer:Int) {
    log.info(DELETE_TIMER_STARTED)
    deleteScheduler = system.scheduler.scheduleOnce(deleteTimer seconds, self, deleteTimerExpired())
  }
  
  log.info(AUCTION_START)
  
  def updateState(event : StateChangeEvent): Unit = {
    auctionState = event.data
    context.become(
      event.state match {
        case Created => created
        case Ignored => {
          startDeleteTimer(dataBundle.deleteTime);
          ignored
        }
        case Activated => activated
        case Sold => {
          startDeleteTimer(dataBundle.deleteTime);
          sold
        }
        case Undefined => {
          log.info("Undefined state")
          created
        }
      })
  }
  
  def created: Receive = LoggingReceive {
    case startAuction() => {
      startBidTimer(auctionTimer);
    }
    case bidTimerExpired() => {
      log.info(AUCTION_EXPIRED)
      persist(StateChangeEvent(Ignored,auctionState)) {
        evt => updateState(evt)
      }
    }
    case bid(price,buyer) => {
      log.info("b");
      auctionState match {
        case AuctionDataBundle(value,bidTime,deleteTime) => {
          if (price > value && price > 0) {
            log.info(AUCTION_ACTIVATE)
            persist(StateChangeEvent(Activated,AuctionBuyerBundle(buyer,price,deleteTime))) {
              evt => updateState(evt)
            }
          }
        }
        case _ => {
          sender ! stopBidding(self);
        }
      }
      
    }
    case bidTimeCountdown() => {
      persist(bidTimeCountdown()) {
        e => { 
          if (auctionTimer > 0) auctionTimer -= 1
          if (auctionTimer == 0) {
            bidScheduler.cancel();
            
            auctionState match {
              case AuctionDataBundle(value,bidTime,deleteTime) => {
                self ! startDeleteTimer(deleteTime)
              }
              case AuctionBuyerBundle(buyer,bid,deleteTime) => {
                self ! startDeleteTimer(deleteTime)
              }
              case _ => { }
            }
          }
        }
      }
    }
  }
  
  def ignored: Receive = LoggingReceive {
    case deleteTimerExpired() => {
      log.info(DELETE_TIMER_EXPIRED)
    }
    case a => {
      log.info("SOLD state event caught" + a.toString);
      sender ! stopBidding(self)
    }
  }
  
  def activated: Receive = LoggingReceive {
    case bid(newBid,newBuyer) => {
      log.info("a");
      auctionState match {
        case AuctionBuyerBundle(buyer,bid,deleteTime) => {
          if (newBid > bid) {
            persist(StateChangeEvent(Activated,AuctionBuyerBundle(newBuyer,newBid,deleteTime))) {
              evt => updateState(evt);
            }
          }
        }
        case _ => {
          sender ! stopBidding(self)
        }
      }
    }
    case bidTimerExpired() => {
      auctionState match {
        case AuctionBuyerBundle(buyer,bid,deleteTime) => {
          buyer ! notifyWinner(self.path.name,bid)
          dataBundle.seller ! notifySeller(buyer)
          persist(StateChangeEvent(Sold,AuctionBuyerBundle(buyer,bid,deleteTime))) {
            evt => updateState(evt)
          }
        }
        case _ => {
          //ignore
        }
      }
    }
  }
  
  def sold: Receive = LoggingReceive {
    case a => {
      log.info("SOLD state event caught" + a.toString);
      sender ! stopBidding(self)
    }
  }
  
  
  def receiveCommand = created
  def receiveRecover : Receive = {
    case evt : StateChangeEvent => {
      updateState(evt)
    }
    case bidTimeCountdown() => {
      if (auctionTimer > 0) auctionTimer -= 1
    }
    case RecoveryCompleted => {
      if (auctionTimer > 0) {
        //bidScheduler = system.scheduler.schedule(0 seconds, 1 seconds, self, bidTimeCountdown())
      }
      auctionState match {
        case AuctionDataBundle(value,bidTime,deleteTime) => {
          log.info(s"Recovery completed with $auctionTimer, $value")
        }
        case AuctionBuyerBundle(buyer,bid,deleteTime) => {
          log.info(s"Recovery completed with $auctionTimer, current_bid : $bid")
        }
        case _ => {
          log.info("Recovery completed")
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
case class AuctionStartupData(seller:ActorRef,value:Int,bidTime:Int,deleteTime:Int,name:String)
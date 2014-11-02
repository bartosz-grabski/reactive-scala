package helpers

import akka.actor.Actor
import akka.actor.FSM
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.ActorLogging
import actors.AuctionActor
import messages.startAuctionSystem
import actors.BuyerActor
import messages.startAuction
import messages.startBidding

class AuctionManager extends Actor with FSM[AuctionManagerState, AuctionManagerData] with ActorLogging {
  

  
  
  private val AUCTION_ACTOR_PREFIX = "auction_"
  private val BUYER_ACTOR_PREFIX = "buyer_"
  private val UNHANDLED_EVENT = "Unhandled event occured"
  private val MANAGER_ALREADY_STARTED = "Manager already started"
  private val MANAGER_GOING_TO_STOP = "Manager going to stop"
  private val MANAGER_STARTING = "Manager starting"
    
  private val DEFAULT_AUCTION_DURATION_SEC = 20
  private val DEFAULT_DELETE_DURATION_SEC = 5
  private val DEFAULT_AUCTION_VALUE = 120
  

  private def createAuctionActors(numberOfAuctions : Int) : List[ActorRef] = {
    return (0 to numberOfAuctions-1).map(num => context.actorOf(Props(new AuctionActor(num)),AUCTION_ACTOR_PREFIX+num)).toList
  }
  
  private def createBuyersActors(numberOfBuyers : Int, auctions:List[ActorRef]) : List[ActorRef] = {
    return (1 to numberOfBuyers).map(num => context.actorOf(Props(new BuyerActor(auctions)),BUYER_ACTOR_PREFIX+num)).toList
  }
  
  
  // FSM
  
  startWith(ManagerDisabled, Uninitialized)
  
  when(ManagerDisabled) {
    case Event(startAuctionSystem(numberOfAuctions, numberOfBuyers), Uninitialized) => {
      val auctions = createAuctionActors(numberOfAuctions)
      val buyers = createBuyersActors(numberOfBuyers,auctions)
      log.info(MANAGER_STARTING)
      goto(ManagerStarted) using AuctionSystemData(auctions,buyers)
    }
  }
  
  when(ManagerStarted) {
    case Event(stopAuctionSystem,_) => {
      log.info(MANAGER_GOING_TO_STOP)
      goto(ManagerDisabled) using AuctionSystemData(Nil,Nil)
    }
    case Event(startAuctionSystem(_,_), _) => {
      log.info(MANAGER_ALREADY_STARTED)
      stay 
    }
  }
  
  whenUnhandled {
    case Event(e,s) => {
      log.info(UNHANDLED_EVENT)
      stay
    }
  }
  
  onTransition {
    case ManagerDisabled -> ManagerStarted => {
      nextStateData match {
        case AuctionSystemData(auctions,buyers) => {
          for (auction <- auctions) {
            auction ! startAuction(DEFAULT_AUCTION_VALUE,DEFAULT_AUCTION_DURATION_SEC,DEFAULT_DELETE_DURATION_SEC)
          }
          for (buyer <- buyers) {
            buyer ! startBidding()
          }
        }
      }
    }
  }
  
  
}


sealed trait AuctionManagerState
case object ManagerStarted extends AuctionManagerState
case object ManagerDisabled extends AuctionManagerState

sealed trait AuctionManagerData
case object Uninitialized extends AuctionManagerData
case class AuctionSystemData(auctions: List[ActorRef], buyers: List[ActorRef]) extends AuctionManagerData
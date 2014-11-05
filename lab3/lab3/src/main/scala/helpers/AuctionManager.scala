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
import actors.SellerActor
import actors.AuctionSearch
import messages.startRegistering
import messages.startRegistering

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

  private def createSellersActors() : List[ActorRef] = {
     val seller1 = context.actorOf(Props(classOf[SellerActor],"Seller1",Array("Audi_A6","Mercedes_Benz","Golf")))
     val seller2 = context.actorOf(Props(classOf[SellerActor],"Seller2",Array("Lenovo_T410","Macbook_Air")))
     seller1 ! startRegistering()
     seller2 ! startRegistering()
     List(seller1,seller2)
  }
  
  private def createBuyersActors() : List[ActorRef] = {
    val buyer1 = context.actorOf(Props(classOf[BuyerActor],150,Array("NotExisting","Benz")),"Buyer1")
    val buyer2 = context.actorOf(Props(classOf[BuyerActor],200,Array("NotExisting1","Benz","Golf")),"Buyer2")
    val buyer3 = context.actorOf(Props(classOf[BuyerActor],400,Array("Macbook_Air","Benz","Lenovo")),"Buyer3")
    List(buyer1,buyer2,buyer3)
  }
  
  
  
  // FSM
  
  startWith(ManagerDisabled, Uninitialized)
  
  when(ManagerDisabled) {
    case Event(startAuctionSystem(), Uninitialized) => {

      val auctionSearch = context.system.actorOf(Props[AuctionSearch],"auctionSearch")
      val sellers = createSellersActors()
      val buyers = createBuyersActors()
      
      log.info(MANAGER_STARTING)
      goto(ManagerStarted) using AuctionSystemData(sellers,Nil)
    }
  }
  
  when(ManagerStarted) {
    case Event(stopAuctionSystem,_) => {
      log.info(MANAGER_GOING_TO_STOP)
      goto(ManagerDisabled) using AuctionSystemData(Nil,Nil)
    }
    case Event(startAuctionSystem(), _) => {
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
  
  initialize()
}


sealed trait AuctionManagerState
case object ManagerStarted extends AuctionManagerState
case object ManagerDisabled extends AuctionManagerState

sealed trait AuctionManagerData
case object Uninitialized extends AuctionManagerData
case class AuctionSystemData(auctions: List[ActorRef], buyers: List[ActorRef]) extends AuctionManagerData
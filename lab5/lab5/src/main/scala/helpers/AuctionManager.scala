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
import messages.startAuctionSystemTest
import messages.startAuctionSystem
import actors.AuctionActor
import actors.AuctionDataBundle
import actors.AuctionStartupData
import scala.util.Random
import messages.startAuction

class AuctionManager(sellers:Int = 2,buyers:Int = 3,auctions:Int = 5) extends Actor with FSM[AuctionManagerState, AuctionManagerData] with ActorLogging {
  
  
  private val AUCTION_ACTOR_PREFIX = "auction_"
  private val BUYER_ACTOR_PREFIX = "buyer_"
  private val UNHANDLED_EVENT = "Unhandled event occured"
  private val MANAGER_ALREADY_STARTED = "Manager already started"
  private val MANAGER_GOING_TO_STOP = "Manager going to stop"
  private val MANAGER_STARTING = "Manager starting"
    
  private val DEFAULT_AUCTION_DURATION_SEC = 60
  private val DEFAULT_DELETE_DURATION_SEC = 10
  private val DEFAULT_AUCTION_VALUE = 200
  private val AUCTION_PREFIX = "auction";
  
  private def createAuctions(sellers:List[ActorRef]) : List[ActorRef] = {
    var auctions = List[ActorRef]()
    for (i <- 1 to this.auctions) {
      val sellerIndex = Random.nextInt(sellers.size);
      val data = AuctionStartupData(sellers(sellerIndex),DEFAULT_AUCTION_VALUE,DEFAULT_AUCTION_DURATION_SEC,DEFAULT_DELETE_DURATION_SEC,AUCTION_PREFIX+i)
      val auction = context.actorOf(Props(classOf[AuctionActor],data),data.name)
      auction ! startAuction();
      auctions = auction :: auctions
    }
    auctions
  }

  private def createSellersActors() : List[ActorRef] = {
     val seller1 = context.actorOf(Props(classOf[SellerActor],"Seller1"))
     val seller2 = context.actorOf(Props(classOf[SellerActor],"Seller2"))
     seller1 ! startRegistering()
     seller2 ! startRegistering()
     List(seller1,seller2)
  }
  
  private def createBuyersActors() : List[ActorRef] = {
    val buyer1 = context.actorOf(Props(classOf[BuyerActor],150,Array("auction1","auction2")),"Buyer1")
    val buyer2 = context.actorOf(Props(classOf[BuyerActor],200,Array("auction1","auction4","auction2")),"Buyer2")
    val buyer3 = context.actorOf(Props(classOf[BuyerActor],400,Array("auction20","auction25","auction1")),"Buyer3")
    List(buyer1,buyer2,buyer3)
  }

  
  
  
  // FSM
  
  startWith(ManagerDisabled, Uninitialized)
  
  when(ManagerDisabled) {
    case Event(startAuctionSystem(), Uninitialized) => {

      val auctionMasterSearch = context.system.actorOf(Props[AuctionSearch],"auctionMasterSearch")
      val sellers = createSellersActors()
      val auctions = createAuctions(sellers)
      val buyers = createBuyersActors()
      
      
      log.info(MANAGER_STARTING)
      goto(ManagerStarted) using AuctionSystemData(sellers,Nil)
    }
    case Event(startAuctionSystemTest(), Uninitialized) => {
      
      val auctionSearch = context.system.actorOf(Props[AuctionSearch],"auctionMasterSearch")
      val sellers = createSellersActors()
      val auctions = createAuctions(sellers)
      val buyers = createBuyersActors()
      

      goto(ManagerStarted) using AuctionSystemData(Nil,Nil)
    }
  }
  
  when(ManagerStarted) {
    case Event(stopAuctionSystem,_) => {
      log.info(MANAGER_GOING_TO_STOP)
      goto(ManagerDisabled) using AuctionSystemData(Nil,Nil)
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
            auction ! startAuction()
          }
          for (buyer <- buyers) {
            buyer ! startBidding()
          }
        }
        case _ => {
          
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
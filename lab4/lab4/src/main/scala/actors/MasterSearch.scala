package actors

import akka.actor.Actor
import akka.routing._
import messages.registerAuction
import akka.event.LoggingReceive
import messages.findAuction
import akka.actor.Props
import messages.registerAuction
import messages.registerAuction
import helpers.AuctionSearchAwareProxy
import helpers.Conf

class MasterSearch extends Actor {
  
  /*
  val routees = Vector.fill(Conf.AUCTION_SEARCH_COUNT) {
    val r = context.actorOf(Props[AuctionSearch])
    context watch r
    ActorRefRoutee(r)
  }
    
  private val routerSearch = Router(RoundRobinRoutingLogic(), routees)
  private val routerRegister = Router(BroadcastRoutingLogic(), routees)
  
  
  private val routerSearch = {
    
    Router(BroadcastRoutingLogic(), routees)
  }
  private val routerRegister = {
    Router(RoundRobinRoutingLogic(), routees)
  }*/
  
  /*
   * Using resizer might not make sense, because auction search is stateful. There is no replication when new actor is created.
   */
  var resizer = DefaultResizer(lowerBound = 2, upperBound = 10)
  
  val router = context.actorOf(BroadcastPool(5, Some(resizer)).props(Props[AuctionSearch]))
      
  def receive = LoggingReceive{
    
    case registerAuction(auction,seller,name) => {
      //routerRegister.route(registerAuction(auction,seller,name), sender)
      router ! registerAuction(auction,seller,name)
    }
    
    case findAuction(nameElement) => {
      //val proxy = context.actorOf(Props(classOf[AuctionSearchAwareProxy],sender,nameElement))
      //routerSearch.route(findAuction(nameElement), proxy)
      router ! findAuction(nameElement)
    }
    
  }

}
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
import messages.registerAuction
import messages.findAuction
import akka.actor.Props
import messages.auctionFound
import messages.auctionNotFound
import akka.actor.InvalidActorNameException
import akka.actor.InvalidActorNameException
import messages.auctionNameInUse
import messages.startAuction

class AuctionSearch extends Actor with FSM[AuctionSearchState, AuctionSearchData] with ActorLogging {

  startWith(AuctionSearchStarted, AuctionSearchBundle(List()))

  when(AuctionSearchStarted) {

    case Event(registerAuction(name: String, seller:ActorRef), AuctionSearchBundle(auctions)) => {
      log.info(s"Registering an auction with a name : $name")
      try {
        val auction = context.actorOf(Props(classOf[AuctionActor], seller), name)
        auction ! startAuction(300,60,5)
        stay using AuctionSearchBundle(auction :: auctions)
      } catch {
        case e: InvalidActorNameException => {
          sender ! auctionNameInUse(name)
          stay using AuctionSearchBundle(auctions)
        }
      }

    }
    case Event(findAuction(nameElement: String), AuctionSearchBundle(auctions)) => {
      log.info(s"Searching for an auction with a name : $nameElement")
      val actor = auctions.find(x => x.path.name.contains(nameElement))
      if (actor == None) {
        sender ! auctionNotFound(nameElement: String)
      } else {
        sender ! auctionFound(actor.get, nameElement)
      }
      stay using AuctionSearchBundle(auctions)
    }
  }
  
  initialize()
}

sealed trait AuctionSearchState
case object AuctionSearchStarted extends AuctionSearchState

sealed trait AuctionSearchData
case class AuctionSearchBundle(auctions: List[ActorRef]) extends AuctionSearchData
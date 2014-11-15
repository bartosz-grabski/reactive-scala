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

    case Event(registerAuction(auction:ActorRef, seller:ActorRef, name:String), AuctionSearchBundle(auctions)) => {
      stay using AuctionSearchBundle(auction :: auctions)
    }
    case Event(findAuction(nameElement: String), AuctionSearchBundle(auctions)) => {
      log.info(s"Searching for an auction with a name : $nameElement")
      val actor = auctions.find(x => x.path.name.equals(nameElement))
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
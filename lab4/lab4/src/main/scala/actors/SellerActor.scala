package actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import messages.registerAuction
import messages.notifySeller
import messages.startRegistering
import messages.startRegistering
import messages.startRegistering
import messages.startRegistering
import scala.concurrent.duration._
import messages.auctionNameInUse
import messages.startRegistering

object SellerActor {
  val STARTUP_DELAY_MILL = 2000 
}

class SellerActor(name:String) extends Actor with ActorLogging { 
  
  
  val system = context.system
  val auctionSearch = system.actorSelection("/*/auctionMasterSearch")
  import system.dispatcher
  
  def receive = {
    case notifySeller(winner) => {
      val winnerName = winner.path.name
      log.info(s"Seller $name's auction is finished. The winner is $winnerName")
    }
    case auctionNameInUse(name) => {
      log.info("Auction name in use")
    }
  }
  
  
}
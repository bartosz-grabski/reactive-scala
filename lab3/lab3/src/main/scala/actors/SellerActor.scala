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

object SellerActor {
  val STARTUP_DELAY_MILL = 3000 
}

class SellerActor(name:String,titles:Array[String]) extends Actor with ActorLogging { 
  
  
  val system = context.system
  val auctionSearch = system.actorSelection("/*/auctionSearch")
  import system.dispatcher
  
  //system.scheduler.scheduleOnce(SellerActor.STARTUP_DELAY_MILL milliseconds, self, startRegistering())
  
  def receive = {
    case notifySeller(winner) => {
      val winnerName = winner.path.name
      log.info(s"Seller $name's auction is finished. The winner is $winnerName")
    }
    case startRegistering() => {
        for (title <- titles) {
        	log.info("Registering auction $title")
        	auctionSearch ! registerAuction(title,self)
        }
    }
    case auctionNameInUse(name) => {
      log.info("Auction name in use")
    }
  }
  
}
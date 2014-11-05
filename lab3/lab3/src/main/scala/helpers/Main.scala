package helpers

import akka.actor.ActorSystem
import akka.actor.Props
import messages.startAuctionSystem
import messages.startAuctionSystem

object Main {
  def main(args: Array[String]) {
	  
	  val system = ActorSystem()
	  val manager = system.actorOf(Props[AuctionManager])
	  manager ! startAuctionSystem()
  }
}
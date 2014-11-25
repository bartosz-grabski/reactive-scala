package helpers

import akka.actor.ActorSystem
import akka.actor.Props
import messages.startAuctionSystem
import messages.startAuctionSystem

object Main {
  def main(args: Array[String]) {
	  
	  val system = ActorSystem()
	  val manager = system.actorOf(Props(classOf[AuctionManager],5,5,1000))
	  manager ! startAuctionSystem()
  }
}
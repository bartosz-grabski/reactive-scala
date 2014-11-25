package helpers

import akka.actor.ActorSystem
import akka.actor.Props
import messages.startAuctionSystem

object MainInMemory {
  def main(args: Array[String]) {
	  val system = ActorSystem()
	  val manager = system.actorOf(Props(classOf[AuctionManager],5,5,1000))
	  manager ! startAuctionSystem()
	  
	  Thread.sleep(10000)
	  system.shutdown();
	  Thread.sleep(10000)
	  
	  val restarted = ActorSystem()
	  val restartedManager = restarted.actorOf(Props(classOf[AuctionManager],5,5,1000))
	  restartedManager ! startAuctionSystem()
  }
}
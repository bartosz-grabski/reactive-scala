package performance.tests

import akka.actor.ActorSystem
import akka.actor.Props
import messages.startAuctionSystem
import messages.startAuctionSystem
import akka.testkit.EventFilter
import helpers.AuctionManager
import messages.startAuctionSystemTest
import helpers.Conf

object MainPerformanceTest {
  
  def main(args: Array[String]) {
    
      val auctionCount = Conf.AUCTIONS
	  val system = ActorSystem()
	  val performanceActor = system.actorOf(Props(classOf[PerformanceGatheringActor],auctionCount),"performanceActor")
	  val manager = system.actorOf(Props(classOf[AuctionManager],3, 3, auctionCount))
	  manager ! startAuctionSystemTest()

  }
}
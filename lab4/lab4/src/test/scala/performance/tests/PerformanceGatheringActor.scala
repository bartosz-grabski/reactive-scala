package performance.tests

import akka.actor.Actor
import messages.perfInfo
import scala.concurrent.duration._
import messages.savePerformance
import java.io.PrintWriter
import java.io.File
import akka.actor.ActorLogging
import helpers.Conf

class PerformanceGatheringActor(auctions:Int) extends Actor with ActorLogging {
  
  val system = context.system
  import system.dispatcher
 
  system.scheduler.scheduleOnce(Conf.PERF_DURATION_MILL milliseconds, self, savePerformance())
  
  var performanceInfo = List[Long]();
  
  def receive = {
    case perfInfo(info) => {
      log.error("performance received "+info)
      performanceInfo = info :: performanceInfo;
    }
    case savePerformance() => {
      val writer = new PrintWriter(new File("rroutput"+(auctions)))
      performanceInfo.foreach( x => {
    	  writer.write(x.toString+" ")
      })
      writer.flush();
      exit();
    }
  }
}
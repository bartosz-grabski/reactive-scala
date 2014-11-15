package helpers

import akka.actor.Actor
import messages.findAuction
import messages.auctionFound
import messages.auctionNotFound
import akka.actor.ActorRef
import akka.actor.ActorLogging
import akka.actor.ReceiveTimeout
import messages.auctionNotFound
import messages.auctionNotFound

/**
 * This class is responsible for creating a proxy between the master search and auction searches. When broadcast logic search 
 * is enabled then it waits for responses from all auction search actors (it is aware how many responses should it get). Then it
 * passes the result to the querying seller actor
 */
class AuctionSearchAwareProxy(buyer:ActorRef, nameElement: String) extends Actor with ActorLogging {
  
  import scala.concurrent.duration._
  
  var received = 0;
  var found = false;
  
  context.setReceiveTimeout(Conf.PROXY_TIMEOUT_SEC seconds);
  
  def receive = {
    case auctionFound(auctionActor:ActorRef,nameElement:String) => {
      log.info("auction found "+nameElement);
      found = true;
      buyer ! auctionFound(auctionActor,nameElement);
    }
    case auctionNotFound(nameElement:String) => {
      received = received + 1;
      if (received == Conf.AUCTION_SEARCH_COUNT && !found) {
        buyer ! auctionNotFound(nameElement)
      }
    }
    case ReceiveTimeout => {
      log.info("AuctionSearchAwareProxy stopped after timeout")
      if (!found) buyer ! auctionNotFound(nameElement)
      context.stop(self)
    }
    case _ => {
      
    }
  }

}
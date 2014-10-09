package reactive1

import akka.actor._
import akka.event.LoggingReceive
import com.typesafe.config.ConfigFactory
 
///////////////////////////////
// Logging actor activities  //
///////////////////////////////


// Logging options: read article
// http://doc.akka.io/docs/akka/snapshot/scala/logging.html
//
// a) ActorLogging
// class MyActor extends Actor with akka.actor.ActorLogging {
//  ...
// }
//
// b) LoggingReceive
//
//  def receive = LoggingReceive {
//     ....
//  }
// 
// Hint: in order to enable logging you can also pass the following arguments to the VM
// -Dakka.loglevel=DEBUG -Dakka.actor.debug.receive=on
// (In Eclipse: Run Configuraiton -> Arguments / VM Arguments)


/*
 *  type Receive = PartialFunction[Any, Unit]
 *           Any  -> any message can arrive
 *           Unit -> the actor can do something, but does not return anything
 *            
 *  trait Actor {
 *     implicit val self: ActorRef
 *     def receive: Receive
 *     ...
 *  }
 *
 * API documentation: http://doc.akka.io/api/akka/2.2.3
 * 
 */

class Counter2 extends Actor {
  var count = 0
  def receive = LoggingReceive {
    case "incr" => count += 1; println(Thread.currentThread.getName + "!!!.")
    case "get"  => sender ! count // "!" operator is pronounced "tell" in Akka
  }
}
 
/* 
 * abstract class ActorRef {
 *   def !(message: Any)(implicit sender: ActorRef = Actor.noSender): Unit
 *   ...
 * }
 * 
 * 
 */

class CounterMain2 extends Actor {
  val counter = context.actorOf(Props[Counter2], "counter")
  counter ! "incr"
  counter ! "incr"
  counter ! "incr"
  counter ! "get"
  
  def receive = {
    case count: Int =>
      println(s"count received: $count" )
      println(Thread.currentThread.getName + ".")
      context.stop(self)
  }
}

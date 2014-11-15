    package reactiveRoutersDemo      
    
    import akka.routing._
    import akka.actor._
    import akka.event._
    
    import scala.concurrent.duration._
    
    import akka.event.LoggingReceive
    object Master{
	   
	    case class Terminated(a: ActorRef);
    }
    
    class Master extends Actor {
     val nbOfroutees :Int = 5
     import Master._
     import Worker._
      var router = {
         val routees = Vector.fill(5) {
         val r = context.actorOf(Props[Worker])
         context watch r
         ActorRefRoutee(r)
     }
     Router(BroadcastRoutingLogic(), routees)
    }
     
    def receive = LoggingReceive{
     
	    case w: Work =>
	    	router.route(w, sender())
	    case Terminated(a) =>
		    router = router.removeRoutee(a)
		    val r = context.actorOf(Props[Worker])
		    context watch r
		    router = router.addRoutee(r)
    }
    }

   
	object Worker{
	  case class Work(data :String)
	  case object  Done
	  case object  Failed
	}
	
	class Worker extends Actor with ActorLogging{
	  import Worker._
	  
	  def action: Receive = LoggingReceive {
	    case Work(data)  =>
	     
	      log.info("I got " + data + " and I am working")
	     // sender ! Done
	     
	  }
	
	  def receive = action
	
	}
	
	object MasterManager{
	  case object Init
	  
	}
	
	class MasterManager (master: ActorRef) extends Actor with ActorLogging {
	
	import MasterManager._
	import Worker._
	
	def myBehaviour: Receive = LoggingReceive {
	  
	  case Init =>
	     master ! Work("my data")
	 // case Done =>
	    //log.info("I got Done message from worker")
	  
	 }
	
	  def receive = myBehaviour
	}
	
	object ReactiveRouterApp extends App {
	  
	 val system = ActorSystem("ReactiveRouters")
	 val master=system.actorOf(Props(classOf[Master]), "master")  
		
	 val manager=system.actorOf(Props(classOf[MasterManager], master), "manager")
	 manager! MasterManager.Init
	   
	  
	 
	 
	}

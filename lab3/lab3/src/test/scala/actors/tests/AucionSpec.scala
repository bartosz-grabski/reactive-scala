package actors.tests
import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.WordSpecLike
import scala.concurrent.Future
import java.util.concurrent.Executor
import org.scalatest.BeforeAndAfterAll
import akka.testkit.ImplicitSender
import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorRef
import actors.AuctionActor
import akka.testkit.TestFSMRef
import actors.SellerActor
import akka.testkit.TestProbe
import actors.Uninitialized
import actors.Undefined
import messages._
import actors.Created
import actors.AuctionData
import actors.AuctionDataBundle
import actors.Ignored
import actors.Activated
import actors.AuctionDataBundle
import actors.AuctionBuyerBundle
import actors.Sold
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit

/**
 * Ask about testing case classes and stateData in FSM
 * Testing sending message to itself
 */
class AuctionSpec extends TestKit(ActorSystem("AuctionSpec"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender {
  
  val sellerProbe = TestProbe()
  var buyerProbe = TestProbe()
  val sellerMock = sellerProbe.ref
  val buyerMock = buyerProbe.ref
  
  override def afterAll(): Unit = {
    system.shutdown()
  }

  "An Auction" should {
     "start in an Undefined state with Uninitialized data" in {
       val fsm = TestFSMRef(new AuctionActor(sellerMock))
       assert(fsm.stateName == Undefined)
       assert(fsm.stateData == Uninitialized)
     }
  }
    
  "An Auction" when {
    "Undefined" should {
      "proceed to Created with data set when startAuction message is sent" in {
        
        val value = 100;
        val bidTime = 50;
        val deleteTime = 30;
        val fsm = TestFSMRef(new AuctionActor(sellerMock))
        fsm ! startAuction(value,bidTime,deleteTime)
        assert(fsm.stateName == Created)
        //dopytac
        fsm.stateData match {
          case AuctionDataBundle(a,b,c) => {
            assert(a == value);
            assert(b == bidTime);
            assert(c == deleteTime);
          }
          case _ => {
            fail();
          }
        }
      }
      "ignore messages other than startAuction" in {
        val fsm = TestFSMRef(new AuctionActor(sellerMock))
        fsm ! "other message"
        expectMsg("Undefined operation for current state")
      }
    }
    "Created" should {
      "proceed to Ignored if bidTimerExpired message is received" in {
        val fsm = TestFSMRef(new AuctionActor(sellerMock))
        fsm.setState(Created,AuctionDataBundle(10,10,10))
        fsm ! bidTimerExpired()
        assert(fsm.stateName == Ignored)
      }
      "proceed to Activated if bid message is received" in {
        val fsm = TestFSMRef(new AuctionActor(sellerMock))
        val bidValue = 100
        fsm.setState(Created,AuctionDataBundle(10,10,10))
        fsm ! bid(bidValue,buyerMock)
        assert(fsm.stateName == Activated)
      }
      "ignore other messages" in {
        val fsm = TestFSMRef(new AuctionActor(sellerMock))
        fsm.setState(Created,AuctionDataBundle(10,10,10))
        fsm ! "other message"
        expectMsg("Undefined operation for current state")
      }
    }
     "Ignored" should {
       "stay in ignored after delete timer expires" in {
         val fsm = TestFSMRef(new AuctionActor(sellerMock))
         fsm.setState(Ignored)
         fsm ! deleteTimerExpired()
         assert(fsm.stateName == Ignored)
       }
       "send stopBidding event to sender for other events" in {
         val fsm = TestFSMRef(new AuctionActor(sellerMock))
         fsm.setState(Ignored,AuctionDataBundle(10,10,10))
         fsm ! bid(100,buyerMock)
         expectMsg(stopBidding(fsm))
       }
     }
     "Activated" should {
       "top an offer if a higher bid is received and notify previous buyer" in {
         val fsm = TestFSMRef(new AuctionActor(sellerMock))
         fsm.setState(Activated,AuctionBuyerBundle(buyerMock,100,5))
         fsm ! bid(150,buyerMock)
         assert(fsm.stateName == Activated)
         buyerProbe.expectMsg(notifyTopAnOffer(150,buyerMock,fsm))
         fsm.stateData match {
           case AuctionBuyerBundle(m,b,t) => {
             assert(m == buyerMock)
             assert(b == 150)
             assert(t == 5)
             
           }
           case _ => {
             fail()
           }
         }
       }
       "notify winner when bid timer is expired" in {
         val fsm = TestFSMRef(new AuctionActor(sellerMock))
         fsm.setState(Activated,AuctionBuyerBundle(buyerMock,100,5))
         fsm ! bidTimerExpired()
         sellerProbe.expectMsg(notifySeller(buyerMock))
         buyerProbe.expectMsg(notifyWinner(fsm.path.name,100))
         assert(fsm.stateName == Sold)
       }
     }
     "Sold" should {
       "ignore all events" in {
         val fsm = TestFSMRef(new AuctionActor(sellerMock))
         fsm.setState(Sold)
         fsm ! "message"
         assert(fsm.stateName == Sold)
         fsm ! deleteTimerExpired()
         assert(fsm.stateName == Sold)
         
       }
     }
   }
}



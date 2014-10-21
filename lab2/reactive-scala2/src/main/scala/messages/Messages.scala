package messages

import akka.actor.ActorRef

sealed trait AuctionManagamentMessage
sealed trait AuctionMessage
sealed trait TimerMessage
 
case class startAuctionSystem(numberOfAuctions: Int, numberOfBuyers: Int) extends AuctionManagamentMessage
case class stopAuctionSystem() extends AuctionManagamentMessage
case class startAuction(value:Int,bidTime:Int,deleteTime:Int) extends AuctionManagamentMessage

case class bid(price:Int, buyer: ActorRef) extends AuctionMessage
case class startBidding() extends AuctionMessage
case class notifyWinner(auctionId:Int, bidPrice:Int) extends AuctionMessage
case class youWon(auctionId: Int) extends AuctionMessage
case class auctionIsOver() extends AuctionMessage

case class bidTimerExpired() extends TimerMessage
case class deleteTimerExpired() extends TimerMessage
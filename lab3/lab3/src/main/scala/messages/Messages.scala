package messages

import akka.actor.ActorRef

sealed trait AuctionManagamentMessage
sealed trait AuctionMessage
sealed trait TimerMessage
sealed trait AuctionSearchMessage
 
case class startAuctionSystem() extends AuctionManagamentMessage
case class stopAuctionSystem() extends AuctionManagamentMessage
case class startAuction(value:Int,bidTime:Int,deleteTime:Int) extends AuctionManagamentMessage

case class bid(price:Int, buyer: ActorRef) extends AuctionMessage
case class startBidding() extends AuctionMessage
case class notifyWinner(auction:String, bidPrice:Int) extends AuctionMessage
case class notifySeller(winner:ActorRef) extends AuctionMessage
case class startRegistering() extends AuctionMessage
case class youWon(auctionId: Int) extends AuctionMessage
case class auctionIsOver() extends AuctionMessage
case class bidRandom() extends AuctionMessage
case class notifyTopAnOffer(price:Int,who:ActorRef,auction: ActorRef) extends AuctionMessage
case class stopBidding(auction:ActorRef) extends AuctionMessage

case class bidTimerExpired() extends TimerMessage
case class deleteTimerExpired() extends TimerMessage

case class registerAuction(name:String, seller:ActorRef) extends AuctionSearchMessage
case class findAuction(nameElement:String) extends AuctionSearchMessage
case class auctionFound(actor:ActorRef, nameElement:String) extends AuctionSearchMessage
case class auctionNotFound(nameElement:String) extends AuctionSearchMessage
case class auctionNameInUse(name:String) extends AuctionSearchMessage
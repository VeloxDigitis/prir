package me.kcybulski.gauss

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import me.kcybulski.gauss.GaussSeidelActor.{XCurrentRequest, XOldRequest}

class GaussSeidelRow(A: Array[Double], b: Double, index: Int, boss: ActorRef) extends Actor with ActorLogging {

  var next: Option[ActorRef] = Option.empty
  var sum = 0.0

  override def receive: Receive = LoggingReceive {
    case next: ActorRef => this.next = Option(next)
    case x: XOldRequest => sum = b + A
      .zipWithIndex
      .map{
        case (v, i) if i > this.index => v * -x.values(i)
        case _ => 0
      }
      .sum
    case x: XCurrentRequest =>
      val result = XCurrentRequest(
        x.values :+ sum + A
          .zipWithIndex
          .map{
            case (v, i) if i < this.index => v * -x.values(i)
            case _ => 0
          }
          .sum
      )
      next.fold(boss ! result)(_ ! result)
  }

}

object GaussSeidelRow {

  def props(A: Array[Double], b: Double, index: Int, boss: ActorRef) = Props(new GaussSeidelRow(A, b, index, boss))

}
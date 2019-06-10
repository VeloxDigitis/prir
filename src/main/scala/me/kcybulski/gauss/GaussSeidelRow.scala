package me.kcybulski.gauss

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import me.kcybulski.gauss.GaussSeidelActor.XRequest

class GaussSeidelRow(A: Array[Double], b: Double, index: Int, boss: ActorRef) extends Actor with ActorLogging {

  var next: Option[ActorRef] = Option.empty

  override def receive: Receive = LoggingReceive {
    case next: ActorRef => this.next = Option(next)
    case x: XRequest =>
      val result = XRequest(x.old, x.current.:+(f(x)))
      next.fold(boss ! result)(_ ! result)
  }

  def f(req: XRequest): Double = {
    b + A
      .zipWithIndex
      .map{
        case (v, i) if i < this.index => v * -req.current(i)
        case (_, i) if i == this.index => 0
        case (v, i) if i > this.index => v * -req.old(i)
      }
      .sum
  }

}

object GaussSeidelRow {

  def props(A: Array[Double], b: Double, index: Int, boss: ActorRef) = Props(new GaussSeidelRow(A, b, index, boss))

}
package me.kcybulski.jacobi

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import me.kcybulski.Start.Row
import me.kcybulski.jacobi.JacobiActor.Result
import me.kcybulski.jacobi.RowActor.XRequest

class RowActor(Ao: Row, b: Double, index: Int) extends Actor with ActorLogging {
  private val n = 1 / Ao.values(index)
  private val bn = b*n
  private val A = Row(Ao.values.map(-_*n))
  A.values(index) = 0

  override def receive: Receive = LoggingReceive {
    case req: XRequest => req.requester.tell(Result(req.x.zip(A.values).map(v => v._1*v._2).sum + bn, index), self)
  }

}

object RowActor {

  def props(A: Row, b: Double, index: Int) = Props(new RowActor(A, b, index))

  case class XRequest(x: Array[Double], requester: ActorRef) {
    override def toString: String = x.mkString("[", ", ", "]")
  }

}
package me.kcybulski

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import me.kcybulski.MatrixActor.{Result, Row}
import me.kcybulski.RowActor.XRequest

class RowActor(Ao: Row, b: Double, index: Int) extends Actor with ActorLogging{
  private val n = 1 / Ao.values(index)
  private val bn = b*n
  private val A = Row(Ao.values.map(-_*n))
  A.values(index) = 0

  override def receive: Receive = {
    case req: XRequest =>
      log.debug("Sending result")
      req.requester.tell(Result(req.x.zip(A.values).map(v => v._1*v._2).sum + bn, index), self)
  }

}

object RowActor {

  def props(A: Row, b: Double, index: Int) = Props(new RowActor(A, b, index))

  case class XRequest(x: Array[Double], requester: ActorRef)

}
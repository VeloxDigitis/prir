package me.kcybulski.gauss

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import me.kcybulski.gauss.GaussSeidelActor.XRequest

class GaussSeidelActor(rows: Array[Array[Double]], iterations: Int) extends Actor with ActorLogging {

  private val rowActors = rows
      .zipWithIndex
      .map{case (e, i) => (e.map(_ / e(i)), i)}
      .map({
        case( row: Array[Double], index: Int ) =>
          context.actorOf(GaussSeidelRow.props(row.dropRight(1), row.last, index, self), s"row-$index")
      })

  rowActors.reduceLeft{(a: ActorRef, b: ActorRef) => a ! b; b}

  gaussSeidel(Array.fill(rows.length)(0.0))

  override def receive: Receive = LoggingReceive {

    case result: XRequest =>
      if(result.current
        .zip(result.old)
        .map(z => Math.abs(z._1 - z._2))
        .exists(_ > 0.001))
        gaussSeidel(result.current)
      else
        log.info("{}", result.current.mkString("[", ",", "]"))

  }

  def gaussSeidel(current: Array[Double]) {
    rowActors(0) ! XRequest(current, Array.empty[Double])
  }

}


object GaussSeidelActor {

  def props(rows: Array[Array[Double]], iterations: Int = 100) = Props(new GaussSeidelActor(rows, iterations))

  case class XRequest(old: Array[Double], current: Array[Double]) {
    override def toString: String = this.old.mkString("[", ", ", "]") + "\\" + this.current.mkString("[", ", ", "]")
  }

}

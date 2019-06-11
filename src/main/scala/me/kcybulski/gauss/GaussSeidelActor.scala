package me.kcybulski.gauss

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import me.kcybulski.gauss.GaussSeidelActor.{XCurrentRequest, XOldRequest}

class GaussSeidelActor(rows: Array[Array[Double]], iterations: Int) extends Actor with ActorLogging {

  private val rowActors = rows
      .zipWithIndex
      .map{case (e, i) => (e.map(_ / e(i)), i)}
      .map({
        case( row: Array[Double], index: Int ) =>
          context.actorOf(GaussSeidelRow.props(row.dropRight(1), row.last, index, self), s"row-$index")
      })
  private var lastResults = Array.fill(rows.length)(0.0)

  rowActors.reduceLeft{(a: ActorRef, b: ActorRef) => a ! b; b}

  gaussSeidel(lastResults)

  override def receive: Receive = LoggingReceive {

    case result: XCurrentRequest =>
      if(lastResults.zip(result.values)
      .map(x => Math.abs(x._1 - x._2))
      .exists(_ > 0.001))
        gaussSeidel(result.values)
      else
        log.info("{}", result)

  }

  def gaussSeidel(current: Array[Double]) {
    this.lastResults = current
    rowActors.foreach(_ ! XOldRequest(current))
    rowActors(0) ! XCurrentRequest(Array.empty[Double])
  }

}


object GaussSeidelActor {

  def props(rows: Array[Array[Double]], iterations: Int = 100) = Props(new GaussSeidelActor(rows, iterations))

  case class XOldRequest(values: Array[Double]) {
    override def toString: String = this.values.mkString("[", ", ", "]")
  }

  case class XCurrentRequest(values: Array[Double]) {
    override def toString: String = this.values.mkString("[", ", ", "]")
  }

}

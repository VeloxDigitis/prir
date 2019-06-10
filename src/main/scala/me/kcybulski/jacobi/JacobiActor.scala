package me.kcybulski.jacobi

import akka.actor.{Actor, ActorLogging, Props}
import akka.event.LoggingReceive
import me.kcybulski.Start.Row
import me.kcybulski.jacobi.JacobiActor.Result
import me.kcybulski.jacobi.RowActor.XRequest

class JacobiActor(rows: Array[Array[Double]]) extends Actor with ActorLogging {

  private var iteration = 0
  private val rowActors = rows
      .zipWithIndex
    .map{case (e, i) => (e.map(_ / e(i)), i)}
      .map({
        case( row: Array[Double], index: Int ) =>
          context.actorOf(RowActor.props(Row(row.dropRight(1)), row.last, index), s"row-$index")
      })


  var x: Array[Array[Double]] = Array.fill(1, rowActors.length)(0)

  jacobi()

  override def receive: Receive = LoggingReceive {

    case result: Result =>
      x(iteration + 1)(result.index) = result.x

      if(!x(iteration + 1).exists(s => s.isNaN)) {
        iteration = iteration + 1
        if(x(iteration).zip(x(iteration - 1))
          .map(z => Math.abs(z._1 - z._2))
          .exists(v => v > 0.001))
          jacobi()
        else
          log.info(x(iteration).mkString("[", ", ", "]"))
      }

  }

  private def jacobi(): Unit = {
    this.x = this.x.:+(Array.fill[Double](rowActors.length)(Double.NaN))
    this.rowActors.foreach(_ ! XRequest(x(iteration), self))
  }
}

object JacobiActor {

  def props(rows: Array[Array[Double]]) = Props(new JacobiActor(rows))

  case class Result(x: Double, index: Int)

}

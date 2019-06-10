package me.kcybulski.jacobi

import akka.actor.{Actor, ActorLogging, Props}
import akka.event.LoggingReceive
import me.kcybulski.Start.Row
import me.kcybulski.jacobi.JacobiActor.Result
import me.kcybulski.jacobi.RowActor.XRequest

class JacobiActor(rows: Array[Array[Double]]) extends Actor with ActorLogging {

  private var i = 0
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
      x(i + 1)(result.index) = result.x

      if(!x(i + 1).exists(s => s.isNaN)) {
        i = i + 1
        if(x(i).zip(x(i - 1))
          .map(z => Math.abs(z._1 - z._2))
          .exists(v => v > 0.001))
          jacobi()
        else
          log.info(x(i).mkString("[", ", ", "]"))
      }

  }

  private def jacobi(): Unit = {
    this.x = this.x.:+(Array.fill[Double](rowActors.length)(Double.NaN))
    this.rowActors.foreach(_ ! XRequest(x(i), self))
  }
}

object JacobiActor {

  def props(rows: Array[Array[Double]]) = Props(new JacobiActor(rows))

  case class Result(x: Double, index: Int)

}

package me.kcybulski

import akka.actor.{Actor, ActorLogging, Props}
import me.kcybulski.MatrixActor.{Result, Row}
import me.kcybulski.RowActor.XRequest

import scala.io.BufferedSource
import scala.io.Source.fromFile

class MatrixActor(filepath: String, iterations: Int) extends Actor with ActorLogging {

  private var i = 0
  private val file: BufferedSource = fromFile(filepath)
  private val rowActors = file
      .getLines
      .toArray
      .map(_.split("\\s+"))
      .map(_.map(d => d.toDouble))
      .zipWithIndex
      .map({
        case( row: Array[Double], index: Int ) =>
          context.actorOf(RowActor.props(Row(row.dropRight(1)), row.last, index), s"row-$index")
      })

  file.close()

  log.debug("Loaded {} rows", rowActors.length)

  var x: Array[Array[Double]] = Array.fill(iterations, rowActors.length)(Double.NaN)
  x(0) = Array.fill[Double](rowActors.length)(0)

  jacobi()

  override def receive: Receive = {

    case result: Result => {
      x(i + 1)(result.index) = result.x
      log.debug("Received x={} from row={}", result.x, result.index)

      if(!x(i + 1).exists(s => s.isNaN)) {
        i = i + 1
        log.debug("Iteration {}", i)
        if(i < x.length - 1 && x(i)
          .zip(x(i - 1))
          .map(z => Math.abs(z._1 - z._2))
          .exists(v => v > 0.001))
          jacobi()
        else {
          log.info(x(i).mkString("[", ", ", "]"))
          context.stop(self)
        }
      }
    }

  }

  private def jacobi(): Unit = {
    this.rowActors.foreach(_ ! XRequest(x(i), self))
  }
}

object MatrixActor {

  def props(filepath: String, iterations: Int = 100) = Props(new MatrixActor(filepath, iterations))

  case class Equation(A: Array[Row] = Array.empty[Row], B: Row = Row(Array.empty[Double]))
  case class Row (values: Array[Double])

  case class Result(x: Double, index: Int)

}

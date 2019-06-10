package me.kcybulski

import akka.actor.{ActorSystem, Props}
import me.kcybulski.gauss.GaussSeidelActor
import me.kcybulski.jacobi.JacobiActor

import scala.io.BufferedSource
import scala.io.Source.fromFile

object Start extends App {

  private val file: BufferedSource = fromFile(args(1))
  private val matrix = file
    .getLines
    .toArray
    .map(_.split("\\s+"))
    .map(_.map(d => d.toDouble))

  file.close()

  if(args(0).contains("gauss"))
    start(GaussSeidelActor.props(matrix))
  else if(args(0).contains("jacobi"))
    start(JacobiActor.props(matrix))
  else
    println("No method found!")

  def start(props: Props): Unit = {
    ActorSystem("prir").actorOf(props, "matrix")
  }

  case class Equation(A: Array[Row] = Array.empty[Row], B: Row = Row(Array.empty[Double]))
  case class Row (values: Array[Double])

}


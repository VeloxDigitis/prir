package me.kcybulski

import akka.actor.{ActorRef, ActorSystem}

object Start extends App {

  val system: ActorSystem = ActorSystem("prir")
  val matrixActor: ActorRef = system.actorOf(MatrixActor.props(args(0)), "matrix")

}

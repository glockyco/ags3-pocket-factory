package pocketfactory.actors

import akka.actor.FSM.{CurrentState, SubscribeTransitionCallBack, Transition}
import akka.actor.{Actor, Props}
import pocketfactory.domain.Registry

object LoggingActor {
  def props(registry: Registry): Props = Props(new LoggingActor(registry))
}

class LoggingActor(registry: Registry) extends Actor {

  registry.entries.values.foreach(_ ! SubscribeTransitionCallBack(self))

  override def receive: Receive = {
    case CurrentState(ref, state) => println(s"${ref.path.name}: $state")
    case Transition(ref, oldState, newState) => println(s"${ref.path.name}: $oldState -> $newState")
  }
}

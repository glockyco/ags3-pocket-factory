package pocketfactory.messages

import akka.actor.ActorRef
import pocketfactory.Machines.Machine

final case class RegistrationResponse(machine: Machine, ref: ActorRef)

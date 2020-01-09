package pocketfactory

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.scaladsl.StreamConverters
import akka.stream.{Materializer, SystemMaterializer}
import pocketfactory.Machines.Machine
import pocketfactory.actors.MainActor
import pocketfactory.actors.ui.{FactoryStateActor, OrderInputActor, OrderStateActor}
import pocketfactory.messages.OrderInputRequest
import scalafx.application.JFXApp
import scalafx.beans.property.StringProperty
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Button, TextField}
import scalafx.scene.layout.{GridPane, HBox, VBox}
import scalafx.scene.text.Text

import scala.concurrent.ExecutionContext

object Application extends JFXApp {

  val system = ActorSystem("system")
  val mainRef: ActorRef = system.actorOf(Props[MainActor], "main")

  implicit val executor: ExecutionContext = system.dispatcher
  implicit val mat: Materializer = SystemMaterializer(system).materializer

  val model: UiModel = new UiModel()

  val inputRef: ActorRef = system.actorOf(OrderInputActor.props(), "order-input")
  val factoryStateRef: ActorRef = system.actorOf(FactoryStateActor.props(model), "factory-state")
  val orderStateRef: ActorRef = system.actorOf(OrderStateActor.props(model), "order-state")

  StreamConverters.fromInputStream(() => System.in)
    .map(input => input.utf8String)
    .runForeach(string => inputRef ! OrderInputRequest(string))

  stage = new JFXApp.PrimaryStage {
    title.value = "Factory In A Box"

    width = 600
    height = 450

    scene = new Scene {
      content = new GridPane {
        padding = Insets(10)

        add(orderInput(), 0, 0)
        add(factoryState(), 0, 1)
        add(orderState(), 0, 2)
      }
    }
  }

  def orderInput(): HBox = {
    new HBox {
      margin = Insets(0, 0, 20, 0)

      children = Seq(
        new Text { margin = Insets(0, 10, 0, 0); text.value = "Order:" },
        new TextField { margin = Insets(0, 10, 0, 0); text <==> model.order },
        new Button {
          text.value = "Send"
          onAction = _ => inputRef ! OrderInputRequest(model.order.value)
        },
      )
    }
  }

  def factoryState(): GridPane = {
    new GridPane {
      add(machine(Machines.In, model.inStatus), 0, 1)
      add(machine(Machines.Out, model.outStatus), 3, 1)

      add(machine(Machines.C1, model.c1Status), 1, 1)
      add(machine(Machines.C2, model.c2Status), 2, 1)

      add(machine(Machines.P1, model.p1Status), 1, 0)
      add(machine(Machines.P2, model.p2Status), 1, 2)
      add(machine(Machines.P3, model.p3Status), 2, 0)
      add(machine(Machines.P4, model.p4Status), 2, 2)
    }
  }

  def machine(machine: Machine, status: StringProperty): VBox = {
    new VBox {
      prefWidth() = 150
      prefHeight() = 50

      children = Seq(
        new Text { text = machine.toString },
        new Text { text <==> status },
      )
    }
  }

  def orderState(): VBox = {
    new VBox {
      children = Seq(
        new Text { margin = Insets(0, 0, 10, 0); text.value = "Status:" },
        new Text { text <==> model.orderStatus },
      )
    }
  }
}

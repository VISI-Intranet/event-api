import Alpakka.Operations.RecieveMessageAlpakka
import Alpakka.RabbitMQModel.RabbitMQModel
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.stream.alpakka.amqp.{AmqpConnectionProvider, AmqpLocalConnectionProvider}
import route.EventRoutes

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("web-service")
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    implicit val amqpConnectionProvider: AmqpConnectionProvider = AmqpLocalConnectionProvider


    val routes = EventRoutes(amqpConnectionProvider).route

    val subMQModel: RabbitMQModel = RabbitMQModel("StudentPublisher", "", "")

    RecieveMessageAlpakka.subscription(subMQModel, amqpConnectionProvider)

    val bindingFuture = Http().bindAndHandle(routes, "localhost", 8086)

    println("Server is online at http://localhost:8081/\nPress RETURN to stop...")

    StdIn.readLine()

    bindingFuture
      .flatMap(_ => bindingFuture.flatMap(_.unbind()))
      .onComplete(_ => {
        system.terminate()
      })
  }
}
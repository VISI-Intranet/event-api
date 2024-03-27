package route

import Alpakka.Operations.SendMessageWithCorrelationIdAlpakka
import Alpakka.RabbitMQModel.RabbitMQModel
import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import akka.stream.alpakka.amqp.AmqpConnectionProvider
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import model.ModelEvent
import org.json4s.{DefaultFormats, jackson}
import repository.EventRepository

import scala.concurrent.ExecutionContext

class EventRoutes(amqpConnectionProvider: AmqpConnectionProvider)(implicit system: ActorSystem, mat: Materializer) extends Json4sSupport {
  implicit val serialization = jackson.Serialization
  implicit val formats = DefaultFormats
  implicit val ec: ExecutionContext = system.dispatcher

  val route =
    pathPrefix("event") {
      concat(
        pathEnd {
          concat(
            get {
              complete(EventRepository.getAllModelEvents())
            },
            post {
              entity(as[ModelEvent]) { event =>
                complete(EventRepository.addModelEvent(event))
              }
            }
          )
        },
        path(IntNumber) { eventId =>
          concat(
            get {
              complete(EventRepository.getEventById(eventId))
            },
            put {
              entity(as[ModelEvent]) { updatedEvent =>
                complete(EventRepository.updateModelEvent(eventId, updatedEvent))
              }
            },
            delete {
              complete(EventRepository.deleteModelEvent(eventId))
            }
          )
        }
      )
    }~
      pathPrefix("eventNotifiaction") {
    pathEnd {
      concat(
        get {
          complete(EventRepository.getAllModelEvents())
        },
        post {
          entity(as[ModelEvent]) { event =>
            val addEventResult = EventRepository.addModelEvent(event)


            val flagStudentNotification = true
            val flagTeacherNotification = true

            onSuccess(addEventResult) { result =>
              val eventMessage = s"Внимание в ${event.date} произойдет событие ${event.name_event} , успейте зарегистрироваться !!!"

              if(flagStudentNotification){
                val notificationEventToStudentMQModel: RabbitMQModel = RabbitMQModel("EventPublisher", "UniverSystem", "univer.event-api.notficationEventForStudentPost")
                SendMessageWithCorrelationIdAlpakka.sendMessageWithCorrelationId(eventMessage,notificationEventToStudentMQModel,amqpConnectionProvider)()
              }

              if(flagTeacherNotification){
                val notificationEventToTeacherMQModel: RabbitMQModel = RabbitMQModel("EventPublisher", "UniverSystem", "univer.event-api.notficationEventForTeacherPost")
                SendMessageWithCorrelationIdAlpakka.sendMessageWithCorrelationId(eventMessage,notificationEventToTeacherMQModel,amqpConnectionProvider)()

              }


              complete(StatusCodes.Created, result)
            }
          }
        }
      )

  }
}
}

object EventRoutes {
  def apply(amqpConnectionProvider: AmqpConnectionProvider)(implicit system: ActorSystem, mat: Materializer): EventRoutes = new EventRoutes(amqpConnectionProvider)
}

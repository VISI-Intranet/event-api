package routing


import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import Model._
import Model.JsonFormatss
import org.json4s.{DefaultFormats, jackson}
import repository._

object RouteEvent extends Json4sSupport {
  implicit val serialization = jackson.Serialization
  implicit val formats = JsonFormatss.formats

  val route =
    pathPrefix("Event") {
      concat(
        pathEnd {
          concat(
            get {
              complete(RepoEvent.getAll())
            },
            post {
              entity(as[ModelEvent]) { event =>
                complete(RepoEvent.insertData(event))
              }
            }
          )
        },
        path(Segment) { id_event =>
          concat(
            get {
              complete(RepoEvent.getById(id_event))
            },
            put {
              entity(as[ModelEvent]) { updatedEvent =>
                complete(RepoEvent.updateData(updatedEvent))
              }
            },
            delete {
              complete(RepoEvent.deleteData(id_event))
            }
          )
        }


      )
    }
}
package repository

import scala.concurrent.ExecutionContext
import model._
import connection.MongoDBConnection
import org.mongodb.scala.Document
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonInt32, BsonString}

import scala.concurrent.{ExecutionContext, Future}
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.Updates._
import scala.concurrent.Future
import org.mongodb.scala.model.Filters

object EventRepository {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global


  def getEventById(eventId: Int): Future[Option[ModelEvent]] = {
    val filter = Filters.eq("id_event", eventId)
    val futureEvent = MongoDBConnection.eventCollection.find(filter).headOption()

    futureEvent.map {
      case Some(doc) =>
        Some(
          ModelEvent(
            _id = doc.getInteger("id_event"),
            name_event = doc.getString("name_event"),
            content_event = doc.getString("content_event"),
            comments = doc.getInteger("comments"),
            golos = doc.getInteger("golos"),
            location = doc.getString("location"),
            date = doc.getString("date"),
            creator = doc.getString("creator"),
            auditorium = doc.getString("auditorium")
          )
        )
      case None => None
    }
  }


  def getAllModelEvents(): Future[List[ModelEvent]] = {
    val futureEvents = MongoDBConnection.eventCollection.find().toFuture()

    futureEvents.map { docs =>
      docs.map { doc =>
        ModelEvent(
          _id = doc.getInteger("_id"),
          name_event = doc.getString("name_event"),
          content_event = doc.getString("content_event"),
          comments = doc.getInteger("comments"),
          golos = doc.getInteger("golos"),
          location = doc.getString("location"),
          date = doc.getString("date"),
          creator = doc.getString("creator"),
          auditorium = doc.getString("auditorium")
        )
      }.toList
    }
  }

  def addModelEvent(event: ModelEvent): Future[String] = {
    val eventDocument = BsonDocument(
      "_id" -> BsonInt32(event._id),
      "name_event" -> BsonString(event.name_event),
      "content_event" -> BsonString(event.content_event),
      "comments" -> BsonInt32(event.comments),
      "golos" -> BsonInt32(event.golos),
      "location" -> BsonString(event.location),
      "date" -> BsonString(event.date),
      "creator" -> BsonString(event.creator),
      "auditorium" -> BsonString(event.auditorium)
    )

    MongoDBConnection.eventCollection.insertOne(eventDocument).toFuture().map(_ => s"Модельное событие с id_event=${event._id} было успешно добавлено в базу данных.")
  }


  def deleteModelEvent(eventId: Int): Future[String] = {
    val eventFilter = Filters.eq("_id", eventId)
    MongoDBConnection.eventCollection.deleteOne(eventFilter).toFuture().map(_ => s"Модельное событие с id_event=$eventId было успешно удалено из базы данных.")
  }

  def updateModelEvent(eventId: Int, updatedEvent: ModelEvent): Future[String] = {
    val filter = Document("_id" -> eventId)

    val eventDocument = BsonDocument(
      "$set" -> BsonDocument(
        "name_event" -> BsonString(updatedEvent.name_event),
        "content_event" -> BsonString(updatedEvent.content_event),
        "comments" -> BsonInt32(updatedEvent.comments),
        "golos" -> BsonInt32(updatedEvent.golos),
        "location" -> BsonString(updatedEvent.location),
        "date" -> BsonString(updatedEvent.date),
        "creator" -> BsonString(updatedEvent.creator),
        "auditorium" -> BsonString(updatedEvent.auditorium)
      )
    )

    MongoDBConnection.eventCollection.updateOne(filter, eventDocument).toFuture().map { updateResult =>
      if (updateResult.wasAcknowledged() && updateResult.getModifiedCount > 0) {
        s"Модельное событие с id_event=$eventId было успешно обновлено в базе данных."
      } else {
        "Обновление модельного события не выполнено: возможно, запись с указанным id_event не найдена."
      }
    }
  }

}

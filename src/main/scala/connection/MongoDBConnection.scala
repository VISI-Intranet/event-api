package connection

import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}

object MongoDBConnection {

  private val mongoClient = MongoClient("mongodb://localhost:27017")
  val database: MongoDatabase = mongoClient.getDatabase("MicroserviceSystemDB")
  val eventCollection: MongoCollection[Document] = database.getCollection("Event")

}

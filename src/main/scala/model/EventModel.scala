package model

case class ModelEvent(_id:Int,
                      name_event:String,
                      content_event:String,
                      comments:Int,
                      golos:Int,
                      location:String,
                      date:String,
                      creator:String,
                      auditorium: String
                     )

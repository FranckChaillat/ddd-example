package com.example.ddd.infra.repositories

import com.example.ddd.domain.entities.TaskEvent
import com.example.ddd.domain.repositories.EventsRepository
import com.example.ddd.domain.entities._
import com.roundeights.hasher.Implicits._

import java.time.Instant
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

class EventsRepositoryImpl extends EventsRepository {

  val events: ListBuffer[TaskEvent] = ListBuffer(
    StatusChange(
      eventDate = Instant.ofEpochMilli(1677225773302L),
      taskId = "main task boardImplement feature A".sha1.hex,
      user = "JohnDoe",
      fromStatus = TaskStatus.TODO,
      toStatus = TaskStatus.DOING
    ),
    StatusChange(
      eventDate = Instant.ofEpochMilli(1677298993302L),
      taskId = "main task boardImplement feature A".sha1.hex,
      user = "JohnDoe",
      fromStatus = TaskStatus.DOING,
      toStatus = TaskStatus.TEST
    ),
    StatusChange(
      eventDate = Instant.ofEpochMilli(1677398993302L),
      taskId = "main task boardImplement feature A".sha1.hex,
      user = "KristerOwe",
      fromStatus = TaskStatus.TEST,
      toStatus = TaskStatus.DONE
    )
  )

  override def insert(item: TaskEvent): Future[String] = {
    events.addOne(item)
    Future.successful(item.eventId)
  }

  override def get(eventId: String): Future[TaskEvent] =
    events.find(x => x.eventId == eventId) match {
      case Some(value) => Future.successful(value)
      case None => Future.failed(new NoSuchElementException("Event not found."))
    }

  override def getEventsForTask(taskId: String): Future[List[TaskEvent]] =
    Future.successful(events.filter(_.taskId == taskId).toList)

}

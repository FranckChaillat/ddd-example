package com.example.ddd.domain.repositories

import com.example.ddd.domain.entities.TaskEvent

import scala.concurrent.Future

trait EventsRepository {
  def getEventsForTask(taskId: String): Future[List[TaskEvent]]

  def insert(item: TaskEvent): Future[String]

  def get(eventId: String): Future[TaskEvent]
}

package com.example.ddd.domain.repositories

import com.example.ddd.domain.entities.{Task, TaskBoard}

import scala.concurrent.Future


trait TaskBoardRepository {
  def upsertTask(task: Task): Future[String]

  def insert(item: TaskBoard): Future[String]

  def get(taskBoardId: String): Future[TaskBoard]

  def delete(itemId: String): Future[Unit]
}

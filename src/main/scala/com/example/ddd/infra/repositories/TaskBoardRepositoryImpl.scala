package com.example.ddd.infra.repositories

import com.example.ddd.domain.entities.{Task, TaskBoard, TaskStatus}
import com.example.ddd.domain.repositories.TaskBoardRepository

import scala.concurrent.ExecutionContext.Implicits.global
import java.util.UUID
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.util.chaining._

class TaskBoardRepositoryImpl extends TaskBoardRepository {

  private val task1 = Task.createTask(
    "Implement feature A",
    "main task board",
    "As a PO i want the feature A to be implemented",
    Some("MEDIUM"),
    Some(3),
    List("JohnDoe")
  )

  private val task2 = Task.createTask(
    UUID.randomUUID().toString,
    "Implement feature B",
    "as a PO i want the feature B to be implemented",
    Some("HIGH"),
    Some(2),
    List("JamesConnor")
  )

  private val task3 = Task.createTask(
    UUID.randomUUID().toString,
    "TI automation",
    "as a PO i would like to automate integration tests on the solution.",
    Some("HIGH"),
    Some(5),
    List("KristerOwe")
  )

  private val taskBoardList = ListBuffer(
    TaskBoard(
      "main task board", List(task1, task2, task3), List("JohnDoe", "JamesConnor", "KristerOwe")
    )
  )

  def upsertTask(task: Task): Future[String] = {
    val tbIdx = taskBoardList.indexWhere(_.id == task.taskBoard)
    if(tbIdx < 0)
      Future.failed(new NoSuchElementException("No such taskboard found"))
    else Future {
      val tb = taskBoardList(tbIdx)
      val taskIdx = taskBoardList(tbIdx).tasks.indexWhere(t => t.id == task.id)
      val tbUpdated = if(taskIdx < 0)
        taskBoardList(tbIdx).copy(tasks = tb.tasks.prepended(task))
      else
        taskBoardList(tbIdx).copy(tasks = tb.tasks.updated(taskIdx, task))

      taskBoardList.update(tbIdx, tbUpdated)
        .pipe(_ => task.id)
    }
  }

  override def insert(item: TaskBoard): Future[String] = ???

  override def get(taskBoardId: String): Future[TaskBoard] =
    taskBoardList.find(tb => tb.id == taskBoardId) match {
      case Some(value) => Future.successful(value)
      case None => Future.failed(new NoSuchElementException("No such taskboard found"))
    }

  override def delete(itemId: String): Future[Unit] = ???

}

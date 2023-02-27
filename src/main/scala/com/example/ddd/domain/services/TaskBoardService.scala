package com.example.ddd.domain.services

import com.example.ddd.domain.entities.{Task, TaskBoard, TaskDependency}
import com.example.ddd.domain.entities.exceptions.{NotifyException, TaskBoardNotFoundException, TaskNotFoundException}
import com.example.ddd.domain.repositories.TaskBoardRepository
import com.example.ddd.domain.services.notifications.{NotificationType, Notifier}

import scala.concurrent.{ExecutionContext, Future}


class TaskBoardService(repository: TaskBoardRepository,
                       notifier: Notifier)(implicit ec: ExecutionContext) {

  def addTask(task: Task): Future[String] =
    for {
      _  <- repository
              .upsertTask(task)
              .recoverWith {
                case _: NoSuchElementException => Future.failed(TaskBoardNotFoundException(taskBoardId = task.taskBoard))
                case failure                   => Future.failed(failure)
              }
      _  <- notifyAssigned(NotificationType.ASSIGNMENT)(task.assignment)
    } yield task.id

  def getTask(taskBoardId: String, taskId: String): Future[Task] =
    repository.get(taskBoardId)
      .flatMap { x =>
         x.tasks.find(_.id == taskId) match {
           case Some(value) => Future.successful(value)
           case None        => Future.failed(TaskNotFoundException(taskId))

         }
      }

  def assign(taskBoardId: String, taskId: String, userName: String): Future[Unit] =
    repository.get(taskBoardId)
      .map { tb =>
        val updatedTask = tb.tasks.collectFirst {
          case task: Task if task.id == taskId =>
            task.copy(assignment = task.assignment.prepended(userName))
        }
        updatedTask match {
          case Some(value) =>
            repository.upsertTask(value)
            notifyAssigned(NotificationType.ASSIGNMENT)(List(userName))
          case None =>
            Future.failed(TaskNotFoundException(taskId))
        }
      }

  def getTaskDependencies(taskBoardId: String, taskId: String): Future[TaskDependency] = {
    val eventualTaskDependency = for {
      taskBoard <- repository.get(taskBoardId)
      if taskBoard.tasks.exists(_.id == taskId)
      res       <- taskBoard.tasks
        .find(_.id == taskId)
        .map(t => getDependencies(t.title, taskBoard))
        .fold(Future.failed[TaskDependency](TaskNotFoundException(taskId)))(Future.successful)
    } yield res

    eventualTaskDependency
      .recoverWith {
        case _: NoSuchElementException =>
          Future.failed(TaskNotFoundException(taskId))
      }
  }

  private def getDependencies(taskTitle: String, taskBoard: TaskBoard): TaskDependency = {
    val indexed = taskBoard.tasks.map(t => t.title -> t).toMap
    def collect(currentTask: String, previousTasks: Set[String]): List[TaskDependency] =
        indexed(currentTask)
          .dependsOnTask
          .collect {
            case d if !previousTasks.contains(d) =>
              TaskDependency(d, collect(d, previousTasks.+(currentTask)))
          }

    TaskDependency(taskTitle = taskTitle, dependentOn = collect(taskTitle, Set.empty))
  }

  private def notifyAssigned(notificationType: NotificationType.Value)(users: List[String]) = {
    Future.sequence {
      users.map(u => notifier.notify(u, NotificationType.getMessageFromValue(notificationType)))
    }
    .map(_ => ())
    .recoverWith {
      case t: Throwable =>
        Future.failed { NotifyException(notificationType, users.mkString(";"), t.getMessage) }
    }
  }
}

package com.example.ddd.domain.services

import com.example.ddd.domain.entities.exceptions.{NoStatisticsAvailableException, StatisticsException}
import com.example.ddd.domain.entities.{Creation, StatusChange, Task, TaskStatus}
import com.example.ddd.domain.repositories.{EventsRepository, TaskBoardRepository}

import java.time.temporal.ChronoUnit
import scala.concurrent.{ExecutionContext, Future}

class StatisticService(eventsRepository: EventsRepository,
                       taskBoardRepository: TaskBoardRepository
                      )(implicit ec: ExecutionContext) {

  def getMeanCycleTime(taskBoardId: String, tasksIds: Set[String] = Set()): Future[Long] = {
    val eventualMeanCycleTime = for {
      tb            <- taskBoardRepository.get(taskBoardId)
      filteredTasks = if (tasksIds.isEmpty) tb.tasks else tb.tasks.filter(t => tasksIds.contains(t.id))
      tasksEvents   <- Future.sequence {
        filteredTasks.collect { case t: Task if t.status == TaskStatus.DONE => getCycleTimeForTask(t.id) }
      }
      if tasksEvents.nonEmpty
    } yield
      tasksEvents.sum / tasksEvents.length

    eventualMeanCycleTime.recoverWith {
      case _: NoSuchElementException =>
        Future.failed(NoStatisticsAvailableException(taskBoardId))
    }
  }

  private def getCycleTimeForTask(task: String) =
    eventsRepository.getEventsForTask(task)
      .flatMap { events =>
        val relevantEvents = events.sortBy(_.eventDate)
                .collect {
                  case e: StatusChange if e.toStatus == TaskStatus.DONE => e
                  case e: Creation => e
                }

        lazy val err: Future[Long] = Future.failed(StatisticsException(s"Cycle time computation error for task $task, events list malformed or inconsistent."))
        if (relevantEvents.length > 1) {
          val cycleTimeOpt = for {
            createDate <- relevantEvents.headOption.map(_.eventDate)
            doneDate   <- relevantEvents.tail.reverse
                            .collectFirst { case e: StatusChange => e.eventDate }
          } yield
            ChronoUnit.SECONDS.between(createDate, doneDate)
          cycleTimeOpt.fold(err)(Future.successful)
        } else
          err
      }
}

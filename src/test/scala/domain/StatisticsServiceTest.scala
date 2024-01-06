package domain

import com.example.ddd.domain.entities
import com.example.ddd.domain.entities._
import com.example.ddd.domain.entities.exceptions.NoStatisticsAvailableException
import com.example.ddd.domain.repositories.{EventsRepository, TaskBoardRepository}
import com.example.ddd.domain.services.StatisticService
import com.example.ddd.infra.repositories.{EventsRepositoryImpl, TaskBoardRepositoryImpl}
import com.roundeights.hasher.Implicits._
import org.mockito.MockitoSugar.{mock, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.{a, convertToAnyShouldWrapper}

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class StatisticsServiceTest extends AnyFlatSpec with ScalaFutures {

  private val fixtures = new {
    val taskList: Map[String, Task] = Map(
      "A" -> Task.createTask("A", "dummyTaskBoard", "", None, Some(3), List.empty)
        .copy(status = TaskStatus.DONE),
      "B" -> Task.createTask("B", "dummyTaskBoard", "", None, Some(5), List.empty)
        .copy(status = TaskStatus.DONE),
      "C" -> Task.createTask("C", "dummyTaskBoard", "", None, Some(1), List.empty)
        .copy(status = TaskStatus.DOING),
      "D" -> Task.createTask("D", "dummyTaskBoard", "", None, Some(8), List.empty)
             .copy(status = TaskStatus.TODO)
    )

    println("test")

    val taskAEventList: List[entities.TaskEvent] = List(
      Creation(Instant.ofEpochMilli(1677298993302L), "dummyTaskBoardA".sha1.hex, "Francky"),
      StatusChange(
        Instant.ofEpochMilli(1677298993302L)
          .plus(2, ChronoUnit.HOURS),
        "dummyTaskBoardA".sha1.hex,
        "Francky",
        TaskStatus.TODO,
        TaskStatus.DOING
      ),
      StatusChange(
        Instant.ofEpochMilli(1677298993302L)
          .plus(8, ChronoUnit.HOURS),
        "dummyTaskBoardA".sha1.hex,
        "Francky",
        TaskStatus.DOING,
        TaskStatus.TEST
      ),
      StatusChange(
        Instant.ofEpochMilli(1677298993302L)
          .plus(25, ChronoUnit.HOURS),
        "dummyTaskBoardA".sha1.hex,
        "Francky",
        TaskStatus.TEST,
        TaskStatus.DONE
      )
    )

    val taskBEventList: List[entities.TaskEvent] = List(
      Creation(Instant.ofEpochMilli(1677488281231L), "dummyTaskBoardB".sha1.hex, "Francky"),
      StatusChange(
        Instant.ofEpochMilli(1677488281231L)
          .plus(2, ChronoUnit.HOURS),
        "dummyTaskBoardB".sha1.hex,
        "Francky",
        TaskStatus.TODO,
        TaskStatus.DOING
      ),
      StatusChange(
        Instant.ofEpochMilli(1677488281231L)
          .plus(3, ChronoUnit.DAYS),
        "dummyTaskBoardB".sha1.hex,
        "Francky",
        TaskStatus.DOING,
        TaskStatus.TEST
      ),
      StatusChange(
        Instant.ofEpochMilli(1677488281231L)
          .plus(4, ChronoUnit.DAYS),
        "dummyTaskBoardB".sha1.hex,
        "Francky",
        TaskStatus.TEST,
        TaskStatus.DONE
      )
    )

    val eventsRepo: EventsRepository = mock[EventsRepositoryImpl]
    val taskBoardRepo: TaskBoardRepository = mock[TaskBoardRepositoryImpl]

  }

  behavior of "Statistics computation on the taskboard"

  "Statistics computation" should "compute the mean cycle time for only one task" in {
    import fixtures._

    val testTaskBoard: TaskBoard = TaskBoard(
      "dummyTaskBoard",
      List(fixtures.taskList("A")),
      List()
    )

    when(taskBoardRepo.get("dummyTaskBoard"))
      .thenReturn(Future.successful(testTaskBoard))

    when(eventsRepo.getEventsForTask("dummyTaskBoardA".sha1.hex))
      .thenReturn(Future.successful(taskAEventList))

    val service = new StatisticService(fixtures.eventsRepo, fixtures.taskBoardRepo)
    whenReady(service.getMeanCycleTime("dummyTaskBoard")) { result =>
      result shouldBe 90000
    }
  }

  "Statistics computation" should "compute the mean cycle time for two finished task and one still in progress" in {
    import fixtures._

    val taskBoard: TaskBoard = TaskBoard(
      "dummyTaskBoard",
      List(taskList("A"), taskList("B"), taskList("C")),
      List()
    )

    when(taskBoardRepo.get("dummyTaskBoard"))
      .thenReturn(Future.successful(taskBoard))

    when(eventsRepo.getEventsForTask("dummyTaskBoardA".sha1.hex))
      .thenReturn(Future.successful(taskAEventList))

    when(eventsRepo.getEventsForTask("dummyTaskBoardB".sha1.hex))
      .thenReturn(Future.successful(taskBEventList))

    val service = new StatisticService(fixtures.eventsRepo, fixtures.taskBoardRepo)
    whenReady(service.getMeanCycleTime("dummyTaskBoard")) { result =>
      result shouldBe 217800
    }
  }

  "Statistics computation" should "return a custom error if the metric can't be computed" in {

    import fixtures._

    val taskBoard: TaskBoard = TaskBoard(
      "dummyTaskBoard",
      List(taskList("D")),
      List()
    )

    when(taskBoardRepo.get("dummyTaskBoard"))
      .thenReturn(Future.successful(taskBoard))

    when(eventsRepo.getEventsForTask("dummyTaskBoardA".sha1.hex))
      .thenReturn(Future.successful(List.empty))

    val service = new StatisticService(fixtures.eventsRepo, fixtures.taskBoardRepo)
    whenReady(service.getMeanCycleTime("dummyTaskBoard").failed) { e =>
      e shouldBe a [NoStatisticsAvailableException]
    }
  }

  "Statistics computation" should "compute the burndown chart" in {
    fail("not implemented")
  }

}

package domain

import com.example.ddd.domain.entities.{Task, TaskBoard}
import com.example.ddd.domain.entities.exceptions.TaskBoardNotFoundException
import com.example.ddd.domain.repositories.TaskBoardRepository
import com.example.ddd.domain.services.TaskBoardService
import com.example.ddd.domain.services.notifications.Notifier
import com.example.ddd.infra.repositories.TaskBoardRepositoryImpl
import com.roundeights.hasher.Implicits._
import org.mockito.MockitoSugar.{mock, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec._
import org.scalatest.matchers.should._

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.Success

class TodoListServiceTest extends AnyFlatSpec with Matchers with ScalaFutures {

  private val fixtures = new {
    val repository = new TaskBoardRepositoryImpl()
    val testData = mutable.Map.empty[String, String]
    val dummyNotifier: Notifier = (recipient: String, messageContent: String) =>
      Future.successful(testData.addOne(recipient -> messageContent))
  }

  behavior of "Appending a task to a board"

  "Append action" should "add the task to the board if it doesn't exist" in {
    val service = new TaskBoardService(fixtures.repository, fixtures.dummyNotifier)
    val addedTask = Task.createTask("Implement feature C", "main task board", "as a PO i want the feature C to be implemented", Some("HIGH"), None, List("JamesConnor"))
    whenReady(service.addTask(addedTask)) { result =>
      result shouldBe s"${addedTask.taskBoard}${addedTask.title}".sha1.hex
      service.getTask("main task board", result)
        .onComplete {
          case Success(_) => succeed
          case _ => fail()
        }
      fixtures.testData.contains("JamesConnor") shouldBe true
    }
  }

  "Append action" should "not add a task if the board doesn't exists" in {
    val service = new TaskBoardService(fixtures.repository, fixtures.dummyNotifier)
    val addedTask = Task.createTask("Implement feature C", "a non existing task board", "as a PO i want the feature C to be implemented", Some("HIGH"), None, List("JamesConnor"))
    whenReady(service.addTask(addedTask).failed) {
      case _: TaskBoardNotFoundException => succeed
      case _ => fail("Task appending failed with wrong error.")
    }
  }

  "Assignment " should "assign a user to the task and notify him" in {
    val service = new TaskBoardService(fixtures.repository, fixtures.dummyNotifier)
    val expectedTaskId = "main task boardImplement feature A".sha1.hex
    Await.ready(service.assign("main task board", expectedTaskId, "KristerOwe"), 3.seconds)
    whenReady(service.getTask("main task board", expectedTaskId)) { result =>
      fixtures.testData.contains("KristerOwe") shouldBe true
    }
  }

  "Dependencies retrieval" should "get the only dependency" in {
    val repositoryMock = mock[TaskBoardRepository]
    when(repositoryMock.get("dummyTaskBoard")).thenReturn {
      val taskList = List(
        Task.createTask("A", "dummyTaskBoard", "", None, Some(3), List.empty)
          .copy(dependsOnTask = List("B")),
        Task.createTask("B", "dummyTaskBoard", "", None, Some(5), List.empty)
      )
      Future.successful(TaskBoard(
        "main task board", taskList, List("JohnDoe", "JamesConnor", "KristerOwe")
      ))
    }

    val service = new TaskBoardService(repositoryMock, fixtures.dummyNotifier)
    whenReady(service.getTaskDependencies("dummyTaskBoard", "4371234ac02863ecd193a81267ea9ff0e9645de2")) { result =>
      result.taskTitle shouldBe "A"
      result.dependentOn.length shouldBe 1
      result.dependentOn.head.taskTitle shouldBe "B"
    }
  }

  "Dependencies retrieval" should "get the dependency on three levels (linear)" in {
    val repositoryMock = mock[TaskBoardRepository]
    when(repositoryMock.get("dummyTaskBoard")).thenReturn {
      val taskList = List(
        Task.createTask("A", "dummyTaskBoard", "", None, Some(3), List.empty)
          .copy(dependsOnTask = List("B")),
        Task.createTask("B", "dummyTaskBoard", "", None, Some(5), List.empty)
          .copy(dependsOnTask = List("C")),
        Task.createTask("C", "dummyTaskBoard", "", None, Some(1), List.empty)
      )
      Future.successful(TaskBoard(
        "main task board", taskList, List("JohnDoe", "JamesConnor", "KristerOwe")
      ))
    }

    val service = new TaskBoardService(repositoryMock, fixtures.dummyNotifier)
    whenReady(service.getTaskDependencies("dummyTaskBoard", "dummyTaskBoardA".sha1.hex)) { result =>
      result.taskTitle shouldBe "A"
      result.dependentOn.length shouldBe 1
      result.dependentOn.head.taskTitle shouldBe "B"
      val taskBDependencies = result.dependentOn.head.dependentOn
      taskBDependencies.length shouldBe(1)
      taskBDependencies.head.taskTitle shouldBe "C"
    }
  }

  "Dependencies retrieval" should "get the two dependencies of the task" in {
    val repositoryMock = mock[TaskBoardRepository]
    when(repositoryMock.get("dummyTaskBoard")).thenReturn {
      val taskList = List(
        Task.createTask("A", "dummyTaskBoard", "", None, Some(3), List.empty)
          .copy(dependsOnTask = List("B", "C")),
        Task.createTask("B", "dummyTaskBoard", "", None, Some(5), List.empty)
          .copy(dependsOnTask = List("D")),
        Task.createTask("C", "dummyTaskBoard", "", None, Some(1), List.empty),
        Task.createTask("D", "dummyTaskBoard", "", None, Some(2), List.empty),

      )
      Future.successful(TaskBoard(
        "main task board", taskList, List("JohnDoe", "JamesConnor", "KristerOwe")
      ))
    }

    val service = new TaskBoardService(repositoryMock, fixtures.dummyNotifier)
    whenReady(service.getTaskDependencies("dummyTaskBoard", "dummyTaskBoardA".sha1.hex)) { result =>
      result.taskTitle shouldBe "A"
      result.dependentOn.length shouldBe 2

      val fstDep = result.dependentOn.head
      fstDep.taskTitle shouldBe "B"
      fstDep.dependentOn.headOption.exists(_.taskTitle == "D") shouldBe true

      val sndDep = result.dependentOn(1)
      sndDep.taskTitle shouldBe "C"
      sndDep.dependentOn.isEmpty shouldBe true
    }
  }

  "Dependencies retrieval" should "not break in case of circular dependencies" in {
    val repositoryMock = mock[TaskBoardRepository]
    when(repositoryMock.get("dummyTaskBoard")).thenReturn {
      val taskList = List(
        Task.createTask("A", "dummyTaskBoard", "", None, Some(3), List.empty)
          .copy(dependsOnTask = List("B", "C")),
        Task.createTask("B", "dummyTaskBoard", "", None, Some(5), List.empty)
          .copy(dependsOnTask = List("D")),
        Task.createTask("C", "dummyTaskBoard", "", None, Some(1), List.empty),
        Task.createTask("D", "dummyTaskBoard", "", None, Some(2), List.empty)
        .copy(dependsOnTask = List("A")),
      )

      Future.successful(TaskBoard(
        "main task board", taskList, List("JohnDoe", "JamesConnor", "KristerOwe")
      ))
    }

    val service = new TaskBoardService(repositoryMock, fixtures.dummyNotifier)
    whenReady(service.getTaskDependencies("dummyTaskBoard", "dummyTaskBoardA".sha1.hex)) { result =>
      result.taskTitle shouldBe "A"
      result.dependentOn.length shouldBe 2

      val fstDep = result.dependentOn.head
      fstDep.taskTitle shouldBe "B"
      fstDep.dependentOn.headOption.exists(_.taskTitle == "D") shouldBe true

      fstDep.dependentOn.head.dependentOn.isEmpty shouldBe true

      val sndDep = result.dependentOn(1)
      sndDep.taskTitle shouldBe "C"
      sndDep.dependentOn.isEmpty shouldBe true
    }
  }
}

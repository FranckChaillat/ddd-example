package com.example.ddd.domain.entities
import com.roundeights.hasher.Implicits._


final case class Task private (
                      id: String,
                      taskBoard: String,
                      title: String,
                      description: String,
                      priority: Option[String],
                      status: TaskStatus.Value,
                      score: Option[Int],
                      assignment: List[String],
                      dependsOnTask: List[String],
                      sprintId: Option[Int])


object Task {
  def createTask(title: String, taskBoard: String, description: String, priority: Option[String], score: Option[Int], assignment: List[String]): Task = {
    Task(
      id = s"$taskBoard$title".sha1.hex,
      taskBoard = taskBoard,
      title = title,
      description = description,
      priority = priority,
      status = TaskStatus.TODO,
      score = score,
      assignment = assignment,
      dependsOnTask = List.empty,
      sprintId = None)
  }
}
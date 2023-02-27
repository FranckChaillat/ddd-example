package com.example.ddd.domain.entities.exceptions

final case class TaskBoardNotFoundException(taskBoardId: String) extends Throwable(s"The task board $taskBoardId hasn't been found.")

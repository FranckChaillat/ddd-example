package com.example.ddd.domain.entities.exceptions

final case class TaskNotFoundException(taskId: String) extends Throwable(s"Task $taskId hasn't been found.")
package com.example.ddd.domain.entities.exceptions

final case class NoStatisticsAvailableException(taskBoardId: String) extends Throwable(s"The required statistics cannot be computed (task board $taskBoardId")

package com.example.ddd.domain.entities

final case class TaskDependency(taskTitle: String,
                                dependentOn: List[TaskDependency])

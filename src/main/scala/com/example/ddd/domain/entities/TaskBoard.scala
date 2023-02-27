package com.example.ddd.domain.entities

final case class TaskBoard(id: String, tasks: List[Task], users: List[String])

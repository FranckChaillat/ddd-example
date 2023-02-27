package com.example.ddd.domain.entities

import java.time.Instant
import com.roundeights.hasher.Implicits._


trait TaskEvent {
  val eventDate: Instant
  val taskId: String
  val user: String

  def eventId: String = s"${eventDate.toEpochMilli}$taskId".sha1.hex
}

final case class StatusChange(eventDate: Instant,
                              taskId: String,
                              user: String,
                              fromStatus: TaskStatus.Value,
                              toStatus: TaskStatus.Value) extends TaskEvent

final case class Creation(eventDate: Instant,
                          taskId: String,
                          user: String) extends TaskEvent


final case class Archive(eventDate: Instant,
                         taskId: String,
                         user: String) extends TaskEvent
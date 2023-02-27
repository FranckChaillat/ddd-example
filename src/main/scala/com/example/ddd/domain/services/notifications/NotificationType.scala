package com.example.ddd.domain.services.notifications

object NotificationType extends Enumeration {
  val ASSIGNMENT = Value

  def getMessageFromValue(notification: NotificationType.Value): String = {
    notification match {
      case ASSIGNMENT => "A task has been assigned to you"
    }
  }
}

package com.example.ddd.domain.entities.exceptions

import com.example.ddd.domain.services.notifications.NotificationType

final case class NotifyException(notificationType: NotificationType.Value, recipient: String, errorMessage: String)
  extends Throwable(s"An error occurred while trying to notify recipient $recipient with ${notificationType.toString}; $errorMessage")

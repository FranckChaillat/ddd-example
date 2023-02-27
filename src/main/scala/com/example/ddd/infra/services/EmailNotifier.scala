package com.example.ddd.infra.services

import com.example.ddd.domain.services.notifications.Notifier

import scala.concurrent.Future

class EmailNotifier extends Notifier {
  override def notify(recipient: String, messageContent: String): Future[Unit] = {
    // fake implementation of email sending logic
    Future.successful(println(s"Sending email content: $messageContent to recipient $recipient."))
  }
}

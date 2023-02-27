package com.example.ddd.domain.services.notifications

import scala.concurrent.Future

trait Notifier {
  def notify(recipient: String, messageContent: String): Future[Unit]
}

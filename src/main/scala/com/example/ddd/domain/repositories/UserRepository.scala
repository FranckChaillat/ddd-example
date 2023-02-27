package com.example.ddd.domain.repositories

import com.example.ddd.domain.entities.User

import scala.concurrent.Future

trait UserRepository {

  def insert(item: User): Future[String]

  def get(itemId: String): Future[User]

  def delete(itemId: String): Future[Unit]
}

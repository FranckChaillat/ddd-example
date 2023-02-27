package com.example.ddd.infra.repositories

import com.example.ddd.domain.entities.User
import com.example.ddd.domain.repositories.UserRepository

import scala.concurrent.Future

class UserRepositoryImpl extends UserRepository {

  private val users: List[User] = List(
    User("JohnDoe", "Architect"),
    User("JamesConnor", "Developer"),
    User("KristerOwe", "QA")
  )

  override def insert(item: User): Future[String] = ???

  override def get(itemId: String): Future[User] = ???

  override def delete(itemId: String): Future[Unit] = ???
}

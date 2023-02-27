package com.example.ddd.domain.services

import com.example.ddd.domain.entities.User
import com.example.ddd.domain.repositories.UserRepository

class UserService(userRepository: UserRepository) {
  def createUser(user: User) {
    ???
  }
}

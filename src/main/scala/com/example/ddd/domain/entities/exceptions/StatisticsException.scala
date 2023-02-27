package com.example.ddd.domain.entities.exceptions

final case class StatisticsException(message: String) extends Throwable(message)

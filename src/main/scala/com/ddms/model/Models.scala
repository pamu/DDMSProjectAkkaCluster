package com.ddms.model

object Models {
  case class User(username: String, id: Option[Long] = None)
}
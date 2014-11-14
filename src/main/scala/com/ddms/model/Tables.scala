package com.ddms.model

import scala.slick.driver.MySQLDriver.simple._

object Tables {
  import Models.User
  
  class Users(tag: Tag) extends Table[User](tag, "USERS") {
    def id = column[Long]("ID", O.NotNull,O.PrimaryKey, O.AutoInc)
    def username = column[String]("USER_NAME", O.NotNull)
    def * = (username, id.?) <> (User.tupled, User.unapply)
  }
  
  
}
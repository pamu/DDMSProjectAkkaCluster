package com.ddms.model

import Tables._
import Models._
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta.MTable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

object DAO {
  
  lazy val db1 = Database.forURL(url = "jdbc:mysql://localhost/db1", driver = "com.mysql.jdbc.Driver", user = "root", password = "root")
  
  lazy val db2 = Database.forURL(url = "jdbc:mysql://localhost/db2", driver = "com.mysql.jdbc.Driver", user = "root", password = "root")
  
  //lazy val db3 = Database.forURL(url = "jdbc:mysql://localhost/db3", driver = "com.mysql.jdbc.Driver", user = "root", password = "root")
  
  //lazy val db4 = Database.forURL(url = "jdbc:mysql://localhost/db4", driver = "com.mysql.jdbc.Driver", user = "root", password = "root")
  
  val users = TableQuery[Users]
  
  implicit class DB(name: String) {
    def db: Database = name match {
      case "1" => db1
      case "2" => db2
      //case "3" => db3
      //case "4" => db4
    }
  }
  
  def toF[T](name: String)(f: Session => T): Future[T] = {
    name.db.withSession{
      session: Session => Future(f(session))
    }
  }
  
  def createF(name: String) = toF[Boolean](name){ implicit session: Session => {
	  if(MTable.getTables("USERS").list.isEmpty) { 
	    users.ddl.create;
	    true
	  }else false
  	}
}
  
  def insertF(name: String, user: User) = toF[Long](name) {implicit session => {
    users.returning(users.map(_.id)).insert(user)
  }}
  
  def getF(name: String, id: Long) = toF[Option[User]](name) { implicit session => {
	  val q = for(user <- users.filter(_.id === id)) yield user
	  q.firstOption
  	}
  }
  
  def deleteF(name: String, id: Long) = toF[Int](name) { implicit session => {
	   val q = for(user <- users.filter(_.id === id)) yield user
	   q.delete
  	}
  }
  
  def create(name: String): Boolean = name.db.withSession { implicit session => {
	  if(MTable.getTables("USERS").list.isEmpty) { 
	    users.ddl.create
	    true
	  }else false
  	}
  }
  
  def createTableF(name: String, table: TableQuery[_ <: Table[_]]) = toF[Unit](name) { implicit session => {
	  table.ddl.create
  	}
  }
  
  def insert(name: String, user: User): Long = name.db.withSession { implicit session: Session => {
	  users.returning(users.map(_.id)).insert(user)
  	}
  }
  
  def get(name: String, id: Long): Option[User] = name.db.withSession {implicit session: Session => {
    val q = for(user <- users.filter(_.id === id)) yield user
    q.firstOption
  }}
  
  def delete(name: String, id: Long): Int = name.db.withSession(implicit session => {
    val q = for(user <- users.filter(_.id === id)) yield user
    q.delete
  })
  
  def createTable(name: String, table: TableQuery[_ <: Table[_]]) = name.db.withSession { implicit session => {
	  table.ddl.create
  	}
  }
  
}
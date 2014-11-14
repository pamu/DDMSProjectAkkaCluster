package com.ddms.cluster.messages

import com.ddms.model.Models._
import com.ddms.model.DAO._
import com.ddms.model.Tables._

object ClusterMessages {
  case class Job(message: String)
  case class JobResult(message: String)
  case object BackendRegistration
  case class CalFac(num: Int)
  case class Insert(user: User)
  case class Get(id: Long)
  case class Delete(id: Long)
  case class Create(name: String)
  case class Result(user: Option[User])
}
package com.ddms.cluster.backend

import akka.actor.Actor
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberUp
import com.ddms.cluster.messages.ClusterMessages._
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.Member
import akka.actor.RootActorPath
import akka.cluster.MemberStatus
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.Props
import com.ddms.model.Models._
import com.ddms.model.DAO._
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import akka.pattern.pipe

object ClusterBackend {
  def main(args: Array[String]): Unit = {
    val port = if(args.isEmpty) "0" else args(0)
    
    val db = if(args.isEmpty) "1" else args(1)
    
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
    			  withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend]")).
    			  withFallback(ConfigFactory.load())
    
    val system = ActorSystem("ClusterSystem", config)
    
    system actorOf(Props(new ClusterBackend(db)), name = "backend")
    
  }
}

class ClusterBackend(db: String) extends Actor {
  
  val cluster = Cluster(context.system)
  
  override def preStart(): Unit = {
    
    cluster.subscribe(self, classOf[MemberUp])
  }
  
  override def postStop(): Unit = cluster.unsubscribe(self)
  
  def receive = {
    
    case job: Job => sender ! JobResult(s"${job.message.toUpperCase()} executed by ${cluster.selfAddress}")
    
    case created: Create => {
      Try(create(db)) match {
        case Success(t) => if(t) {
          sender ! JobResult("Creation successful")
        }else {
          sender ! JobResult("Table already exists")
        }
        case Failure(t) => sender ! JobResult(s"Creation failed due to ${t.getCause()}")
      }
    }
    
    case Insert(user) => {
      println("async insert command execution")
      import context.dispatcher
      insertF(db, user) pipeTo self
    }
    
    case id: Int => sender ! JobResult(s"user with $id is inserted")
    
    case Delete(id) => sender ! JobResult(s"user with ID ${delete(db, id)} is deleted successfully")
    
    case Get(id: Long) => sender ! Result(get(db, id))
    
    case state: CurrentClusterState => {
      println("current cluster state changed")
      state.members.filter(_.status == MemberStatus.Up) foreach register
    }
    
    case MemberUp(member) => {
      println(member.toString+"  is member up, has role: "+member.roles)
      register(member)
    }
    
  }
  
  def fac(n: Int): Int = (0 to n).toList.foldLeft(1)((r, c) => r * c)
  
  def register(member: Member): Unit = {
    if(member.hasRole("frontend")) {
      println("member has role frontend member path: "+member.toString)
      context.actorSelection(RootActorPath(member.address) / "user" / "frontend") ! BackendRegistration
    }
  }
}
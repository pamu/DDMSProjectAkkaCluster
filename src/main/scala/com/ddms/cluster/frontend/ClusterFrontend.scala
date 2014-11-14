package com.ddms.cluster.frontend

import akka.actor.Actor
import akka.actor.ActorRef
import com.ddms.cluster.messages.ClusterMessages._
import akka.actor.Terminated
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.Props
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.duration._
import akka.util.Timeout
import akka.pattern.ask
import com.ddms.model.Models._
import scala.util._

object ClusterFrontend {
  def main(args: Array[String]): Unit = {
    val port = if(args.isEmpty) "0" else args(0)
    
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
    			 withFallback(ConfigFactory.parseString("akka.cluster.roles = [frontend]")).
    			 withFallback(ConfigFactory.load())
    
    val system = ActorSystem("ClusterSystem", config)
    
    val frontend = system actorOf(Props[ClusterFrontend], name = "frontend")
    
    /*
    val counter = new AtomicInteger
    
    import system.dispatcher
    
    implicit val timeout = Timeout(5 seconds)
    
    system.scheduler.schedule(2 seconds, 2 seconds)(
    	(frontend ? Job("cluster node job: "+counter.getAndIncrement())) onSuccess {
    	  case result => println("result: "+result)
    	}
    )
    * 
    */
    
    import system.dispatcher
    
    implicit val timeout = Timeout(10 seconds)
    
    val counter = new AtomicInteger
    
    (frontend ? Create("")) onComplete {
      case Success(result) => println(s"result : $result")
      case Failure(value) => println(s"failed: ${value}")
    }
    
    system.scheduler.schedule(3 seconds, 5 seconds) {
        
        println("fired a insert command")
        
        (frontend ? Insert(User(s"pamu${counter.getAndIncrement()}", Some(counter.getAndIncrement().toString.toLong)))) onSuccess {
          case result => println(s"result: $result")
        }
  }
  }
}

class ClusterFrontend extends Actor {
  
  var backends = IndexedSeq.empty[ActorRef]
  
  var jobCounter = 0
  
  def receive = {
    
  	case job: Job if backends isEmpty => {
  	  println("running cluster nodes: "+backends.size)
  	  sender ! JobResult("Service temporary unavailable, Please try again later. Job: "+job.message)
  	}
    
    case job: Job => {
      jobCounter += 1
      //backends(jobCounter % backends.size) forward job
      backends foreach(_.forward(job))
    }
    
    case create: Create => {
      println("craete query fired")
      jobCounter += 1
      backends foreach(_.forward(create))
    }
    
    case insert: Insert => {
      jobCounter += 1
      backends foreach(_.forward(insert))
    }
    
    case delete: Delete => {
      jobCounter += 1
      backends foreach(_.forward(delete))
    }
    
    case get: Get => {
      jobCounter += 1
      backends foreach(_.forward(get))
    }
    
    case BackendRegistration if ! backends.contains(sender) => {
      context watch sender
      backends = backends :+ sender
    }
    
    case Terminated(actorRef) => {
      backends = backends.filterNot(_ == actorRef)
    }
    
  }
}
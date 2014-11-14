name := "DDMS"

version := "1.0.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq("com.typesafe.akka" %% "akka-cluster" % "2.3.6",
			   "com.typesafe.slick" %% "slick" % "2.1.0",
			   "mysql" % "mysql-connector-java" % "5.1.34")

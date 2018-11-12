
name := "AlpakkaESDemo"

version := "0.1"

scalaVersion := "2.12.7"
libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-elasticsearch" % "1.0-M1"
libraryDependencies += "org.elasticsearch.client" % "rest" % "5.4.0"
libraryDependencies += "com.typesafe.akka" %% "akka-stream-kafka" % "0.18"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.10"




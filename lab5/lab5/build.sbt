name := "lab-5"

EclipseKeys.withSource := true

scalaVersion := "2.10.4"

resolvers += "dnvriend at bintray" at "http://dl.bintray.com/dnvriend/maven"

libraryDependencies += "com.github.dnvriend" %% "akka-persistence-inmemory" % "1.0.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.6",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.3",
  "com.typesafe.akka" %% "akka-persistence-experimental" % "2.3.6",
  "ch.qos.logback" % "logback-classic" % "1.0.7")

name := "reactive-tests"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.3",
  "org.scalatest" %% "scalatest" % "1.9.2-SNAP2" % "test",
  "ch.qos.logback" % "logback-classic" % "1.0.7")

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

name := "reactive-persistence"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.6",
  "com.typesafe.akka" %% "akka-persistence-experimental" % "2.3.6")

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource


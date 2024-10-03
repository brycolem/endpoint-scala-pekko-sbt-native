name := "scala"
version := "0.1"

scalaVersion := "3.3.1"

libraryDependencies ++= Seq(
    "com.typesafe.slick" %% "slick" % "3.5.1",
    "com.typesafe.slick" %% "slick-hikaricp" % "3.5.1",
    "org.postgresql" % "postgresql" % "42.6.0",
    "io.spray" %% "spray-json" % "1.3.6",
    "org.apache.pekko" %% "pekko-actor" % "1.0.1",
    "org.apache.pekko" %% "pekko-stream" % "1.0.1",
    "org.apache.pekko" %% "pekko-http" % "1.0.1",
    "org.apache.pekko" %% "pekko-http-spray-json" % "1.0.1",
    "ch.qos.logback" % "logback-classic" % "1.5.7",
    "org.apache.pekko" %% "pekko-slf4j" % "1.0.1",
    "com.typesafe" % "config" % "1.4.2"
)

enablePlugins(JavaAppPackaging)

maintainer := "BPC"
mainClass in Compile := Some("scala.HttpServer")

javaOptions in Universal ++= Seq(
  "-J-Xms512M",
  "-J-Xmx2G",
  "-J-XX:+UseG1GC",
  "-J-XX:+PrintGCDetails",
  "-J-server"
)
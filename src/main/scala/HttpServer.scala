package scala

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import slick.jdbc.PostgresProfile.api._
import controller.ApplicationController
import service.ApplicationService
import org.apache.pekko.actor.CoordinatedShutdown

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

object HttpServer extends App {
  implicit val system: ActorSystem = ActorSystem("pekko-system")
  val log = system.log
  
  val db = Database.forConfig("db")
  
  val applicationService = new ApplicationService(db)
  val applicationController = new ApplicationController(applicationService)

  val bindingFuture = Http().newServerAt("0.0.0.0", 8001).bind(applicationController.routes)
  
  println(s"Server online at http://localhost:8001/")
  
  CoordinatedShutdown(system).addJvmShutdownHook {
    log.info("Shutting down gracefully...")
  }
  
  Await.result(system.whenTerminated, Duration.Inf)
}

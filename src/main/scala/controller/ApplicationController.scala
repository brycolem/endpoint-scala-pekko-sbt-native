package controller

import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives._
import service.ApplicationService
import scala.concurrent.ExecutionContext
import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

class ApplicationController(applicationService: ApplicationService)(implicit executionContext: ExecutionContext) {

  val routes = pathPrefix("application") {
    pathEnd {
      get {
        onSuccess(applicationService.getAllApplications()) { applicationsJson =>
          complete(StatusCodes.OK -> applicationsJson)
        }
      } ~
      post {
        entity(as[String]) { jsonData =>
          onSuccess(applicationService.createApplication(jsonData)) { applicationJson =>
            complete(StatusCodes.Created -> applicationJson)
          }
        }
      }
    } ~
    path(IntNumber) { id =>
      get {
        onSuccess(applicationService.getApplication(id)) {
          case Some(applicationJson) => complete(StatusCodes.OK -> applicationJson)
          case None => complete(StatusCodes.NotFound -> s"""{"message": "Application with id $id not found"}""")
        }
      } ~
      put {
        entity(as[String]) { jsonData =>
          onSuccess(applicationService.updateApplication(id, jsonData)) {
            case Some(updatedApplicationJson) => complete(StatusCodes.OK -> updatedApplicationJson)
            case None => complete(StatusCodes.NotFound -> s"""{"message": "Application with id $id not found"}""")
          }
        }
      } ~
      patch {
        entity(as[String]) { partialJson =>
          onSuccess(applicationService.updatePartialApplication(id, partialJson)) {
            case Some(updatedApplicationJson) => complete(StatusCodes.OK -> updatedApplicationJson)
            case None => complete(StatusCodes.NotFound -> s"""{"message": "Application with id $id not found"}""")
          }
        }
      } ~
      delete {
        onSuccess(applicationService.deleteApplication(id)) { resultJson =>
          complete(StatusCodes.OK -> resultJson)
        }
      }
    }
  }
}

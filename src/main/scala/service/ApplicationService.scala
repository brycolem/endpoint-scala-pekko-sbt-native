package service

import model.{Application, ApplicationTable, Note, NoteTable, ApplicationJsonProtocol}
import slick.jdbc.PostgresProfile.api._
import spray.json._
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

class ApplicationService(db: Database)(implicit executionContext: ExecutionContext) {

  import ApplicationJsonProtocol._

  private val applicationTable = TableQuery[ApplicationTable]
  private val noteTable = TableQuery[NoteTable]

  def getAllApplications(): Future[String] = {
    val query = applicationTable.result.flatMap { applications =>
      val appIds = applications.map(_.id.get)
      noteTable.filter(_.applicationId inSet appIds).result.map { notes =>
        val notesGrouped = notes.groupBy(_.applicationId)
        applications.map { application =>
          val associatedNotes = notesGrouped.getOrElse(application.id.get, Seq.empty)
          application.copy(notes = associatedNotes)
        }
      }
    }
    
    db.run(query).map { applicationsWithNotes =>
      applicationsWithNotes.toJson.compactPrint
    }
  }

  def createApplication(jsonData: String): Future[String] = {
    val application = jsonData.parseJson.convertTo[Application]
    db.run((applicationTable returning applicationTable.map(_.id) into ((application, id) => application.copy(id = Some(id)))) += application).map { createdApplication =>
      createdApplication.toJson.compactPrint
    }
  }

  def getApplication(id: Int): Future[Option[String]] = {
    val query = for {
      application <- applicationTable.filter(_.id === id).result.headOption
      notes <- noteTable.filter(_.applicationId === id).result
    } yield (application, notes)

    db.run(query).map {
      case (Some(application), notes) => Some(application.copy(notes = notes).toJson.compactPrint)
      case (None, _) => None
    }
  }

  def updateApplication(id: Int, jsonData: String): Future[Option[String]] = {
    val updatedApplicationData = jsonData.parseJson.convertTo[Application]
    val query = applicationTable.filter(_.id === id).result.headOption
    db.run(query).flatMap {
      case Some(_) =>
        db.run(applicationTable.filter(_.id === id).update(updatedApplicationData)).map(_ => Some(updatedApplicationData.toJson.compactPrint))
      case None => Future.successful(None)
    }
  }

  def updatePartialApplication(id: Int, partialJson: String): Future[Option[String]] = {
    val partialData = partialJson.parseJson.asJsObject
    val query = applicationTable.filter(_.id === id).result.headOption

    db.run(query).flatMap {
      case Some(existingApplication) =>
        val updatedApplication = partialData.fields.foldLeft(existingApplication) {
          case (application, ("employer", JsString(newEmployer))) => application.copy(employer = newEmployer)
          case (application, ("title", JsString(newTitle))) => application.copy(title = newTitle)
          case (application, ("link", JsString(newLink))) => application.copy(link = newLink)
          case (application, _) => application
        }
        db.run(applicationTable.filter(_.id === id).update(updatedApplication)).map(_ => Some(updatedApplication.toJson.compactPrint))
      case None => Future.successful(None)
    }
  }

  def deleteApplication(id: Int): Future[String] = {
    db.run(applicationTable.filter(_.id === id).delete).map { rowsAffected =>
      s"""{"message": "Application $id deleted"}"""
    }
  }
}

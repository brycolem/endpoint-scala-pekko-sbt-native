package model

import slick.jdbc.PostgresProfile.api._
import spray.json._

case class Application(id: Option[Int], employer: String, title: String, companyId: Int, link: String, notes: Seq[Note] = Seq.empty)

object Application {
  val tupled = (Application.apply _).tupled
}

case class Note(id: Option[Int], applicationId: Int, noteText: String)

object Note {
  val tupled = (Note.apply _).tupled
}

class ApplicationTable(tag: Tag) extends Table[Application](tag, "applications") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def employer = column[String]("employer")
  def title = column[String]("title")
  def companyId = column[Int]("company_id")
  def link = column[String]("link")

  def * = (id.?, employer, title, companyId, link) <> ((id, employer, title, companyId, link) => Application(id, employer, title, companyId, link, Seq.empty), (app: Application) => Some((app.id, app.employer, app.title, app.companyId, app.link)))
}

class NoteTable(tag: Tag) extends Table[Note](tag, "notes") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def applicationId = column[Int]("application_id")
  def noteText = column[String]("note_text")

  def applicationFk = foreignKey("app_fk", applicationId, TableQuery[ApplicationTable])(_.id)

  def * = (id.?, applicationId, noteText) <> (Note.tupled, Note.unapply)
}

object ApplicationJsonProtocol extends DefaultJsonProtocol {
  implicit val noteFormat: RootJsonFormat[Note] = jsonFormat3(model.Note.apply)
  implicit val applicationFormat: RootJsonFormat[Application] = jsonFormat6(model.Application.apply)
}

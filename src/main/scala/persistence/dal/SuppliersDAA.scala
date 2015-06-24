package persistence.dal

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import persistence.{SuppliersRow, Tables}

import slick.driver.JdbcProfile

object SuppliersRowsDAA {

  case class Save(sup: SuppliersRow)

  case class GetSupplierById(id: Int)

  case class CreateTables()

}


class SuppliersDAA(implicit val db: JdbcProfile#Backend#Database, implicit val profile: JdbcProfile)
  extends Actor with Tables with LazyLogging {

  import SuppliersRowsDAA._
  import profile.api._

  def receive = {
    case Save(sup) ⇒ sender ! db.run(Suppliers += sup)

    case GetSupplierById(id) ⇒ sender ! db.run(Suppliers.filter(_.id === id).result)

    case CreateTables =>
      try {
        sender ! db.run(DBIO.seq(Suppliers.schema.create))
      } catch {
        case e: Exception => logger.info("Could not create table of suppliers.... assuming it already exists")
      }
  }
}
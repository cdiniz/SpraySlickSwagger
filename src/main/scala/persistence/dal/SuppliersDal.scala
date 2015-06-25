package persistence.dal

import com.typesafe.scalalogging.LazyLogging
import persistence.SuppliersRow
import slick.driver.JdbcProfile
import utils.DbModule

import scala.concurrent.Future


trait SuppliersDal {
  def save(sup: SuppliersRow): Future[Int]

  def getSupplierById(id: Int): Future[Vector[SuppliersRow]]

  def createTables(): Future[Unit]
}


class SuppliersDalImpl(implicit val db: JdbcProfile#Backend#Database, implicit val profile: JdbcProfile)
  extends SuppliersDal with DbModule with LazyLogging {

  import profile.api._

  override def save(sup: SuppliersRow): Future[Int] = {
    db.run(Suppliers += sup).mapTo[Int]
  }

  override def getSupplierById(id: Int): Future[Vector[SuppliersRow]] = {
    db.run(Suppliers.filter(_.id === id).result).mapTo[Vector[SuppliersRow]]
  }

  override def createTables(): Future[Unit] = {
    db.run(DBIO.seq(Suppliers.schema.create))
  }

}

package utils

import persistence.Tables
import persistence.dal.{SuppliersDal, SuppliersDalImpl}
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile


/*trait Profile {
  val profile: JdbcProfile
}*/

trait DbModule extends Tables {
  val db: JdbcProfile#Backend#Database
}

trait PersistenceModule {
  val suppliersDal: SuppliersDal
}

trait PersistenceModuleImpl extends PersistenceModule with DbModule {
  this: ActorModule with Configuration =>

  // use an alternative database configuration ex:
  // private val dbConfig : DatabaseConfig[JdbcProfile]  = DatabaseConfig.forConfig("mysqldb")
  private val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("h2db")

  override implicit val profile: JdbcProfile = dbConfig.driver
  override implicit val db: JdbcProfile#Backend#Database = dbConfig.db

  override val suppliersDal = new SuppliersDalImpl()

  val self = this

}

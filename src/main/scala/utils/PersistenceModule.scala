package utils

import akka.actor.{ActorSelection, Props}
import persistence.Tables
import persistence.dal.SuppliersDAA
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile


/*trait Profile {
  val profile: JdbcProfile
}*/

trait DbModule extends Tables {
  val db: JdbcProfile#Backend#Database
}

trait PersistenceModule {
  val suppliersDAA: ActorSelection
}

trait PersistenceModuleImpl extends PersistenceModule with DbModule {
  this: ActorModule with Configuration =>

  private val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("mysqldb")
  override implicit val profile: JdbcProfile = dbConfig.driver
  override implicit val db: JdbcProfile#Backend#Database = dbConfig.db
  override val suppliersDAA = system.actorSelection("/user/suppliersDAA")
  val self = this

  system.actorOf(Props(new SuppliersDAA()), "suppliersDAA")

}

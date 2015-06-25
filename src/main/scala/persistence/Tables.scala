package persistence
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.driver.MySQLDriver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.driver.JdbcProfile
  import profile.api._
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema = Suppliers.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema


  /** GetResult implicit for fetching SuppliersRow objects using plain SQL queries */
  implicit def GetResultSuppliersRow(implicit e0: GR[Int], e1: GR[String]): GR[SuppliersRow] = GR{
    prs => import prs._
    SuppliersRow.tupled((<<[Int], <<[String], <<[String]))
  }
  /** Table description of table SUPPLIERS. Objects of this class serve as prototypes for rows in queries. */
  class Suppliers(_tableTag: Tag) extends Table[SuppliersRow](_tableTag, "SUPPLIERS") {
    def * = (id, userid, lastName) <> (SuppliersRow.tupled, SuppliersRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(userid), Rep.Some(lastName)).shaped.<>({r=>import r._; _1.map(_=> SuppliersRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column userID SqlType(TEXT) */
    val userid: Rep[String] = column[String]("userID")
    /** Database column last_name SqlType(TEXT) */
    val lastName: Rep[String] = column[String]("last_name")
  }
  /** Collection-like TableQuery object for table Suppliers */
  lazy val Suppliers = new TableQuery(tag => new Suppliers(tag))
  /** implicit join conditions for auto joins */
  object AutoJoins {

  }
}
/** Entity class storing rows of table Suppliers
   *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
   *  @param name Database column userID SqlType(TEXT)
   *  @param desc Database column last_name SqlType(TEXT) */
  case class SuppliersRow(id: Int, name: String, desc: String)
  /** UX Helper case class without id */
  case class SimpleSuppliersRow(name: String, desc: String)

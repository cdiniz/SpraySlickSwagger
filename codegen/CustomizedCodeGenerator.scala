package codegen

import slick.codegen.SourceCodeGenerator
import slick.driver.JdbcProfile
import slick.model.Model

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}


class CustomizedCodeGenerator(model: Model) extends SourceCodeGenerator(model) {
  val joins = tables.flatMap(_.foreignKeys.map { foreignKey =>
    import foreignKey._
    val fkt = referencingTable.TableClass.name
    val pkt = referencedTable.TableClass.name
    val columns = referencingColumns.map(_.name) zip referencedColumns.map(_.name)
    s"implicit def autojoin$fkt$pkt = (left:$fkt,right:$pkt) => " +
      columns.map {
        case (lcol, rcol) =>
          "left." + lcol + " === " + "right." + rcol
      }.mkString(" && ")
  })
  val models = new mutable.MutableList[String]

  override def packageCode(profile: String, pkg: String, container: String, parentType: Option[String]): String = {
    super.packageCode(profile, pkg, container, parentType) + "\nimport org.joda.time.DateTime\n" + outsideCode
  }

  def outsideCode = s"${indent(models.mkString("\n"))}"

  override def code =
    "import com.github.tototoshi.slick.MySQLJodaSupport._\nimport org.joda.time.DateTime\n" + super.code + "\n" +
      s"""/** implicit join conditions for auto joins */
         |object AutoJoins {
         |${indent(joins.mkString("\n"))}
          |}""".stripMargin.trim

  override def Table = new Table(_) {
    override def EntityType = new EntityTypeDef {
      override def docWithCode: String = {
        models += super.docWithCode.toString
        ""
      }

      override def code = {
        val args = columns.map(c =>
          c.default.map(v =>
            s"${c.name}: ${c.exposedType} = $v"
          ).getOrElse(
              s"${c.name}: ${c.exposedType}"
            )
        ).mkString(", ")
        val prns = (parents.take(1).map(" extends " + _) ++ parents.drop(1).map(" with " + _)).mkString("")
        s"""case class $name($args)$prns
            |/** UX Helper case class without id */
            |case class Simple$name(${args.substring(args.indexOf(",") + 1).trim})
        """.stripMargin.trim + "\n"
      }
    }

    override def Column = new Column(_) {
      override def rawType = model.tpe match {
        case "java.sql.Timestamp" => "DateTime" // kill j.s.Timestamp
        case _ =>
          super.rawType
      }
    }
  }
}


/** A runnable class to execute the code generator without further setup */
object CustomizedCodeGenerator {

  def main(args: Array[String]): Unit = {
    args.toList match {
      case slickDriver :: jdbcDriver :: url :: outputDir :: pkg :: user :: password :: Nil =>
        run(slickDriver, jdbcDriver, url, outputDir, pkg, Some(user), Some(password))
      case _ =>
        println( """
                   |Usage:
                   |  SourceCodeGenerator configURI [outputDir]
                   |  SourceCodeGenerator slickDriver jdbcDriver url outputDir pkg [user password]
                   |
                   |Options:
                   |  configURI: A URL pointing to a standard database config file (a fragment is
                   |    resolved as a path in the config), or just a fragment used as a path in
                   |    application.conf on the class path
                   |  slickDriver: Fully qualified name of Slick driver class, e.g. "slick.driver.H2Driver"
                   |  jdbcDriver: Fully qualified name of jdbc driver class, e.g. "org.h2.Driver"
                   |  url: JDBC URL, e.g. "jdbc:postgresql://localhost/test"
                   |  outputDir: Place where the package folder structure should be put
                   |  pkg: Scala package the generated code should be places in
                   |  user: database connection user name
                   |  password: database connection password
                   |
                   |When using a config file, in addition to the standard config parameters from
                   |slick.backend.DatabaseConfig you can set "codegen.package" and
                   |"codegen.outputDir". The latter can be overridden on the command line.
                 """.stripMargin.trim)
        System.exit(1)
    }
  }

  def run(slickDriver: String, jdbcDriver: String, url: String, outputDir: String, pkg: String, user: Option[String], password: Option[String]): Unit = {
    val driver: JdbcProfile =
      Class.forName(slickDriver + "$").getField("MODULE$").get(null).asInstanceOf[JdbcProfile]
    val dbFactory = driver.api.Database
    val db = dbFactory.forURL(url, driver = jdbcDriver,
      user = user.orNull, password = password.orNull, keepAliveConnection = true)
    try {
      val m = Await.result(db.run(driver.createModel(None, ignoreInvalidDefaults = false)(ExecutionContext.global).withPinnedSession), Duration.Inf)
      new CustomizedCodeGenerator(m).writeToFile(slickDriver, outputDir, pkg)
    } finally db.close()
  }

}

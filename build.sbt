
version := "0.0.3"

val slickV = "3.0.0"
// shared sbt config between main project and codegen project
val sharedSettings = Defaults.coreDefaultSettings ++ Seq(
  scalaVersion := "2.11.6",
  libraryDependencies ++= Seq(
    "com.typesafe.slick" %% "slick" % slickV,
    "com.h2database" % "h2" % "1.3.175",
    "org.postgresql" % "postgresql" % "9.3-1100-jdbc41",
    "mysql" % "mysql-connector-java" % "5.1.35"
  )
)
/** main project containing main source code depending on slick and codegen project */
lazy val mainProject = Project(
  id = "main",
  base = file("."),
  settings = sharedSettings ++ Seq(
    libraryDependencies ++= {
      val akkaV = "2.3.11"
      val sprayV = "1.3.3"
      Seq(
        "io.spray" %% "spray-can" % sprayV,
        "io.spray" %% "spray-routing" % sprayV,
        "io.spray" %% "spray-testkit" % sprayV % "test",
        "io.spray" %% "spray-json" % "1.3.1",
        "com.gettyimages" %% "spray-swagger" % "0.5.1",
        "com.typesafe.akka" %% "akka-actor" % akkaV,
        "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
        "org.specs2" %% "specs2-core" % "2.3.11" % "test",
        "org.specs2" %% "specs2-mock" % "2.3.11",
        "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
        "junit" % "junit" % "4.11" % "test",
        "com.typesafe" % "config" % "1.3.0",
        "joda-time" % "joda-time" % "2.8.1",
        "org.joda" % "joda-convert" % "1.7",
        "com.github.tototoshi" %% "slick-joda-mapper" % "2.0.0",
        "com.zaxxer" % "HikariCP" % "2.3.8",
        "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
        "ch.qos.logback" % "logback-classic" % "1.1.3"
      )
    },
    slick <<= slickCodeGenTask // register manual sbt command
    //    ,sourceGenerators in Compile <+= slickCodeGenTask // register automatic code generation on every compile, remove for only manual use
  )
).dependsOn(codegenProject)
/** codegen project containing the customized code generator */
lazy val codegenProject = Project(
  id = "codegen",
  base = file("codegen"),
  settings = sharedSettings ++ Seq(
    libraryDependencies ++= List(
      "com.typesafe.slick" %% "slick-codegen" % slickV,
      "org.slf4j" % "slf4j-nop" % "1.7.12"
    )
  )
)
lazy val slick = taskKey[Seq[File]]("gen-tables")
lazy val slickCodeGenTask = (dependencyClasspath in Compile, runner in Compile, streams) map { (cp, r, s) =>
  val outputDir = "src/main/scala"
  val pkg = "persistence"
  val jdbcDriver = "com.mysql.jdbc.Driver"
  val slickDriver = "slick.driver.MySQLDriver"
  val url = "jdbc:mysql://localhost/test"
  val username = "mysql"
  val password = "mysql"
  toError(r.run("codegen.CustomizedCodeGenerator", cp.files, Array(slickDriver, jdbcDriver, url, outputDir, pkg, username, password), s.log))
  val fname = file(s"$outputDir/${pkg.replace('.', '/')}/Tables.scala").getAbsolutePath
  Seq(file(fname))
}


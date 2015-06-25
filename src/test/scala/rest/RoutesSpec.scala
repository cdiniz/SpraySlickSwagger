package rest

import persistence.entities.JsonProtocol
import persistence.{SimpleSuppliersRow, SuppliersRow}
import spray.httpx.SprayJsonSupport
import spray.http._
import StatusCodes._
import scala.concurrent.Future
import JsonProtocol._
import SprayJsonSupport._

class RoutesSpec  extends AbstractRestTest {
  sequential

  def actorRefFactory = system

  val modules = new Modules {}

  val suppliers = new SupplierHttpService(modules){
    override def actorRefFactory = system
  }

  "SuppliersRow Routes" should {

    "return an empty array of suppliers" in {
     modules.suppliersDal.getSupplierById(1) returns Future(Vector())

      Get("/supplier/1") ~> suppliers.SupplierGetRoute ~> check {
        handled must beTrue
        status mustEqual OK
        responseAs[Seq[SuppliersRow]].isEmpty
      }
    }

    "return an array with 2 suppliers" in {
      modules.suppliersDal.getSupplierById(1) returns Future(Vector(SuppliersRow(1,"name 1", "desc 1"),SuppliersRow(2,"name 2", "desc 2")))
      Get("/supplier/1") ~> suppliers.SupplierGetRoute ~> check {
        handled must beTrue
        status mustEqual OK
        responseAs[Seq[SuppliersRow]].length == 2
      }
    }

    "create a supplier with the json in post" in {
      modules.suppliersDal.save(SuppliersRow(0,"name 1","desc 1")) returns  Future(1)
      Post("/supplier",SimpleSuppliersRow("name 1","desc 1")) ~> suppliers.SupplierPostRoute ~> check {
        handled must beTrue
        status mustEqual Created
      }
    }

    "not handle the invalid json" in {
      Post("/supplier","{\"name\":\"1\"}") ~> suppliers.SupplierPostRoute ~> check {
        handled must beFalse
      }
    }

    "not handle an empty post" in {
      Post("/supplier") ~> suppliers.SupplierPostRoute ~> check {
        handled must beFalse
      }
    }

  }

}

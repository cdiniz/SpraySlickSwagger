package rest

import akka.actor.Actor
import akka.pattern.ask
import akka.util.Timeout
import com.gettyimages.spray.swagger._
import com.typesafe.scalalogging.LazyLogging
import com.wordnik.swagger.annotations._
import com.wordnik.swagger.model.ApiInfo
import persistence.dal.SuppliersRowsDAA._
import persistence.entities.JsonProtocol
import persistence.{SimpleSuppliersRow, SuppliersRow}
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.http._
import spray.httpx.SprayJsonSupport
import spray.routing._
import utils.{Configuration, PersistenceModule}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.reflect.runtime.universe._
import scala.util.{Failure, Success}

class RoutesActor(modules: Configuration with PersistenceModule) extends Actor with HttpService with LazyLogging {

  implicit val timeout = Timeout(5.seconds)
  val swaggerService = new SwaggerHttpService {
    override def apiTypes = Seq(typeOf[SupplierHttpService])

    override def apiVersion = "2.0"

    override def baseUrl = "/"

    override def docsPath = "api-docs"

    override def actorRefFactory = context

    override def apiInfo = Some(new ApiInfo("Spray-Slick-Swagger Sample", "A scala rest api.", "TOC Url", "Cláudio Diniz cfpdiniz@gmail.com", "Apache V2", "http://www.apache.org/licenses/LICENSE-2.0"))
  }

  // create table for suppliers if the table didn't exist (should be removed, when the database wasn't h2)
  modules.suppliersDAA ? CreateTables
  val suppliers = new SupplierHttpService(modules) {
    def actorRefFactory = context
  }

  def actorRefFactory = context

  def receive = runRoute(suppliers.SupplierPostRoute ~ suppliers.SupplierGetRoute ~ swaggerService.routes ~
    get {
      pathPrefix("") {
        pathEndOrSingleSlash {
          getFromResource("swagger-ui/index.html")
        }
      } ~
        getFromResourceDirectory("swagger-ui")
    })
}


@Api(value = "/supplier", description = "Operations about suppliers")
abstract class SupplierHttpService(modules: Configuration with PersistenceModule) extends HttpService {

  import JsonProtocol._
  import SprayJsonSupport._

  implicit val timeout = Timeout(5.seconds)

  @ApiOperation(httpMethod = "GET", response = classOf[SuppliersRow], value = "Returns a supplier based on ID")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "supplierId", required = true, dataType = "integer", paramType = "path", value = "ID of supplier that needs to be fetched")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Ok")))
  def SupplierGetRoute = path("supplier" / IntNumber) { (supId) =>
    get {
      respondWithMediaType(`application/json`) {
        onComplete((modules.suppliersDAA ? GetSupplierById(supId)).mapTo[Future[Seq[SuppliersRow]]]) {
          case Success(photos) => complete(photos)
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
        }
      }
    }
  }

  @ApiOperation(value = "Add Supplier", nickname = "addSuplier", httpMethod = "POST", consumes = "application/json", produces = "text/plain; charset=UTF-8")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "Supplier Object", dataType = "persistence.SimpleSuppliersRow", required = true, paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Bad Request"),
    new ApiResponse(code = 201, message = "Entity Created")
  ))
  def SupplierPostRoute = path("supplier") {
    post {
      entity(as[SimpleSuppliersRow]) { supplierToInsert => onComplete(modules.suppliersDAA ? Save(SuppliersRow(0, supplierToInsert.userid, supplierToInsert.lastName))) {
        // ignoring the number of insertedEntities because in this case it should always be one, you might check this in other cases
        case Success(insertedEntities) => complete(StatusCodes.Created)
        case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
      }
      }
    }
  }
}


package persistence.entities

import persistence.{SimpleSuppliersRow, SuppliersRow}
import spray.json.DefaultJsonProtocol

object JsonProtocol extends DefaultJsonProtocol {
  implicit val simpleSupplierFormat = jsonFormat2(SimpleSuppliersRow)
  implicit val supplierFormat = jsonFormat3(SuppliersRow)
}
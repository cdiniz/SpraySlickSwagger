package persistence.dal

import akka.util.Timeout
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import persistence.SuppliersRow

import scala.concurrent.Await
import scala.concurrent.duration._


@RunWith(classOf[JUnitRunner])
class SuppliersDalTest extends FunSuite with AbstractPersistenceTest with BeforeAndAfterAll {
  implicit val timeout = Timeout(5.seconds)

  val modules = new Modules {
  }

  test("SuppliersActor: Testing Suppliers Actor") {
    Await.result(modules.suppliersDal.createTables(), 5.seconds)
    val numberOfEntities: Int = Await.result(modules.suppliersDal.save(SuppliersRow(0, "sup", "desc")), 5.seconds)
    assert(numberOfEntities == 1)
    val supplier: Seq[SuppliersRow] = Await.result(modules.suppliersDal.getSupplierById(1), 5.seconds)
    assert(supplier.length == 1 && supplier.head.name.compareTo("sup") == 0)
    val empty: Seq[SuppliersRow] = Await.result(modules.suppliersDal.getSupplierById(2), 5.seconds)
    assert(empty.isEmpty)
  }

  override def afterAll(): Unit = {
    modules.db.close()
  }

}

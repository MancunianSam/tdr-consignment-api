package uk.gov.nationalarchives.tdr.api.db.repository

import java.util.UUID

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.nationalarchives.tdr.api.db.DbConnection
import uk.gov.nationalarchives.tdr.api.utils.TestUtils
import uk.gov.nationalarchives.tdr.api.utils.TestUtils._

import scala.concurrent.ExecutionContext

class ConsignmentRepositorySpec extends AnyFlatSpec with ScalaFutures with Matchers {
  implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  "addParentFolder" should "add parent folder name to an existing consignment row" in {
    val db = DbConnection.db
    val consignmentRepository = new ConsignmentRepository(db)
    val consignmentId = UUID.fromString("0292019d-d112-465b-b31e-72dfb4d1254d")

    TestUtils.createConsignment(consignmentId, userId)

    consignmentRepository.addParentFolder(consignmentId, Option("TEST ADD PARENT FOLDER NAME")).futureValue

    val parentFolderName = consignmentRepository.getConsignment(consignmentId).futureValue.map(consignment => consignment.parentfolder)

    parentFolderName should contain only Some("TEST ADD PARENT FOLDER NAME")
  }

  "getParentFolder" should "get parent folder name for a consignment" in {
    val db = DbConnection.db
    val consignmentRepository = new ConsignmentRepository(db)
    val consignmentId = UUID.fromString("b6da7577-3800-4ebc-821b-9d33e52def9e")

    TestUtils.createConsignment(consignmentId, userId)
    consignmentRepository.addParentFolder(consignmentId, Option("TEST GET PARENT FOLDER NAME"))

    val parentFolderName = consignmentRepository.getParentFolder(consignmentId).futureValue

    parentFolderName should be(Some("TEST GET PARENT FOLDER NAME"))
  }

  "getParentFolder" should "return nothing if no parent folder exists" in {
    val db = DbConnection.db
    val consignmentRepository = new ConsignmentRepository(db)
    val consignmentId = UUID.fromString("8233b9a4-5c2d-4c2d-9355-e6ec5751fea5")

    TestUtils.createConsignment(consignmentId, userId)

    val parentFolderName = consignmentRepository.getParentFolder(consignmentId).futureValue

    parentFolderName should be(None)
  }

  "getSeriesOfConsignment" should "get the series for a consignment" in {
    val db = DbConnection.db
    val consignmentRepository = new ConsignmentRepository(db)
    val consignmentId = UUID.fromString("b59a8bfd-5709-46c7-a5e9-71bae146e2f1")
    val seriesId = UUID.fromString("9e2e2a51-c2d0-4b99-8bef-2ca322528861")
    val bodyId = UUID.fromString("6e3b76c4-1745-4467-8ac5-b4dd736e1b3e")
    val seriesCode = "Mock series"

    TestUtils.addSeries(seriesId, bodyId, seriesCode)
    TestUtils.createConsignment(consignmentId, userId)

    val consignmentSeries = consignmentRepository.getSeriesOfConsignment(consignmentId).futureValue.head

    consignmentSeries.code.get should be(seriesCode)
  }

  "getTransferringBodyOfConsignment" should "get the transferring body for a consignment" in {
    val db = DbConnection.db
    val consignmentRepository = new ConsignmentRepository(db)
    val consignmentId = UUID.fromString("a3088f8a-59a3-4ab3-9e50-1677648e8186")
    val seriesId = UUID.fromString("845a4589-d412-49d7-80c6-63969112728a")
    val bodyId = UUID.fromString("6e3b76c4-1745-4467-8ac5-b4dd736e1b3e")
    val seriesCode = "Mock series"
    val bodyName = "Body"

    TestUtils.addSeries(seriesId, bodyId, seriesCode)
    TestUtils.createConsignment(consignmentId, userId)

    val consignmentBody = consignmentRepository.getTransferringBodyOfConsignment(consignmentId).futureValue.head

    consignmentBody.name.get should be(bodyName)
  }
}

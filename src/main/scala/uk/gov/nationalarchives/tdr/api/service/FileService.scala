package uk.gov.nationalarchives.tdr.api.service

import java.sql.Timestamp
import java.util.UUID

import uk.gov.nationalarchives
import uk.gov.nationalarchives.Tables.FileRow
import uk.gov.nationalarchives.tdr.api.db.repository.{ConsignmentRepository, FileRepository}
import uk.gov.nationalarchives.tdr.api.graphql.fields.FileFields.{AddFilesInput, Files}

import scala.concurrent.{ExecutionContext, Future}

class FileService(
                   fileRepository: FileRepository,
                   consignmentRepository: ConsignmentRepository,
                   timeSource: TimeSource,
                   uuidSource: UUIDSource
                 )(implicit val executionContext: ExecutionContext) {

  def addFile(addFilesInput: AddFilesInput, userId: UUID): Future[Files] = {
    val rows: Seq[nationalarchives.Tables.FileRow] = List.fill(addFilesInput.numberOfFiles)(1)
      .map(_ => FileRow(uuidSource.uuid, addFilesInput.consignmentId, userId, Timestamp.from(timeSource.now)))

    consignmentRepository.addParentFolder(addFilesInput.consignmentId, addFilesInput.parentFolder)
      .flatMap(_ => fileRepository.addFiles(rows).map(_.map(_.fileid)).map(fileids => Files(fileids)))
  }

  def getOwnersOfFiles(fileIds: Seq[UUID]): Future[Seq[FileOwnership]] = {
    consignmentRepository.getConsignmentsOfFiles(fileIds)
      .map(_.map(consignmentByFile => FileOwnership(consignmentByFile._1, consignmentByFile._2.userid)))
  }

  def fileCount(consignmentId: UUID): Future[Int] = {
    fileRepository.countFilesInConsignment(consignmentId)
  }

  def getFiles(consignmentId: UUID): Future[Files] = {
    fileRepository.getFiles(consignmentId).map(r => Files(r.map(_.fileid)))
  }
}

case class FileOwnership(fileId: UUID, userId: UUID)

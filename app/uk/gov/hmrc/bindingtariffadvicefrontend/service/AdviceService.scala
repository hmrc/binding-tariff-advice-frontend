/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.bindingtariffadvicefrontend.service

import javax.inject.Inject
import play.api.Logger
import play.api.libs.Files.TemporaryFile
import play.api.mvc.Request
import uk.gov.hmrc.bindingtariffadvicefrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffadvicefrontend.connector.EmailConnector
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.routes
import uk.gov.hmrc.bindingtariffadvicefrontend.model._
import uk.gov.hmrc.bindingtariffadvicefrontend.repository.AdviceRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AdviceService @Inject()(repository: AdviceRepository,
                              fileService: FileService,
                              emailConnector: EmailConnector,
                              appConfig: AppConfig) {

  def get(id: String): Future[Option[Advice]] = repository.get(id)

  def insert(advice: Advice): Future[Advice] = repository.update(advice, upsert = true)

  def update(advice: Advice): Future[Advice] = repository.update(advice, upsert = false)

  def delete(id: String): Future[Unit] = repository.delete(id)

  def upload(metadata: FileUpload, file: TemporaryFile)(implicit hc: HeaderCarrier): Future[FileUploaded] = fileService.upload(metadata, file)

  def submit(advice: Advice)(implicit hc: HeaderCarrier, request: Request[_]): Future[Advice] = {
    val contactDetails = advice.contactDetails.getOrElse(throw new IllegalArgumentException("Cannot Submit without Contact Details"))
    val goodDetails = advice.goodDetails.getOrElse(throw new IllegalArgumentException("Cannot Submit without Good Details"))
    val supportingInfo = advice.supportingInformation.getOrElse("")
    val reference = advice.id.substring(32).toUpperCase()
    Logger.info(s"Submitting application with reference [$reference]")
    for {
      updated <- update(advice.copy(reference = Some(reference)))
      documents <- Future.sequence(updated.supportingDocuments.map(doc => fileService.publish(doc)))
      documentURLs = documents.map(doc => routes.ViewSupportingDocumentController.get(doc.id).absoluteURL())

      parameters = AdviceRequestEmailParameters(
        reference = reference,
        contactName = contactDetails.fullName,
        contactEmail = contactDetails.email,
        itemName = goodDetails.itemName,
        itemDescription = goodDetails.description,
        supportingDocuments = documentURLs.mkString("|"),
        supportingInformation = supportingInfo
      )

      _ = Logger.info(s"Sending email [to:${appConfig.submissionMailbox},reference:${parameters.reference}]")
      email = AdviceRequestEmail(Seq(appConfig.submissionMailbox), parameters)
      _ <- emailConnector.send(email) recover loggingAnError // TODO remove this recover block once Digital Contact merge https://github.com/hmrc/hmrc-email-renderer/pull/300
    } yield updated
  }

  private def loggingAnError: PartialFunction[Throwable, Unit] = {
    case e: Throwable => Logger.error("Email failed to send", e)
  }
}

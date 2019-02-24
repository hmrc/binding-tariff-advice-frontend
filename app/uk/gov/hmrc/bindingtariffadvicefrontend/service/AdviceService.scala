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
import play.api.libs.Files.TemporaryFile
import uk.gov.hmrc.bindingtariffadvicefrontend.connector.{FileStoreConnector, UpscanS3Connector}
import uk.gov.hmrc.bindingtariffadvicefrontend.model.{Advice, FileUpload, FileUploaded}
import uk.gov.hmrc.bindingtariffadvicefrontend.repository.AdviceRepository
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class AdviceService @Inject()(repository: AdviceRepository,
                              fileStoreConnector: FileStoreConnector,
                              upscanS3Connector: UpscanS3Connector) {

  def get(id: String): Future[Option[Advice]] = repository.get(id)

  def insert(advice: Advice): Future[Advice] = repository.insert(advice)

  def update(advice: Advice): Future[Advice] = repository.update(advice)

  def delete(id: String): Future[Unit] = repository.delete(id)

  def upload(metadata: FileUpload, file: TemporaryFile)(implicit hc: HeaderCarrier): Future[FileUploaded] = {
    for {
      template <- fileStoreConnector.initiate(metadata)
      _ <- upscanS3Connector.upload(template, file)
    } yield FileUploaded(id = template.id, metadata.fileName, mimeType = metadata.mimeType)
  }

  def submit(advice: Advice)(implicit hc: HeaderCarrier): Future[Advice] = update(
    advice.copy(reference = Some(advice.id.substring(32).toUpperCase()))
  )

}

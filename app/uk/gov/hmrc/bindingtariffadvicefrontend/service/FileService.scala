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
import uk.gov.hmrc.bindingtariffadvicefrontend.model.{FileSubmitted, FileUpload, FileUploaded, SupportingDocument}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FileService @Inject()(fileStoreConnector: FileStoreConnector,
                            upscanS3Connector: UpscanS3Connector){

  def get(id: String)(implicit hc: HeaderCarrier): Future[Option[FileSubmitted]] = fileStoreConnector.get(id)

  def publish(document: SupportingDocument)(implicit hc: HeaderCarrier): Future[FileSubmitted] = fileStoreConnector.publish(document.id)

  def upload(metadata: FileUpload, file: TemporaryFile)(implicit hc: HeaderCarrier): Future[FileUploaded] = {
    for {
      template <- fileStoreConnector.initiate(metadata)
      _ <- upscanS3Connector.upload(template, file)
    } yield FileUploaded(id = template.id, metadata.fileName, mimeType = metadata.mimeType)
  }

}

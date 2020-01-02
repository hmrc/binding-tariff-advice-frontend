/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.bindingtariffadvicefrontend.connector

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.bindingtariffadvicefrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffadvicefrontend.model.{FileSubmitted, FileUpload, FileUploadTemplate}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class FileStoreConnector @Inject()(configuration: AppConfig, http: AuthenticatedHttpClient) {

  def delete()(implicit hc: HeaderCarrier): Future[Unit] = {
    http.DELETE(s"${configuration.fileStoreUrl}/file").map(_ => ())
  }

  def delete(id: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.DELETE(s"${configuration.fileStoreUrl}/file/$id").map(_ => ())
  }

  def get(id: String)(implicit hc: HeaderCarrier): Future[Option[FileSubmitted]] = {
    http.GET[Option[FileSubmitted]](s"${configuration.fileStoreUrl}/file/$id")
  }

  def get(ids: Seq[String])(implicit hc: HeaderCarrier): Future[Seq[FileSubmitted]] = {
    val params = ids.map(id => s"id=$id").mkString("&")
    http.GET[Seq[FileSubmitted]](s"${configuration.fileStoreUrl}/file?$params")
  }

  def initiate(file: FileUpload)(implicit hc: HeaderCarrier): Future[FileUploadTemplate] = {
    http.POST[FileUpload, FileUploadTemplate](s"${configuration.fileStoreUrl}/file", file)
  }

  def publish(id: String)(implicit hc: HeaderCarrier): Future[FileSubmitted] = {
    http.POSTEmpty[FileSubmitted](s"${configuration.fileStoreUrl}/file/$id/publish")
  }

}

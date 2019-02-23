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

package uk.gov.hmrc.bindingtariffadvicefrontend.connector

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.bindingtariffadvicefrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffadvicefrontend.model.{FileUpload, FileUploadTemplate, FileUploaded}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class FileStoreConnector @Inject()(configuration: AppConfig,
                                   http: HttpClient) {

  def delete()(implicit hc: HeaderCarrier): Future[Unit] = {
    http.DELETE(s"${configuration.fileStoreUrl}/file").map(_ => ())
  }

  def delete(id: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.DELETE(s"${configuration.fileStoreUrl}/file/$id").map(_ => ())
  }

  def get(id: String)(implicit hc: HeaderCarrier): Future[FileUploaded] = {
    http.GET[FileUploaded](s"${configuration.fileStoreUrl}/file/$id")
  }

  def get(ids: Seq[String])(implicit hc: HeaderCarrier): Future[Seq[FileUploaded]] = {
    val params = ids.map(id => s"id=$id").mkString("&")
    http.GET[Seq[FileUploaded]](s"${configuration.fileStoreUrl}/file?$params")
  }

  def initiate(file: FileUpload)(implicit hc: HeaderCarrier): Future[FileUploadTemplate] = {
    http.POST[FileUpload, FileUploadTemplate](s"${configuration.fileStoreUrl}/file", file)
  }

  def publish(id: String)(implicit hc: HeaderCarrier): Future[FileUploaded] = {
    http.POSTEmpty[FileUploaded](s"${configuration.fileStoreUrl}/file/$id/publish")
  }

}

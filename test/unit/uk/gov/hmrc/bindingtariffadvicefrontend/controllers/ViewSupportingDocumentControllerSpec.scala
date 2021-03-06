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

package uk.gov.hmrc.bindingtariffadvicefrontend.controllers

import org.mockito.ArgumentMatchers._
import akka.stream.Materializer
import org.mockito.BDDMockito._
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.bindingtariffadvicefrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffadvicefrontend.model.{Advice, FileSubmitted, ScanStatus}
import uk.gov.hmrc.bindingtariffadvicefrontend.service.FileService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future


class ViewSupportingDocumentControllerSpec extends ControllerSpec {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val service = mock[FileService]
  private implicit val mat: Materializer = fakeApplication.materializer
  private def controller(advice: Advice) = new ViewSupportingDocumentController(service, messageApi, appConfig)

  "GET /" should {
    val advice = Advice("id")

    "return 303 and redirect for safe file found" in {
      given(service.get(refEq("id"))(any[HeaderCarrier])) willReturn Future.successful(Some(FileSubmitted("id", "file", "type", Some("url"), Some(ScanStatus.READY))))

      val result = await(controller(advice).get("id")(getRequestWithCSRF))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("url")
    }

    "return 200 for non-safe file found" in {
      given(service.get(refEq("id"))(any[HeaderCarrier])) willReturn Future.successful(Some(FileSubmitted("id", "file", "type", Some("url"), None)))

      val result = await(controller(advice).get("id")(getRequestWithCSRF))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      bodyOf(result) should include("supporting_document-heading")
    }

    "return 200 for un-safe file found" in {
      given(service.get(refEq("id"))(any[HeaderCarrier])) willReturn Future.successful(Some(FileSubmitted("id", "file", "type", Some("url"), Some(ScanStatus.FAILED))))

      val result = await(controller(advice).get("id")(getRequestWithCSRF))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      bodyOf(result) should include("supporting_document-heading")
    }

  }
}
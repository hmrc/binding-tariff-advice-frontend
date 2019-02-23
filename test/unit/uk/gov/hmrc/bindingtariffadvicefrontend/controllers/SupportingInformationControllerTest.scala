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

package uk.gov.hmrc.bindingtariffadvicefrontend.controllers

import akka.stream.Materializer
import org.mockito.BDDMockito._
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.test.Helpers.{charset, contentType, _}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.bindingtariffadvicefrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.action.{ActiveSession, ExistingAnswers}
import uk.gov.hmrc.bindingtariffadvicefrontend.model.{Advice, SupportingDocument}
import uk.gov.hmrc.bindingtariffadvicefrontend.service.AdviceService

import scala.concurrent.Future

class SupportingInformationControllerTest extends ControllerSpec {

  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val service = mock[AdviceService]
  private implicit val mat: Materializer = fakeApplication.materializer

  private def controller(advice: Advice) = new SupportingInformationController(
    ActiveSession("id"),
    ExistingAnswers(advice),
    service,
    messageApi,
    appConfig
  )

  "GET /" should {
    val advice = Advice("id")

    "return 200" in {
      val result = await(controller(advice).get(getRequestWithCSRF))
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      bodyOf(result) should include("supporting_information-heading")
    }
  }

  "POST /" should {
    val advice = Advice("id")

    "return 303 and redirect on valid form - with answer 'Yes'" in {
      val request = postRequestWithCSRF.withFormUrlEncodedBody("state" -> "true")
      val result = await(controller(advice).post(request))
      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(routes.SupportingInformationDetailsController.get().url)
    }

    "return 303 and redirect on valid form - with answer 'No'" in {
      val request = postRequestWithCSRF.withFormUrlEncodedBody("state" -> "false")
      val result = await(controller(advice).post(request))
      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(routes.SupportingInformationController.get().url)
    }

    "return 200 on form errors" in {
      val request = postRequestWithCSRF.withFormUrlEncodedBody()
      val result = await(controller(advice).post(request))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      bodyOf(result) should include("supporting_information-heading")
      bodyOf(result) should include("error-summary")
    }
  }

}

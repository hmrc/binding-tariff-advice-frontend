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


import akka.stream.Materializer
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.bindingtariffadvicefrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.action.ExistingAnswers
import uk.gov.hmrc.bindingtariffadvicefrontend.model.Advice
import uk.gov.hmrc.bindingtariffadvicefrontend.service.AdviceService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future


class CheckYourAnswersControllerSpec extends ControllerSpec {

  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val service = mock[AdviceService]
  private implicit val mat: Materializer = fakeApplication.materializer
  private def controller(advice: Advice) = new CheckYourAnswersController(ExistingAnswers(advice), service, messageApi, appConfig)

  "GET /" should {
    val advice = Advice("id")

    "return 200" in {
      val result = await(controller(advice).get(getRequestWithCSRF))
      status(result) shouldBe Status.OK
    }

    "return HTML" in {
      val result = await(controller(advice).get(getRequestWithCSRF))
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      bodyOf(result) should include("check_your_answers-heading")
    }
  }

  "POST /" should {
    val advice = Advice("id")

    "return 303 and redirect'" in {
      given(service.submit(refEq(advice))(any[HeaderCarrier], any[Request[_]])) willReturn Future.successful(advice)

      val result = await(controller(advice).post()(postRequestWithCSRF))
      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(routes.ConfirmationController.get().url)
    }
  }
}
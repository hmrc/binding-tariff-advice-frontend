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

package uk.gov.hmrc.bindingtariffadvicefrontend.controllers.action

import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers._
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.request.AnswersRequest
import uk.gov.hmrc.bindingtariffadvicefrontend.model.Advice
import uk.gov.hmrc.bindingtariffadvicefrontend.service.AdviceService

import scala.concurrent.Future

class ResetAnswersActionTest extends ControllerSpec with BeforeAndAfterEach {

  private val block = mock[Request[_] => Future[Result]]
  private val result = mock[Result]

  private val service = mock[AdviceService]
  private val action = new ResetAnswersAction(service)

  override def afterEach(): Unit = {
    super.afterEach()
    reset(block)
  }

  "Action" should {
    val request = FakeRequest().withHeaders("X-Session-ID" -> "session-id")

    "Delete existing answers & execute block" in {
      given(service.delete("session-id")) willReturn Future.successful(())

      givenTheBlockReturns(result)

      await(action.invokeBlock(request, block)) shouldBe result

      verify(service.delete("id"))
    }

    "Return Bad Request on missing Session" in {
      val result = await(action.invokeBlock(FakeRequest(), block))
      status(result) shouldBe 400
    }
  }

  private def givenTheBlockReturns(result: Result): Unit = {
    given(block.apply(any[AnswersRequest[_]])) willReturn Future.successful(result)
  }
}

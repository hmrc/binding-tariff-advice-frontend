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

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.mvc.Results._
import play.api.test.FakeRequest
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.request.ActiveSessionRequest
import uk.gov.hmrc.http.HeaderNames
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class RequireSessionActionTest extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private val block = mock[ActiveSessionRequest[_] => Future[Result]]
  private val result = mock[Result]
  private val action: RequireSessionAction = new RequireSessionAction()

  override def beforeEach(): Unit = {
    super.beforeEach()
  }

  override def afterEach(): Unit = {
    super.afterEach()
    reset(block)
  }

  "Require Session" should {
    "Execute Block on valid Session" in {
      givenBlockReturns(result)

      val request = FakeRequest().withHeaders(HeaderNames.xSessionId -> "session id")

      await(action.invokeBlock(request, block)) shouldBe result

      theActiveSessionRequest shouldBe ActiveSessionRequest(request, "session id")
    }

    "Redirect on no session" in {
      val request = FakeRequest()

      await(action.invokeBlock(request, block)) shouldBe Redirect(uk.gov.hmrc.bindingtariffadvicefrontend.controllers.routes.SessionExpiredController.get())
    }
  }

  private def givenBlockReturns(result: Result): Unit = {
    given(block.apply(any[ActiveSessionRequest[_]])) willReturn Future.successful(result)
  }

  private def theActiveSessionRequest: ActiveSessionRequest[_] = {
    val captor: ArgumentCaptor[ActiveSessionRequest[_]] = ArgumentCaptor.forClass(classOf[ActiveSessionRequest[_]])
    verify(block).apply(captor.capture())
    captor.getValue
  }

}

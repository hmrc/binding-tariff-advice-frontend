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

package uk.gov.hmrc.bindingtariffadvicefrontend.controllers.action

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.Result
import play.api.test.FakeRequest
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers._
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.request.AnswersRequest
import uk.gov.hmrc.bindingtariffadvicefrontend.model.Advice
import uk.gov.hmrc.bindingtariffadvicefrontend.service.AdviceService

import scala.concurrent.Future

class RetrieveSubmittedAnswersActionTest extends ControllerSpec with BeforeAndAfterEach {

  private val block = mock[AnswersRequest[_] => Future[Result]]
  private val result = mock[Result]

  private val service = mock[AdviceService]
  private val action = new RetrieveSubmittedAnswersAction(service)

  override def afterEach(): Unit = {
    super.afterEach()
    reset(block, service)
  }

  "Action" should {
    val request = FakeRequest().withHeaders("X-Session-ID" -> "session-id")

    "Retrieve existing answers & execute block" in {
      val existingAnswers = Advice("id", reference = Some("ref"))

      givenTheUserHasEntered(existingAnswers)
      givenTheBlockReturns(result)

      await(action.invokeBlock(request, block)) shouldBe result

      theAnswersRequest shouldBe AnswersRequest(request, existingAnswers)
    }

    "Redirect to Session Expired - on unsubmitted answers" in {
      val existingAnswers = Advice("id")

      givenTheUserHasEntered(existingAnswers)

      val result = await(action.invokeBlock(request, block))
      status(result) shouldBe 303
      locationOf(result) shouldBe Some(routes.SessionExpiredController.get().url)
    }

    "Redirect to Session Expired - on no answers" in {
      givenTheUserHasEnteredNothing()

      val result = await(action.invokeBlock(request, block))
      status(result) shouldBe 303
      locationOf(result) shouldBe Some(routes.SessionExpiredController.get().url)
    }

    "Return Bad Request on missing Session" in {
      givenTheUserHasEnteredNothing()

      val result = await(action.invokeBlock(FakeRequest(), block))
      status(result) shouldBe 400
    }
  }

  private def givenTheUserHasEntered(existing: Advice): Unit = {
    given(service.get("session-id")) willReturn Future.successful(Some(existing))
  }

  private def givenTheUserHasEnteredNothing(): Unit = {
    given(service.get("session-id")) willReturn Future.successful(None)
  }

  private def givenTheBlockReturns(result: Result): Unit = {
    given(block.apply(any[AnswersRequest[_]])) willReturn Future.successful(result)
  }

  private def theAnswersRequest: AnswersRequest[_] = {
    val captor: ArgumentCaptor[AnswersRequest[_]] = ArgumentCaptor.forClass(classOf[AnswersRequest[_]])
    verify(block).apply(captor.capture())
    captor.getValue
  }
}

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
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.test.FakeRequest
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.request.{ActiveSessionRequest, AnswersRequest}
import uk.gov.hmrc.bindingtariffadvicefrontend.model.Advice
import uk.gov.hmrc.bindingtariffadvicefrontend.service.AdviceService
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class RetrieveAnswersActionTest extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private val block = mock[AnswersRequest[_] => Future[Result]]
  private val result = mock[Result]

  private val service = mock[AdviceService]
  private val action = new RetrieveAnswersAction(service)

  override def afterEach(): Unit = {
    super.afterEach()
    reset(block)
  }

  "Action" should {
    val request = FakeRequest()

    "Retrieve existing answers" in {
      val existingAnswers = mock[Advice]

      givenTheUserHasEntered(existingAnswers)
      givenTheBlockReturns()

      val sessionRequest = ActiveSessionRequest(request, "session-id")
      await(action.invokeBlock(sessionRequest, block)) shouldBe result

      theAnswersRequest shouldBe AnswersRequest(request, existingAnswers)
    }

    "Initialize new answers" in {
      givenTheUserHasEnteredNothing()
      givenTheBlockReturns()

      val sessionRequest = ActiveSessionRequest(request, "session-id")
      await(action.invokeBlock(sessionRequest, block)) shouldBe result

      theAnswersRequest shouldBe AnswersRequest(request, Advice("session-id"))
    }
  }

  private def givenTheUserHasEntered(existing: Advice): Unit = {
    given(service.get("session-id")) willReturn Future.successful(Some(existing))
  }

  private def givenTheUserHasEnteredNothing(): Unit = {
    given(service.get("session-id")) willReturn Future.successful(None)
    given(service.insert(any[Advice])) will returnTheAdvice
  }

  private def givenTheBlockReturns(): Unit = {
    given(block.apply(any[AnswersRequest[_]])) willReturn Future.successful(result)
  }

  private def theAnswersRequest: AnswersRequest[_] = {
    val captor: ArgumentCaptor[AnswersRequest[_]] = ArgumentCaptor.forClass(classOf[AnswersRequest[_]])
    verify(block).apply(captor.capture())
    captor.getValue
  }

  private def returnTheAdvice: Answer[Future[Advice]] = {
    new Answer[Future[Advice]]() {
      override def answer(invocation: InvocationOnMock): Future[Advice] = Future.successful(invocation.getArgument(0))
    }
  }
}

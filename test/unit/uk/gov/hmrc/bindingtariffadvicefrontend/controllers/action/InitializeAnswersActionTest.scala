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
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.request.AnswersRequest
import uk.gov.hmrc.bindingtariffadvicefrontend.model.Advice
import uk.gov.hmrc.bindingtariffadvicefrontend.service.AdviceService
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class InitializeAnswersActionTest extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private val block = mock[Request[_] => Future[Result]]
  private val result = mock[Result]
  private val service: AdviceService = mock[AdviceService]
  private val action: InitializeAnswersAction = new InitializeAnswersAction(service)

  override def beforeEach(): Unit = {
    super.beforeEach()
  }

  override def afterEach(): Unit = {
    super.afterEach()
    reset(block)
  }

  "Initialize Answers" should {
    val request = FakeRequest().withHeaders("X-Session-ID" -> "session-id")

    "Create new answers & execute block" in {
      given(service.insert(any[Advice])) will returnTheAdvice
      givenTheBlockReturns(result)

      await(action.invokeBlock(request, block)) shouldBe result

      theAnswersRequest shouldBe AnswersRequest(request, Advice(id = "session-id"))
    }

    "Return Bad Request on missing Session" in {
      val result = await(action.invokeBlock(FakeRequest(), block))
      status(result) shouldBe 400
    }
  }

  private def givenTheBlockReturns(result: Result): Unit = {
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
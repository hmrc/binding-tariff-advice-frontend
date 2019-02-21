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

import org.mockito.Mockito
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.request.{ActiveSessionRequest, AnswersRequest}
import uk.gov.hmrc.bindingtariffadvicefrontend.model.Advice
import uk.gov.hmrc.bindingtariffadvicefrontend.service.AdviceService

import scala.concurrent.Future

class ExistingAnswers(advice: Advice) extends RetrieveAnswersAction(Mockito.mock(classOf[AdviceService])) {
  protected override def transform[A](sessionRequest: ActiveSessionRequest[A]): Future[AnswersRequest[A]] =
    Future.successful(AnswersRequest(sessionRequest.request, advice))
}

object ExistingAnswers {
  def apply(advice: Advice): ExistingAnswers = new ExistingAnswers(advice)
}

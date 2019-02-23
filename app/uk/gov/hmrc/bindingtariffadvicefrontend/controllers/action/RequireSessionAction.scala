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

import javax.inject.Inject
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.request.ActiveSessionRequest
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.routes
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.Future

class RequireSessionAction @Inject()()
  extends ActionBuilder[ActiveSessionRequest]
    with ActionFunction[Request, ActiveSessionRequest] {

  override def invokeBlock[A](request: Request[A], block: ActiveSessionRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    hc.sessionId.map(_.value) match {
      case Some(sessionId: String) => block(ActiveSessionRequest(request, sessionId))
      case _ => Future.successful(Redirect(routes.SessionExpiredController.get()))
    }

  }
}

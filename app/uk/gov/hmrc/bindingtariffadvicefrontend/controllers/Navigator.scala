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

import play.api.mvc.{Call, Result, Results}
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.action.{CheckMode, Mode, NormalMode}

object Navigator {

  def continue(call: Call): Result = Results.Redirect(call)

  def redirect(call: Call)(implicit mode: Mode = NormalMode): Result = mode match {
    case NormalMode => Results.Redirect(call)
    case CheckMode => Results.Redirect(routes.CheckYourAnswersController.get())
  }
}

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

package uk.gov.hmrc.bindingtariffadvicefrontend.controllers.forms

import play.api.data.Form
import play.api.data.Forms._

object BooleanForm {

  val form: Form[Boolean] = Form[Boolean](
    mapping[Boolean, Boolean](
      // Booleans aren't mandatory by default - Have to do similar to the below to enforce it is submitted
      "state" -> optional(boolean).verifying("state.required", _.isDefined).transform(_.get, Some(_))
    )(identity)(Some(_))
  )

}

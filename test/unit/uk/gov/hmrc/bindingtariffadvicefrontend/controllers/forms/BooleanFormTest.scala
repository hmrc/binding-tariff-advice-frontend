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

package uk.gov.hmrc.bindingtariffadvicefrontend.controllers.forms

import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.forms.BooleanForm.form
import uk.gov.hmrc.play.test.UnitSpec

class BooleanFormTest extends UnitSpec {

  private val validParams = Map[String, String](
    "state" -> "true"
  )

  "Form" should {
    "fill" in {
      form().fill(true).data shouldBe validParams
    }

    "bind" in {
      val boundForm = form().bind(validParams)
      boundForm.hasErrors shouldBe false
      boundForm.get shouldBe true
    }

    "mandate populated 'state'" in {
      val invalidParams = validParams + ("state" -> "")
      val boundForm = form().bind(invalidParams)
      boundForm.hasErrors shouldBe true
    }

    "mandate 'state'" in {
      val invalidParams = validParams - "state"
      val boundForm = form().bind(invalidParams)
      boundForm.hasErrors shouldBe true
    }
  }

}

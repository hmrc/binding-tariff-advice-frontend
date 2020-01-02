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

import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.forms.GoodDetailsForm.form
import uk.gov.hmrc.bindingtariffadvicefrontend.model.GoodDetails
import uk.gov.hmrc.play.test.UnitSpec

class GoodDetailsFormTest extends UnitSpec {

  private val validContent = GoodDetails(
    itemName = "name",
    description = "description"
  )

  private val validParams = Map[String, String](
    "item-name" -> "name",
    "description" -> "description"
  )

  "Form" should {
    "fill" in {
      form.fill(validContent).data shouldBe validParams
    }

    "bind" in {
      val boundForm = form.bind(validParams)
      boundForm.hasErrors shouldBe false
      boundForm.get shouldBe validContent
    }

    "mandate 'description'" in {
      val invalidParams = validParams + ("description" -> "")
      val boundForm = form.bind(invalidParams)
      boundForm.hasErrors shouldBe true
    }

    "mandate 'item-name'" in {
      val invalidParams = validParams + ("item-name" -> "")
      val boundForm = form.bind(invalidParams)
      boundForm.hasErrors shouldBe true
    }
  }

}

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

package uk.gov.hmrc.bindingtariffadvicefrontend.model

import play.api.libs.json._
import uk.gov.hmrc.bindingtariffadvicefrontend.model
import uk.gov.hmrc.play.json.Union

sealed trait Email[T] {
  val to: Seq[String]
  val templateId: String
  val parameters: T // Must render to JSON as a Map[String, String]
  val force: Boolean = false
  val eventUrl: Option[String] = None
  val onSendUrl: Option[String] = None
}

object Email {
  implicit val format: Format[Email[_]] =
    Union.from[Email[_]]("templateId")
    .and[AdviceRequestEmail](EmailType.ADVICE_REQUEST.toString)
    .format
}

case class AdviceRequestEmail
(
  override val to: Seq[String],
  override val parameters: AdviceRequestEmailParameters
) extends Email[AdviceRequestEmailParameters] {
  override val templateId: String = EmailType.ADVICE_REQUEST.toString
}

object AdviceRequestEmail {
  implicit val format: OFormat[AdviceRequestEmail] = Json.format[AdviceRequestEmail]
}

case class AdviceRequestEmailParameters
(
  reference: String,
  contactName: String,
  contactEmail: String,
  itemName: String,
  itemDescription: String,
  supportingDocuments: String,
  supportingInformation: String
)

object AdviceRequestEmailParameters {
  implicit val format: OFormat[AdviceRequestEmailParameters] = Json.format[AdviceRequestEmailParameters]
}


object EmailType extends Enumeration {
  type EmailType = Value
  val ADVICE_REQUEST: model.EmailType.Value = Value("digital_tariffs_advice_request")
  implicit val format: Format[model.EmailType.Value] = Format(Reads.enumNameReads(EmailType), Writes.enumNameWrites)
}
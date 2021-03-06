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

package uk.gov.hmrc.bindingtariffadvicefrontend.model

import java.time.Instant
import java.util.UUID

import play.api.libs.json._

case class Advice
(
  id: String,
  reference: Option[String] = None,
  contactDetails: Option[ContactDetails] = None,
  goodDetails: Option[GoodDetails] = None,
  supportingDocuments: Seq[SupportingDocument] = Seq.empty,
  supportingInformation: Option[String] = None
)

object Advice {
  private val baseWrites: OWrites[Advice] = Json.writes[Advice]
  private val writes: OWrites[Advice] = OWrites(
    baseWrites.writes(_) + ("lastUpdated" -> Instants.format.writes(Instant.now()))
  )

  implicit val format: OFormat[Advice] = OFormat(Json.reads[Advice], writes)
}

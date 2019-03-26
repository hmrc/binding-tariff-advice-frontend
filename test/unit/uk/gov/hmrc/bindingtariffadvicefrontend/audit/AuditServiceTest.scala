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

package uk.gov.hmrc.bindingtariffadvicefrontend.audit

import org.mockito.Mockito.verify
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.bindingtariffadvicefrontend.audit.AuditPayloadType.BTIAdviceSubmission
import uk.gov.hmrc.bindingtariffadvicefrontend.model.{Advice, ContactDetails, GoodDetails, SupportingDocument}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.audit.DefaultAuditConnector
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

class AuditServiceTest extends UnitSpec with MockitoSugar {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val auditConnector: DefaultAuditConnector = mock[DefaultAuditConnector]

  private val service = new AuditService(auditConnector)

  "BTI advice auditing" should {

    val advice = Advice(
      id = "id",
      reference = Some("ref"),
      contactDetails = Some(ContactDetails("name", "email")),
      goodDetails = Some(GoodDetails("itemName", "description")),
      supportingDocuments = Seq(
        SupportingDocument("doc1", "doc-name1", "doc-type1", 1),
        SupportingDocument("doc2", "doc-name2", "doc-type2", 2)
      )
    )

    val auditJson = AdviceAuditPayload.from(advice)

    "call the audit connector as expected " in {

      service.auditBTIAdviceSubmission(advice)

      verify(auditConnector).sendExplicitAudit(BTIAdviceSubmission, auditJson)(hc, global, AdviceAuditPayload.format)
    }
  }

}

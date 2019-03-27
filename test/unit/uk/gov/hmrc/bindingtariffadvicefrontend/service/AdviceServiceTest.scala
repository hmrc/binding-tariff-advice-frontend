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

package uk.gov.hmrc.bindingtariffadvicefrontend.service

import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito.verify
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.libs.Files.TemporaryFile
import play.api.libs.json.Writes
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.bindingtariffadvicefrontend.audit.AuditService
import uk.gov.hmrc.bindingtariffadvicefrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffadvicefrontend.connector.EmailConnector
import uk.gov.hmrc.bindingtariffadvicefrontend.model._
import uk.gov.hmrc.bindingtariffadvicefrontend.repository.AdviceRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.routes

import scala.concurrent.Future

class AdviceServiceTest extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private implicit val request: Request[_] = FakeRequest()

  private val repository = mock[AdviceRepository]
  private val fileService = mock[FileService]
  private val emailConnector = mock[EmailConnector]
  private val appConfig = mock[AppConfig]
  private val auditService = mock[AuditService]
  private val service = new AdviceService(repository, fileService, auditService, emailConnector, appConfig)

  override def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(repository, fileService, emailConnector, auditService, appConfig)
  }

  "Get" should {
    val advice = mock[Advice]

    "Delegate to Repository" in {
      given(repository.get("id")) willReturn Future.successful(Some(advice))
      await(service.get("id")) shouldBe Some(advice)
    }
  }

  "Insert" should {
    val advice = mock[Advice]
    val adviceInserted = mock[Advice]

    "Delegate to Repository" in {
      given(repository.update(advice, upsert = true)) willReturn Future.successful(adviceInserted)
      await(service.insert(advice)) shouldBe adviceInserted
    }
  }

  "Update" should {
    val advice = mock[Advice]
    val adviceUpdated = mock[Advice]

    "Delegate to Repository" in {
      given(repository.update(advice, upsert = false)) willReturn Future.successful(adviceUpdated)
      await(service.update(advice)) shouldBe adviceUpdated
    }
  }

  "Delete" should {
    "Delegate to Repository" in {
      given(repository.delete(anyString())) willReturn Future.successful(())

      await(service.delete("id"))

      verify(repository).delete("id")
    }
  }

  "Submit" should {
    val contactDetails = ContactDetails("contact-name", "contact-email")
    val goodDetails = GoodDetails("item-name", "item-description")
    val supportingDocument1 = SupportingDocument("file-id1", "file-name1", "file-type1", 0)
    val supportingDocument2 = SupportingDocument("file-id2", "file-name2", "file-type2", 0)

    val fileSubmitted1 = FileSubmitted("file-published-id1", "file-published-name1", "file-published-type1")
    val fileSubmitted2 = FileSubmitted("file-published-id2", "file-published-name2", "file-published-type2")

    val advice = Advice(
      id = "123456789abcdefghijklmnopqrstuvwxyz",
      contactDetails = Some(contactDetails),
      goodDetails = Some(goodDetails),
      supportingDocuments = Seq(supportingDocument1, supportingDocument2),
      supportingInformation = Some("supporting-info")
    )
    val adviceUpdated = advice.copy(reference = Some("XYZ"))

    "Delegate to Repository" in {
      given(appConfig.submissionMailbox) willReturn "mailbox"
      given(repository.update(any[Advice], refEq(false))) willReturn Future.successful(adviceUpdated)
      given(fileService.publish(refEq(supportingDocument1))(any[HeaderCarrier])) willReturn Future.successful(fileSubmitted1)
      given(fileService.publish(refEq(supportingDocument2))(any[HeaderCarrier])) willReturn Future.successful(fileSubmitted2)
      given(emailConnector.send(any[AdviceRequestEmail])(any[HeaderCarrier], any[Writes[Any]])) willReturn Future.successful(())

      await(service.submit(advice)) shouldBe adviceUpdated

      theAuditEvent shouldBe adviceUpdated

      theAdviceUpdated.reference shouldBe Some("XYZ")

      val email = theEmailSent
      email.to shouldBe Seq("mailbox")
      email.templateId shouldBe "digital_tariffs_advice_request"
      email.parameters shouldBe AdviceRequestEmailParameters(
        reference = "XYZ",
        contactName = "contact-name",
        contactEmail = "contact-email",
        itemName = "item-name",
        itemDescription = "item-description",
        supportingDocuments = routes.ViewSupportingDocumentController.get("file-published-id1").absoluteURL() + "|" + routes.ViewSupportingDocumentController.get("file-published-id2").absoluteURL(),
        supportingInformation = "supporting-info"
      )
    }

    "Fail on missing Contact Details" in {
      intercept[IllegalArgumentException] {
        await(service.submit(advice.copy(contactDetails = None)))
      } getMessage() shouldBe "Cannot Submit without Contact Details"
    }

    "Fail on missing Good Details" in {
      intercept[IllegalArgumentException] {
        await(service.submit(advice.copy(goodDetails = None)))
      } getMessage() shouldBe "Cannot Submit without Good Details"
    }
  }

  "Upload" should {
    val file = mock[TemporaryFile]
    val fileUpload = FileUpload("name", "type")
    val fileUploaded = FileUploaded("id", "name", "type")

    "Delegate to Connectors" in {
      given(fileService.upload(refEq(fileUpload), refEq(file))(any[HeaderCarrier])) willReturn Future.successful(fileUploaded)
      await(service.upload(fileUpload, file)) shouldBe fileUploaded
    }
  }

  private def theAdviceUpdated: Advice = {
    val captor: ArgumentCaptor[Advice] = ArgumentCaptor.forClass(classOf[Advice])
    verify(repository).update(captor.capture(), refEq(false))
    captor.getValue
  }

  private def theEmailSent: AdviceRequestEmail = {
    val captor: ArgumentCaptor[AdviceRequestEmail] = ArgumentCaptor.forClass(classOf[AdviceRequestEmail])
    verify(emailConnector).send(captor.capture())(any[HeaderCarrier], any[Writes[Any]])
    captor.getValue
  }

  private def theAuditEvent: Advice = {
    val captor: ArgumentCaptor[Advice] = ArgumentCaptor.forClass(classOf[Advice])
    verify(auditService).auditBTIAdviceSubmission(captor.capture())(any[HeaderCarrier])
    captor.getValue
  }

}

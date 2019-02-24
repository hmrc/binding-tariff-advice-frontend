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

import org.mockito.{ArgumentCaptor, Mockito}
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito.verify
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.libs.Files.TemporaryFile
import uk.gov.hmrc.bindingtariffadvicefrontend.connector.{FileStoreConnector, UpscanS3Connector}
import uk.gov.hmrc.bindingtariffadvicefrontend.model.{Advice, FileUpload, FileUploadTemplate, FileUploaded}
import uk.gov.hmrc.bindingtariffadvicefrontend.repository.AdviceRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class AdviceServiceTest extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val repository = mock[AdviceRepository]
  private val fileStoreConnector = mock[FileStoreConnector]
  private val upscanS3Connector = mock[UpscanS3Connector]
  private val service = new AdviceService(repository, fileStoreConnector, upscanS3Connector)

  override def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(repository, fileStoreConnector, upscanS3Connector)
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
      given(repository.insert(advice)) willReturn Future.successful(adviceInserted)
      await(service.insert(advice)) shouldBe adviceInserted
    }
  }

  "Update" should {
    val advice = mock[Advice]
    val adviceUpdated = mock[Advice]

    "Delegate to Repository" in {
      given(repository.update(advice)) willReturn Future.successful(adviceUpdated)
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
    val advice = Advice(id = "123456789abcdefghijklmnopqrstuvwxyz")
    val adviceUpdated = mock[Advice]

    "Delegate to Repository" in {
      given(repository.update(any[Advice])) willReturn Future.successful(adviceUpdated)

      await(service.submit(advice)) shouldBe adviceUpdated

      theAdviceUpdated.reference shouldBe Some("XYZ")
    }
  }

  "Upload" should {
    val file = mock[TemporaryFile]
    val fileUpload = FileUpload("name", "type")
    val fileUploadTemplate = FileUploadTemplate("id", "href", Map())

    "Delegate to Connectors" in {
      given(fileStoreConnector.initiate(fileUpload)) willReturn Future.successful(fileUploadTemplate)
      given(upscanS3Connector.upload(refEq(fileUploadTemplate), refEq(file))(any[HeaderCarrier])) willReturn Future.successful(())
      await(service.upload(fileUpload, file)) shouldBe FileUploaded("id", "name", "type")
    }
  }

  private def theAdviceUpdated: Advice = {
    val captor: ArgumentCaptor[Advice] = ArgumentCaptor.forClass(classOf[Advice])
    verify(repository).update(captor.capture())
    captor.getValue
  }

}

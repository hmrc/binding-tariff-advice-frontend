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
import org.mockito.Mockito
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.libs.Files.TemporaryFile
import uk.gov.hmrc.bindingtariffadvicefrontend.connector.{FileStoreConnector, UpscanS3Connector}
import uk.gov.hmrc.bindingtariffadvicefrontend.model._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class FileServiceTest extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val fileStoreConnector = mock[FileStoreConnector]
  private val upscanS3Connector = mock[UpscanS3Connector]
  private val service = new FileService(fileStoreConnector, upscanS3Connector)

  override def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(fileStoreConnector, upscanS3Connector)
  }

  "Get" should {
    val file = mock[FileSubmitted]

    "Delegate to Connector" in {
      given(fileStoreConnector.get("id")) willReturn Future.successful(Some(file))
      await(service.get("id")) shouldBe Some(file)
    }
  }

  "Publish" should {
    val file = mock[FileSubmitted]
    val supportingDocument = SupportingDocument(id = "id", fileName = "name", mimeType = "type", size = 0)

    "Delegate to Connector" in {
      given(fileStoreConnector.publish("id")) willReturn Future.successful(file)
      await(service.publish(supportingDocument)) shouldBe file
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

}

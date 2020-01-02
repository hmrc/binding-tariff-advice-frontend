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

package uk.gov.hmrc.bindingtariffadvicefrontend.controllers

import akka.stream.Materializer
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{MaxSizeExceeded, MultipartFormData}
import play.api.test.Helpers.{charset, contentType, _}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.bindingtariffadvicefrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.action.{NewAnswers, ExistingAnswers, NormalMode}
import uk.gov.hmrc.bindingtariffadvicefrontend.model.{Advice, FileUpload, FileUploaded, SupportingDocument}
import uk.gov.hmrc.bindingtariffadvicefrontend.service.AdviceService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class UploadSupportingDocumentsControllerTest extends ControllerSpec {

  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val service = mock[AdviceService]
  private implicit val mat: Materializer = fakeApplication.materializer

  private def controller(advice: Advice) = new UploadSupportingDocumentsController(
    ExistingAnswers(advice),
    service,
    messageApi,
    appConfig,
    mat
  )


  "GET /" should {
    val advice = Advice("id")

    "return 200" in {
      val result = await(controller(advice).get(NormalMode)(getRequestWithCSRF))
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      bodyOf(result) should include("upload_supporting_documents-heading")
    }
  }

  "POST /" should {
    val fileName = "file.txt"
    val mimeType = "text/plain"
    val tmpFile = TemporaryFile("example-file.txt")
    val existingDocument = SupportingDocument("existing-id", "existing-id", "existing-type", 10)
    val advice = Advice("id", supportingDocuments = Seq(existingDocument))
    val updatedAdvice = mock[Advice]
    val fileUploaded = FileUploaded("id", "name", "type")

    "return 200 and redirect on valid form" in {
      given(service.upload(any[FileUpload], any[TemporaryFile])(any[HeaderCarrier])) willReturn Future.successful(fileUploaded)
      given(service.update(any[Advice])) willReturn Future.successful(updatedAdvice)

      val filePart = FilePart[TemporaryFile](key = "file", fileName, contentType = Some(mimeType), ref = tmpFile)
      val form = MultipartFormData[TemporaryFile](dataParts = Map(), files = Seq(filePart), badParts = Seq.empty)

      val request = postRequestWithCSRF.withBody(Right(form))
      val result = await(controller(advice).post(NormalMode)(request))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(routes.SupportingDocumentsController.get(NormalMode).url)

      theAdviceUpdated shouldBe Advice(
        "id",
        supportingDocuments = Seq(existingDocument, SupportingDocument("id", "name", "type", 0))
      )
      theFileUploaded shouldBe(FileUpload(fileName, mimeType), tmpFile)
    }

    def theAdviceUpdated: Advice = {
      val captor: ArgumentCaptor[Advice] = ArgumentCaptor.forClass(classOf[Advice])
      verify(service).update(captor.capture())
      captor.getValue
    }

    def theFileUploaded: (FileUpload, TemporaryFile) = {
      val fileCaptor: ArgumentCaptor[TemporaryFile] = ArgumentCaptor.forClass(classOf[TemporaryFile])
      val metadataCaptor: ArgumentCaptor[FileUpload] = ArgumentCaptor.forClass(classOf[FileUpload])
      verify(service).upload(metadataCaptor.capture(), fileCaptor.capture())(any[HeaderCarrier])
      (metadataCaptor.getValue, fileCaptor.getValue)
    }

    "return 200 with form errors - given missing filename" in {
      val filePart = FilePart[TemporaryFile](key = "file", "", contentType = Some(mimeType), ref = tmpFile)
      val form = MultipartFormData[TemporaryFile](dataParts = Map(), files = Seq(filePart), badParts = Seq.empty)

      val request = postRequestWithCSRF.withBody(Right(form))
      val result = await(controller(advice).post(NormalMode)(request))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      bodyOf(result) should include("supporting_documents-heading")
      bodyOf(result) should include("error-summary")
    }

    "return 200 with form errors - given missing content type" in {
      val filePart = FilePart[TemporaryFile](key = "file", fileName, contentType = None, ref = tmpFile)
      val form = MultipartFormData[TemporaryFile](dataParts = Map(), files = Seq(filePart), badParts = Seq.empty)

      val request = postRequestWithCSRF.withBody(Right(form))
      val result = await(controller(advice).post(NormalMode)(request))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      bodyOf(result) should include("supporting_documents-heading")
      bodyOf(result) should include("error-summary")
    }

    "return 200 with form errors - given file too large" in {
      val request = postRequestWithCSRF.withBody(Left(MaxSizeExceeded(0)))
      val result = await(controller(advice).post(NormalMode)(request))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      bodyOf(result) should include("supporting_documents-heading")
      bodyOf(result) should include("error-summary")
    }
  }

}

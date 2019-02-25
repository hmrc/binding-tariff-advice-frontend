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

package uk.gov.hmrc.bindingtariffadvicefrontend.controllers

import akka.stream.Materializer
import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.Files.TemporaryFile
import play.api.mvc._
import uk.gov.hmrc.bindingtariffadvicefrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.action.{Mode, RetrieveAnswersAction}
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.forms.FileForm
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.request.AnswersRequest
import uk.gov.hmrc.bindingtariffadvicefrontend.model.{Advice, FileUpload, FileUploaded, SupportingDocument}
import uk.gov.hmrc.bindingtariffadvicefrontend.service.AdviceService
import uk.gov.hmrc.bindingtariffadvicefrontend.views
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class UploadSupportingDocumentsController @Inject()(retrieveAnswers: RetrieveAnswersAction,
                                                    adviceService: AdviceService,
                                                    override val messagesApi: MessagesApi,
                                                    implicit val appConfig: AppConfig,
                                                    implicit val mat: Materializer) extends FrontendController with I18nSupport {

  def get(mode: Mode): Action[AnyContent] = retrieveAnswers.async { implicit request: AnswersRequest[AnyContent] =>
    Future.successful(Ok(views.html.upload_supporting_documents(FileForm.form, mode)))
  }

  def post(mode: Mode): Action[Either[MaxSizeExceeded, MultipartFormData[TemporaryFile]]] =
    retrieveAnswers.async(parse.maxLength(appConfig.fileUploadMaxSize, parse.multipartFormData)) {
      implicit request: AnswersRequest[Either[MaxSizeExceeded, MultipartFormData[TemporaryFile]]] =>

        def respondWithFormError(key: String, message: String): Future[Result] = {
          val form = FileForm.form.withError(key, message)
          Future.successful(Ok(views.html.upload_supporting_documents(form, mode)))
        }

        request.body match {
          case Right(form) if form.file("file").exists(_.filename.nonEmpty) && form.file("file").exists(_.contentType.isDefined) =>
            val optionalFile = form.file("file")
            val filename = optionalFile.map(_.filename).get
            val contentType = optionalFile.flatMap(_.contentType).get
            val file = optionalFile.map(_.ref).get
            val metadata = FileUpload(filename, contentType)
            for {
              uploaded: FileUploaded <- adviceService.upload(metadata, file)
              supportingDocument = SupportingDocument(uploaded, file.file.length())
              advice: Advice = request.advice.copy(supportingDocuments = request.advice.supportingDocuments :+ supportingDocument)
              _ <- adviceService.update(advice)
            } yield Redirect(routes.SupportingDocumentsController.get(mode))

          case Right(_) =>
            respondWithFormError("form_error.file_required", "File is required")

          case Left(MaxSizeExceeded(_)) =>
            respondWithFormError("form_error.file_too_large", "File size too large")
        }

    }

}

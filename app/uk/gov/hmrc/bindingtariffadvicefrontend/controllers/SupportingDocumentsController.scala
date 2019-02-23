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

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.bindingtariffadvicefrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.action.{RequireSessionAction, RetrieveAnswersAction}
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.forms.BooleanForm
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.request.AnswersRequest
import uk.gov.hmrc.bindingtariffadvicefrontend.service.AdviceService
import uk.gov.hmrc.bindingtariffadvicefrontend.views
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SupportingDocumentsController @Inject()(requireSession: RequireSessionAction,
                                              retrieveAnswers: RetrieveAnswersAction,
                                              adviceService: AdviceService,
                                              override val messagesApi: MessagesApi,
                                              implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  def get: Action[AnyContent] = (requireSession andThen retrieveAnswers).async { implicit request: AnswersRequest[AnyContent] =>
    Future.successful(Ok(views.html.supporting_documents(BooleanForm.form.fill(false))))
  }

  def post: Action[AnyContent] = (requireSession andThen retrieveAnswers).async { implicit request: AnswersRequest[AnyContent] =>
    def onError: Form[Boolean] => Future[Result] = formWithErrors => {
        Future.successful(Ok(views.html.supporting_documents(formWithErrors)))
    }

    def onSuccess: Boolean => Future[Result] = {
      case true => Future.successful(Redirect(routes.UploadSupportingDocumentsController.get()))
      case false => Future.successful(Redirect(routes.IndexController.get()))
    }

    BooleanForm.form.bindFromRequest.fold(onError, onSuccess)
  }

  def delete(id: String): Action[AnyContent] = (requireSession andThen retrieveAnswers).async { implicit request: AnswersRequest[AnyContent] =>
    val advice = request.advice.copy(supportingDocuments = request.advice.supportingDocuments.filterNot(_.id == id))
    adviceService.update(advice).map(_ => Redirect(routes.SupportingDocumentsController.get()))
  }

}

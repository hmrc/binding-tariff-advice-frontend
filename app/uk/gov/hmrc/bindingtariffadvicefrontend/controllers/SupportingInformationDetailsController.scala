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
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.action.{Mode, InitializeAnswersAction, RetrieveAnswersAction}
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.forms.TextForm
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.request.AnswersRequest
import uk.gov.hmrc.bindingtariffadvicefrontend.service.AdviceService
import uk.gov.hmrc.bindingtariffadvicefrontend.views
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SupportingInformationDetailsController @Inject()(retrieveAnswers: RetrieveAnswersAction,
                                                       adviceService: AdviceService,
                                                       override val messagesApi: MessagesApi,
                                                       implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  def get(mode: Mode): Action[AnyContent] = retrieveAnswers.async { implicit request: AnswersRequest[AnyContent] =>
    val form: Form[String] = request.advice.supportingInformation.map(TextForm.form.fill).getOrElse(TextForm.form)
    Future.successful(Ok(views.html.supporting_information_details(form, mode)))
  }

  def post(mode: Mode): Action[AnyContent] = retrieveAnswers.async { implicit request: AnswersRequest[AnyContent] =>
    def onError: Form[String] => Future[Result] = formWithErrors => {
        Future.successful(Ok(views.html.supporting_information_details(formWithErrors, mode)))
    }

    def onSuccess: String => Future[Result] = supportingInformation => {
        val updated = request.advice.copy(supportingInformation = Some(supportingInformation))
        adviceService.update(updated).map(_ => Redirect(routes.CheckYourAnswersController.get()))
    }

    TextForm.form.bindFromRequest.fold(onError, onSuccess)
  }

}

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
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.action._
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.forms.ContactDetailsForm
import uk.gov.hmrc.bindingtariffadvicefrontend.controllers.request.AnswersRequest
import uk.gov.hmrc.bindingtariffadvicefrontend.model.ContactDetails
import uk.gov.hmrc.bindingtariffadvicefrontend.service.AdviceService
import uk.gov.hmrc.bindingtariffadvicefrontend.views
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ContactDetailsController @Inject()(requireSession: RequireSessionAction,
                                         retrieveAnswers: RetrieveAnswersAction,
                                         adviceService: AdviceService,
                                         override val messagesApi: MessagesApi,
                                         implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  def get(mode: Mode): Action[AnyContent] = (requireSession andThen retrieveAnswers).async {
    implicit request: AnswersRequest[AnyContent] =>
      val form: Form[ContactDetails] = request.advice.contactDetails.map(ContactDetailsForm.form.fill).getOrElse(ContactDetailsForm.form)
      Future.successful(Ok(views.html.contact_details(form, mode)))
  }

  def post(implicit mode: Mode): Action[AnyContent] = (requireSession andThen retrieveAnswers).async {
    implicit request: AnswersRequest[AnyContent] =>
      def onError: Form[ContactDetails] => Future[Result] = formWithErrors => {
        Future.successful(Ok(views.html.contact_details(formWithErrors, mode)))
      }

      def onSuccess: ContactDetails => Future[Result] = contactDetails => {
        val updated = request.advice.copy(contactDetails = Some(contactDetails))
        adviceService.update(updated).map(_ => Navigator.redirect(routes.GoodDetailsController.get(mode)))
      }

      ContactDetailsForm.form.bindFromRequest.fold(onError, onSuccess)
  }

}

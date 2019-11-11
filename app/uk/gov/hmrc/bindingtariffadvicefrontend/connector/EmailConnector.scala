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

package uk.gov.hmrc.bindingtariffadvicefrontend.connector

import com.google.inject.{Inject, ProvidedBy, Provider, Provides}
import javax.inject.Singleton
import org.slf4j.LoggerFactory
import play.api.libs.json.Writes
import uk.gov.hmrc.bindingtariffadvicefrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffadvicefrontend.model.Email
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ProvidedBy(classOf[EmailConnectorProvider])
trait EmailConnector{
  def send[E >: Email[Any]](e: E)(implicit hc: HeaderCarrier, writes: Writes[E]): Future[Unit]
}

class EmailConnectorProvider @Inject()(appConfig:AppConfig,
                             fakeEmailConnector: FakeEmailConnector,
                             emailServiceConnector: EmailServiceConnector) extends Provider[EmailConnector] {
  val logger = LoggerFactory.getLogger(classOf[EmailConnectorProvider])

  override def get(): EmailConnector = {
    if (appConfig.submissionEmailEnabled) emailServiceConnector
    else fakeEmailConnector
  }
}

@Singleton
class EmailServiceConnector @Inject()(configuration: AppConfig, client: HttpClient) extends EmailConnector {

  def send[E >: Email[Any]](e: E)(implicit hc: HeaderCarrier, writes: Writes[E]): Future[Unit] = {
    val url = s"${configuration.emailUrl}/hmrc/email"
    client.POST(url = url, body = e).map(_ => ())
  }
}

@Singleton
class FakeEmailConnector() extends EmailConnector {
  val logger = LoggerFactory.getLogger(classOf[FakeEmailConnector])

  override def send[E >: Email[Any]](e: E)(implicit hc: HeaderCarrier, writes: Writes[E]): Future[Unit] = {
    Future {logger.info("Email requested but not sent")}
  }
}
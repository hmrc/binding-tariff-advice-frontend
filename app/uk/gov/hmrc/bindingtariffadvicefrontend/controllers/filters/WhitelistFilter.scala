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

package uk.gov.hmrc.bindingtariffadvicefrontend.controllers.filters

import akka.stream.Materializer
import com.google.inject.Inject
import play.api.http.HttpVerbs
import play.api.mvc._
import uk.gov.hmrc.bindingtariffadvicefrontend.config.AppConfig

import scala.concurrent.Future

class WhitelistFilter @Inject()(appConfig: AppConfig,
                      override val mat: Materializer) extends Filter {

  private val excluded: Set[Call] = Set(Call(HttpVerbs.GET, "/ping/ping"))

  override def apply(f: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {
    if(excluded.contains(Call(rh.method, rh.uri))) {
      f(rh)
    } else {
      appConfig.whitelist match {
        case Some(addresses: Set[String]) =>
          rh.headers.get("True-Client-IP") match {
            case Some(ip: String) if addresses.contains(ip) => f(rh)
            case _ => Future.successful(Results.Forbidden)
          }
        case _ => f(rh)
      }
    }
  }
}


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
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.{Headers, RequestHeader, Result, Results}
import uk.gov.hmrc.bindingtariffadvicefrontend.config.AppConfig
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class WhitelistFilterTest extends UnitSpec with MockitoSugar {

  private val header = mock[RequestHeader]
  private val headers = mock[Headers]
  private val block = mock[RequestHeader => Future[Result]]
  private val mat = mock[Materializer]
  private val config = mock[AppConfig]
  private val filter = new WhitelistFilter(config, mat)

  "Filter" should {
    given(header.headers) willReturn headers
    given(block.apply(any[RequestHeader])) willReturn Future.successful(Results.Ok)

    "pass through if endpoint excluded" in {
      given(headers.get("True-Client-IP")) willReturn None
      given(header.uri) willReturn "/ping/ping"
      given(header.method) willReturn "GET"
      given(config.whitelist) willReturn Some(Set("ip"))

      await(filter(block)(header)) shouldBe Results.Ok
    }

    "pass through if disabled" in {
      given(headers.get("True-Client-IP")) willReturn None
      given(header.uri) willReturn "/"
      given(header.method) willReturn "GET"
      given(config.whitelist) willReturn None

      await(filter(block)(header)) shouldBe Results.Ok
    }

    "pass through if user has valid IP" in {
      given(headers.get("True-Client-IP")) willReturn Some("ip")
      given(header.uri) willReturn "/"
      given(header.method) willReturn "GET"
      given(config.whitelist) willReturn Some(Set("ip"))

      await(filter(block)(header)) shouldBe Results.Ok
    }

    "filter if user has no IP" in {
      given(headers.get("True-Client-IP")) willReturn None
      given(header.uri) willReturn "/"
      given(header.method) willReturn "GET"
      given(config.whitelist) willReturn Some(Set("ip"))

      await(filter(block)(header)) shouldBe Results.Forbidden
    }

    "filter if user has invalid IP" in {
      given(headers.get("True-Client-IP")) willReturn Some("unknown-ip")
      given(header.uri) willReturn "/"
      given(header.method) willReturn "GET"
      given(config.whitelist) willReturn Some(Set("ip"))

      await(filter(block)(header)) shouldBe Results.Forbidden
    }
  }

}

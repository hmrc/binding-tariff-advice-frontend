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

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern
import org.apache.http.HttpStatus
import org.mockito.Mockito.when
import uk.gov.hmrc.bindingtariffadvicefrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffadvicefrontend.model.{AdviceRequestEmail, AdviceRequestEmailParameters}

class EmailConnectorSpec extends ConnectorTest {
  "Real Connector 'Send'" should {
    val emailServiceConnector = new EmailServiceConnector(appConfig, standardHttpClient)

    "POST Email payload" in {
      stubFor(post(urlEqualTo("/hmrc/email"))
        .withRequestBody(new EqualToJsonPattern(fromResource("advice_request_email-request.json"), true, false))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_ACCEPTED))
      )

      val email = AdviceRequestEmail(
        to = Seq("user@domain.com"),
        replyToAddress = "reply-to@domain.com",
        parameters = AdviceRequestEmailParameters("ref", "name", "email", "item-name", "item-description", "supporting-docs", "supporting-info")
      )

      await(emailServiceConnector.send(email))

      verify(
        postRequestedFor(urlEqualTo("/hmrc/email"))
          .withoutHeader("X-Api-Token")
      )
    }
  }

  "Fake Connector 'Send'" should {
    val fakeConnector = new FakeEmailConnector()

    "Not POST Email payload" in {

      resetAllRequests()

      val email = AdviceRequestEmail(
        to = Seq("user@domain.com"),
        replyToAddress = "reply-to@domain.com",
        parameters = AdviceRequestEmailParameters("ref", "name", "email", "item-name", "item-description", "supporting-docs", "supporting-info")
      )

      await(fakeConnector.send(email))
      getAllServeEvents.size() shouldBe(0)
    }
  }

  "Provider provides the correct instance based on config" in {
    val mockConfig = mock[AppConfig]
    when(mockConfig.submissionEmailEnabled).thenReturn(false).thenReturn(true)
    val OUT = new EmailConnectorProvider(mockConfig, new FakeEmailConnector(), new EmailServiceConnector(mockConfig, standardHttpClient))

    val result1 = OUT.get()
    result1 shouldBe a[FakeEmailConnector]
    val result2 = OUT.get()
    result2 shouldBe a[EmailServiceConnector]
  }

}

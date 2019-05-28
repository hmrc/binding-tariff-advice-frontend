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

package uk.gov.hmrc.bindingtariffadvicefrontend.config

import java.util.concurrent.TimeUnit

import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.duration.FiniteDuration

class AppConfigTest extends UnitSpec with WithFakeApplication {

  private def appConfig(pairs: (String, String)*): AppConfig = {
    new AppConfig(Configuration.from(pairs.map(e => e._1 -> e._2).toMap), Environment.simple())
  }

  "Build assets prefix" in {
    appConfig(
      "assets.url" -> "http://localhost:9032/assets/",
      "assets.version" -> "4.5.0"
    ).assetsPrefix shouldBe "http://localhost:9032/assets/4.5.0"
  }

  "Build analytics token" in {
    appConfig("google-analytics.token" -> "N/A").analyticsToken shouldBe "N/A"
  }

  "Build analytics host" in {
    appConfig("google-analytics.host" -> "auto").analyticsHost shouldBe "auto"
  }

  "Build report url" in {
    appConfig("contact-frontend.host" -> "host").reportAProblemPartialUrl shouldBe "host/contact/problem_reports_ajax?service=BindingTariffAdvice"
  }

  "Build report non-json url" in {
    appConfig("contact-frontend.host" -> "host").reportAProblemNonJSUrl shouldBe "host/contact/problem_reports_nonjs?service=BindingTariffAdvice"
  }

  "Build 'Email' URL" in {
    appConfig(
      "microservice.services.email.host" -> "host",
      "microservice.services.email.port" -> "123"
    ).emailUrl shouldBe "http://host:123"
  }

  "Build 'Filestore' URL" in {
    appConfig(
      "microservice.services.binding-tariff-filestore.protocol" -> "https",
      "microservice.services.binding-tariff-filestore.host" -> "www.host.co.uk",
      "microservice.services.binding-tariff-filestore.port" -> "123"
    ).fileStoreUrl shouldBe "https://www.host.co.uk:123"
  }

  "Build 'Mongo TTL'" in {
    appConfig("mongodb.ttl" -> "1h").mongoTTL shouldBe FiniteDuration(1, TimeUnit.HOURS)
  }

  "Build 'Upload Max Size'" in {
    appConfig("upload.max-size" -> "10").fileUploadMaxSize shouldBe 10
  }

  "Build 'Upload Mime Types'" in {
    appConfig("upload.mime-types" -> "application/pdf, image/jpeg").fileUploadMimeTypes shouldBe Seq("application/pdf", "image/jpeg")
  }

  "Build 'Submission Mailbox'" in {
    appConfig("submission.mailbox" -> "hmrc@hmrc.gov.uk").submissionMailbox shouldBe "hmrc@hmrc.gov.uk"
  }

  "Build API Token" in {
    appConfig("auth.api-token" -> "token").apiToken shouldBe "token"
  }

  "Build Host" in {
    appConfig("host" -> "url").host shouldBe "url"
  }

  "Build whitelist" in {
    appConfig(
      "filters.whitelist.enabled" -> "true",
      "filters.whitelist.ips" -> "ip1, ip2"
    ).whitelist shouldBe Some(Set("ip1", "ip2"))

    appConfig("filters.whitelist.enabled" -> "false").whitelist shouldBe None
  }
}

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

import com.typesafe.config.ConfigValueType
import javax.inject.{Inject, Singleton}
import org.slf4j.LoggerFactory
import play.api.Mode.Mode
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.config.ServicesConfig

import scala.annotation.tailrec
import scala.concurrent.duration.Duration
import scala.util.Try

@Singleton
class AppConfig @Inject()(val runModeConfiguration: Configuration, environment: Environment) extends ServicesConfig {

  val logger = LoggerFactory.getLogger(classOf[AppConfig])

  def logConfigValues() = {
    @tailrec
    def buildLogMessage(keys: List[String], f: String => String, current: String): String = {
      keys match {
        case Nil => current
        case head :: tail if head.startsWith("submission") => buildLogMessage(tail, f, s"${current}\r\n ${head} =  ${f(head)}")
        case _ :: tail => buildLogMessage(tail, f, current)
      }
    }

    def key2String(key: String): String = {
      val configValue = runModeConfiguration.underlying.getValue(key)
      configValue.toString
    }
    logger.info(buildLogMessage(runModeConfiguration.keys.toList, key2String, ""))
  }

  val init = logConfigValues()

  override protected def mode: Mode = environment.mode

  private def loadConfig(key: String) = runModeConfiguration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private val contactHost = runModeConfiguration.getString(s"contact-frontend.host").getOrElse("")
  private val contactFormServiceIdentifier = "BindingTariffAdvice"

  lazy val assetsPrefix: String = loadConfig(s"assets.url") + loadConfig(s"assets.version")
  lazy val analyticsToken: String = loadConfig(s"google-analytics.token")
  lazy val analyticsHost: String = loadConfig(s"google-analytics.host")
  lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val betaFeedbackUrl = s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier"
  lazy val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated?service=$contactFormServiceIdentifier"
  lazy val emailUrl: String = baseUrl("email")
  lazy val fileStoreUrl: String = baseUrl("binding-tariff-filestore")
  lazy val mongoTTL: Duration = getDuration("mongodb.ttl")
  lazy val fileUploadMaxSize: Long = loadConfig("upload.max-size").toInt
  lazy val fileUploadMimeTypes: Seq[String] = loadConfig("upload.mime-types").split(",").map(_.trim)
  lazy val submissionMailbox: String = loadConfig("submission.mailbox")
  lazy val submissionEmailEnabled: Boolean = Try(getBoolean("submission.email.enabled")).recover {case ex => {
    logger.error("Failed to configure submission email. defaulting to true", ex)
    true
  }}.get
  lazy val apiToken: String = loadConfig("auth.api-token")
  lazy val host: String = loadConfig("host")
  lazy val whitelist: Option[Set[String]] = {
    if (getBoolean("filters.whitelist.enabled")) {
      Some[Set[String]](
        getString("filters.whitelist.ips")
          .split(",")
          .map(_.trim)
          .filter(_.nonEmpty)
          .toSet
      )
    } else None
  }

}

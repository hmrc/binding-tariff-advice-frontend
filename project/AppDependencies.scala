import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "commons-validator"         %  "commons-validator"          % "1.6",
    "io.megl"                   %% "play-json-extra"            % "2.4.3",
    "org.apache.httpcomponents" %  "httpclient"                 % "4.5.7",
    "org.apache.httpcomponents" %  "httpmime"                   % "4.5.7",
    "uk.gov.hmrc"               %% "bootstrap-play-25"          % "4.9.0",
    "uk.gov.hmrc"               %% "govuk-template"             % "5.29.0-play-25",
    "uk.gov.hmrc"               %% "play-ui"                    % "7.33.0-play-25",
    "uk.gov.hmrc"               %% "play-json-union-formatter"  % "1.5.0",
    "uk.gov.hmrc"               %% "simple-reactivemongo"       % "7.14.0-play-25"
  )

  val scope = "test, it"

  val test = Seq(
    "com.github.tomakehurst"  %  "wiremock"                 % "2.21.0"        % scope,
    "com.typesafe.play"       %% "play-test"                % current         % scope,
    "org.mockito"             %  "mockito-core"             % "2.25.0"        % scope,
    "org.jsoup"               %  "jsoup"                    % "1.11.3"        % scope,
    "org.pegdown"             %  "pegdown"                  % "1.6.0"         % scope,
    "org.scalatest"           %% "scalatest"                % "3.0.4"         % scope,
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "2.0.1"         % scope,
    "uk.gov.hmrc"             %% "hmrctest"                 % "3.6.0-play-25" % scope,
    "uk.gov.hmrc"             %% "http-verbs-test"          % "1.4.0-play-25" % scope,
    "uk.gov.hmrc"             %% "reactivemongo-test"       % "4.9.0-play-25" % scope,
    "uk.gov.hmrc"             %% "service-integration-test" % "0.6.0-play-25" % scope
  )

}

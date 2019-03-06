package uk.gov.hmrc.bindingtariffadvicefrontend

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

class ExampleIntegrationTest extends WiremockFeatureTestServer with ResourceFiles {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  feature("TODO") {
    scenario("TODO") {

    }
  }

}

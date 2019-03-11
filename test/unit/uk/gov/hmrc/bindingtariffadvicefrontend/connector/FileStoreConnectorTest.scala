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
import play.api.http.Status
import uk.gov.hmrc.bindingtariffadvicefrontend.model.{FileSubmitted, FileUpload, FileUploadTemplate}

class FileStoreConnectorTest extends ConnectorTest {

  private val connector = new FileStoreConnector(appConfig, authenticatedHttpClient)

  "Connector Delete" should {
    "DELETE from the File Store" in {
      stubFor(
        delete("/file/id")
          .willReturn(
            aResponse()
              .withStatus(Status.NO_CONTENT)
          )
      )

      await(connector.delete("id")) shouldBe ((): Unit)

      verify(
        deleteRequestedFor(urlEqualTo("/file/id"))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }
  }

  "Connector Delete All" should {
    "DELETE from the File Store" in {
      stubFor(
        delete("/file")
          .willReturn(
            aResponse()
              .withStatus(Status.NO_CONTENT)
          )
      )

      await(connector.delete) shouldBe ((): Unit)

      verify(
        deleteRequestedFor(urlEqualTo("/file"))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }
  }

  "Connector Initiate" should {
    "POST to the File Store" in {
      stubFor(
        post("/file")
          .withHeader("Content-Type", equalTo("application/json"))
          .withRequestBody(matchingJsonPath("fileName", equalTo("file name.jpg")))
          .withRequestBody(matchingJsonPath("mimeType", equalTo("type")))
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
              .withBody(fromResource("filestore-initiate_response.json"))
          )
      )

      val file = FileUpload(fileName = "file name.jpg", mimeType = "type")
      val response = await(connector.initiate(file))

      response shouldBe FileUploadTemplate("id", "url", Map("field" -> "value"))

      verify(
        postRequestedFor(urlEqualTo("/file"))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }
  }

  "Connector Get" should {

    "GET from the File Store" in {
      stubFor(
        get("/file/id")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(fromResource("filestore-publish_response.json"))
          )
      )

      await(connector.get("id")) shouldBe Some(FileSubmitted(
        id = "id",
        fileName = "file-name.txt",
        mimeType = "text/plain"
      ))

      verify(
        getRequestedFor(urlEqualTo("/file/id"))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }

    "GET none from the File Store" in {
      stubFor(
        get("/file/id")
          .willReturn(
            aResponse()
              .withStatus(Status.NOT_FOUND)
          )
      )

      await(connector.get("id")) shouldBe None

      verify(
        getRequestedFor(urlEqualTo("/file/id"))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }
  }

  "Connector Get Many" should {

    "GET from the File Store" in {
      stubFor(
        get("/file?id=id")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(fromResource("filestore-search_response.json"))
          )
      )

      await(connector.get(Seq("id"))) shouldBe Seq(FileSubmitted(
        id = "id",
        fileName = "file-name.txt",
        mimeType = "text/plain"
      ))

      verify(
        getRequestedFor(urlEqualTo("/file?id=id"))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }
  }

  "Connector Publish" should {

    "POST to the File Store" in {
      stubFor(
        post("/file/id/publish")
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
              .withBody(fromResource("filestore-publish_response.json"))
          )
      )

      await(connector.publish("id")) shouldBe FileSubmitted(
        id = "id",
        fileName = "file-name.txt",
        mimeType = "text/plain"
      )

      verify(
        postRequestedFor(urlEqualTo("/file/id/publish"))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }
  }

}

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

package uk.gov.hmrc.bindingtariffadvicefrontend.repository

import org.mockito.BDDMockito.given
import org.scalatest.concurrent.Eventually
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import reactivemongo.api.DB
import reactivemongo.api.indexes.IndexType
import reactivemongo.bson.{BSONDocument, BSONLong}
import reactivemongo.play.json.collection.JSONCollection
import uk.gov.hmrc.bindingtariffadvicefrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffadvicefrontend.model.Advice
import uk.gov.hmrc.mongo.MongoSpecSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class AdviceMongoRepositoryTest extends MongoUnitSpec
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with MongoSpecSupport
  with Eventually
  with MockitoSugar {
  self =>

  private val provider: MongoDbProvider = new MongoDbProvider {
    override val mongo: () => DB = self.mongo
  }

  private val config = mock[AppConfig]

  private def repository = new AdviceMongoRepository(config, provider)

  override protected def collection: JSONCollection = repository.collection

  override def beforeEach(): Unit = {
    super.beforeEach()
    given(config.mongoTTL).willReturn(10 seconds)
    await(repository.drop)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    await(repository.drop)
  }

  "Repository" should {
    "Have TTL index" in {
      await(repository.ensureIndexes)

      eventually(timeout(5.seconds), interval(100.milliseconds)) {
        val index = getIndex("expiry")
        index.map(_.options) shouldBe Some(BSONDocument("expireAfterSeconds" -> BSONLong(10)))
        index.map(_.key) shouldBe Some(Seq(("lastUpdated", IndexType.Ascending)))
      }
    }
  }

  "Update" should {
    "Update One" in {
      val document = Advice(id = "id")
      givenAnExistingDocument(document)

      val update = document.copy(reference = Some("ref"))

      await(repository.update(update, upsert = false)) shouldBe update
    }
  }

  "Delete" should {
    "Delete One" in {
      givenAnExistingDocument(Advice(id = "id1"))
      givenAnExistingDocument(Advice(id = "id2"))

      await(repository.delete("id1"))

      thenTheDocumentCountShouldBe(1)
    }
  }

  "Get" should {
    "Retrieve None" in {
      await(repository.get("some id")) shouldBe None
    }

    "Retrieve One" in {
      // Given
      val document = Advice(id = "id")
      givenAnExistingDocument(document)

      await(repository.get("id")) shouldBe Some(document)
    }
  }

  private def givenAnExistingDocument(advice: Advice): Unit = {
    await(repository.collection.insert(advice))
  }

  private def thenTheDocumentCountShouldBe(count: Int): Unit = {
    await(repository.collection.count()) shouldBe count
  }

}

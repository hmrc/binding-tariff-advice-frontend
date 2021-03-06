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

package uk.gov.hmrc.bindingtariffadvicefrontend.repository

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsObject, Json}
import reactivemongo.api.indexes.Index
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.ImplicitBSONHandlers._
import uk.gov.hmrc.bindingtariffadvicefrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffadvicefrontend.model.Advice
import uk.gov.hmrc.bindingtariffadvicefrontend.repository.MongoIndexCreator.{createSingleFieldAscendingIndex, createTTLIndex}
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[AdviceMongoRepository])
trait AdviceRepository {

  def update(advice: Advice, upsert: Boolean): Future[Advice]

  def get(id: String): Future[Option[Advice]]

  def delete(id: String): Future[Unit]

}

@Singleton
class AdviceMongoRepository @Inject()(config: AppConfig,
                                      mongoDbProvider: MongoDbProvider)
  extends ReactiveRepository[Advice, BSONObjectID](
    collectionName = "advice",
    mongo = mongoDbProvider.mongo,
    domainFormat = Advice.format) with AdviceRepository {

  override lazy val indexes: Seq[Index] = Seq(
    createSingleFieldAscendingIndex("id", isUnique = true),
    createTTLIndex(config.mongoTTL)
  )

  override def ensureIndexes(implicit ec: ExecutionContext): Future[Seq[Boolean]] = {
    Future.sequence(indexes.map(collection.indexesManager.ensure(_)))
  }

  override def update(advice: Advice, upsert: Boolean): Future[Advice] = collection.findAndUpdate(
    selector = byId(advice.id),
    update = advice,
    fetchNewObject = true,
    upsert = upsert
  ).map(_.value.map(_.as[Advice]).get)

  override def get(id: String): Future[Option[Advice]] = collection.find(byId(id)).one[Advice]

  override def delete(id: String): Future[Unit] = collection.findAndRemove(byId(id)).map(_ => ())

  private def byId(id: String): JsObject = {
    Json.obj("id" -> id)
  }
}

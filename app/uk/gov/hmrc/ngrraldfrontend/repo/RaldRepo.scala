/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.ngrraldfrontend.repo

import com.google.inject.Singleton
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.*
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.{ascending, descending}
import org.mongodb.scala.model.Updates.combine
import play.api.Logging
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.ngrraldfrontend.config.FrontendAppConfig
import uk.gov.hmrc.ngrraldfrontend.models.RaldUserAnswers
import uk.gov.hmrc.ngrraldfrontend.models.components.Landlord
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId

import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
case class RaldRepo @Inject()(mongo: MongoComponent,
                              config: FrontendAppConfig
                             )(implicit ec: ExecutionContext)
  extends PlayMongoRepository[RaldUserAnswers](
    collectionName = "rald",
    mongoComponent = mongo,
    domainFormat = RaldUserAnswers.format,
    indexes = Seq(
      IndexModel(
        descending("createdAt"),
        IndexOptions()
          .unique(false)
          .name("createdAt")
          .expireAfter(config.timeToLive.toLong, TimeUnit.HOURS)
      ),
      IndexModel(
        ascending("credId.value"),
        IndexOptions()
          .background(false)
          .name("credId.value")
          .unique(true)
          .partialFilterExpression(Filters.gte("credId.value", ""))
      )
    )
  ) with Logging {

  override lazy val requiresTtlIndex: Boolean = false

  private def filterByCredId(credId: CredId): Bson = equal("credId.value", credId.value)

  def upsertRaldUserAnswers(raldUserAnswers: RaldUserAnswers): Future[Boolean] = {
    val errorMsg = s"Rald user answers has not been inserted"

    collection.replaceOne(
      filter = equal("credId.value", raldUserAnswers.credId.value),
      replacement = raldUserAnswers,
      options = ReplaceOptions().upsert(true)
    ).toFuture().transformWith {
      case Success(result) =>
        logger.info(s"Rald user answers has been upsert for credId: ${raldUserAnswers.credId.value}")
        Future.successful(result.wasAcknowledged())
      case Failure(exception) =>
        logger.error(errorMsg)
        Future.failed(new IllegalStateException(s"$errorMsg: ${exception.getMessage} ${exception.getCause}"))
    }
  }

  private def findAndUpdateByCredId(credId: CredId, updates: Bson*): Future[Option[RaldUserAnswers]] = {
    collection.findOneAndUpdate(filterByCredId(credId),
        combine(updates :+ Updates.set("createdAt", Instant.now()): _*),
        FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER))
      .toFutureOption()
  }

  def insertTypeOfAgreement(credId: CredId, whatTypeOfAgreement: String): Future[Option[RaldUserAnswers]] = {
    findAndUpdateByCredId(credId, Updates.set("whatTypeOfAgreement", whatTypeOfAgreement))
  }
  
  def insertLandlord(credId: CredId, landlordName: String, landLordType:String, landlordOther:Option[String]): Future[Option[RaldUserAnswers]] = {
    val name = Seq(Updates.set("landlordName", landlordName))
    val landlord = Updates.set("landLordType", landLordType)

    val radio = landlordOther match {
      case Some(otherDesc) =>
        name :+ landlord :+ Updates.set("landlordOtherDesc", otherDesc)
      case None =>
        name :+ landlord
    }
    findAndUpdateByCredId(credId = credId, radio: _*)
  }

  def insertTypeOfRenewal(credId: CredId, whatTypeOfRenewal: String): Future[Option[RaldUserAnswers]] = {
    findAndUpdateByCredId(credId, Updates.set("whatTypeOfRenewal", whatTypeOfRenewal))
  }
  
  def insertAnnualRent(credId: CredId, rentAmount: Int): Future[Option[RaldUserAnswers]] = {
    findAndUpdateByCredId(credId, Updates.set("rentAmount", rentAmount))
  }
  
  def findByCredId(credId: CredId): Future[Option[RaldUserAnswers]] = {
    collection.find(
      equal("credId.value", credId.value)
    ).headOption()
  }
}

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
    val name = Seq(Updates.set("landlord.landlordName", landlordName))
    val landlord = Updates.set("landlord.landLordType", landLordType)

    val radio = landlordOther match {
      case Some(otherDesc) =>
        name :+ landlord :+ Updates.set("landlord.landlordOtherDesc", otherDesc)
      case None =>
        name :+ landlord
    }
    findAndUpdateByCredId(credId = credId, radio: _*)
  }

  def insertAgreement(credId: CredId, agreementStart: String, openEndedRadio: String, openEndedDate: Option[String], breakClauseRadio: String, breakClauseInfo:Option[String]): Future[Option[RaldUserAnswers]] = {
    val startDate = Updates.set("agreement.agreementStart", agreementStart)
    val openEnded = Updates.set("agreement.isOpenEnded", openEndedRadio match{
      case answer if(answer == "YesOpenEnded") => true
      case _ => false
    })
    val breakClause = Updates.set("agreement.haveBreakClause", breakClauseRadio match {
      case openEndedRadio if(openEndedRadio == "YesBreakClause") => true
      case _ => false
    })
    val openEndedDateAnswer = Updates.set("agreement.openEndedDate", openEndedDate.orNull)
    val breakClauseInfoAnswer = Updates.set("agreement.breakClauseInfo", breakClauseInfo.orNull)

    val answers = Seq(startDate , openEnded , openEndedDateAnswer , breakClause , breakClauseInfoAnswer)
    findAndUpdateByCredId(credId = credId, answers: _*)
  }

  def insertProvideDetailsOfFirstSecondRentPeriod(
                               credId: CredId,
                               firstDateStart: String,
                               firstDateEnd: String,
                               firstRentPeriodRadio: String,
                               firstRentPeriodAmount: Option[String],
                               secondDateStart: String,
                               secondDateEnd: String,
                               secondHowMuchIsRent: BigDecimal
                             ): Future[Option[RaldUserAnswers]] = {

    val updates = Seq(
      Updates.set("provideDetailsOfFirstSecondRentPeriod.firstDateStart", firstDateStart),
      Updates.set("provideDetailsOfFirstSecondRentPeriod.firstDateEnd", firstDateEnd),
      Updates.set("provideDetailsOfFirstSecondRentPeriod.firstRentPeriodRadio", firstRentPeriodRadio match {
        case "yesPayedRent" => true
        case _ => false
      }),
      firstRentPeriodRadio match
        case "yesPayedRent" =>
          Updates.set("provideDetailsOfFirstSecondRentPeriod.firstRentPeriodAmount", firstRentPeriodAmount.get)
        case _ =>
          Updates.unset("provideDetailsOfFirstSecondRentPeriod.firstRentPeriodAmount"),
      Updates.set("provideDetailsOfFirstSecondRentPeriod.secondDateStart", secondDateStart),
      Updates.set("provideDetailsOfFirstSecondRentPeriod.secondDateEnd", secondDateEnd),
      Updates.set("provideDetailsOfFirstSecondRentPeriod.secondHowMuchIsRent", secondHowMuchIsRent.toString)
    )

    findAndUpdateByCredId(credId, updates: _*)
  }

  def insertRentBased(credId: CredId, rentBased: String, rentBasedOtherText:Option[String]): Future[Option[RaldUserAnswers]] = {
    val updates = Seq(Updates.set("rentBasedOn.rentBased", rentBased),
      Updates.set("rentBasedOn.otherDesc", rentBasedOtherText.orNull))
    findAndUpdateByCredId(credId, updates: _*)
  }

  def insertTypeOfRenewal(credId: CredId, whatTypeOfRenewal: String): Future[Option[RaldUserAnswers]] = {
    findAndUpdateByCredId(credId, Updates.set("whatTypeOfRenewal", whatTypeOfRenewal))
  }
  
  def insertAgreedRentChange(credId: CredId, agreedRentChange: String): Future[Option[RaldUserAnswers]] = {
    findAndUpdateByCredId(credId, Updates.set("agreedRentChange", agreedRentChange))
  }

  def insertHasRentFreePeriod(credId: CredId, hasRentFreePeriod: String): Future[Option[RaldUserAnswers]] = {
    hasRentFreePeriod match {
      case "Yes" => findAndUpdateByCredId(credId, Updates.set("hasRentFreePeriod", true))
      case _ => findAndUpdateByCredId(credId, Updates.set("hasRentFreePeriod", false))
    }
    
  }

  def insertAnnualRent(credId: CredId, rentAmount: BigDecimal): Future[Option[RaldUserAnswers]] = {
    findAndUpdateByCredId(credId, Updates.set("rentAmount", rentAmount.toString()))
  }

  def insertAgreementVerbal(credId: CredId, startDate: String, openEnded: Boolean, endDate: Option[String]): Future[Option[RaldUserAnswers]] = {
    val updates = Seq(Updates.set("agreementVerbal.startDate", startDate),
      Updates.set("agreementVerbal.openEnded", openEnded),
      Updates.set("agreementVerbal.endDate", endDate.orNull))
    findAndUpdateByCredId(credId, updates: _*)
  }

  def insertDidYouAgreeRentWithLandlord(credId: CredId, radioValue: String): Future[Option[RaldUserAnswers]] = {
    findAndUpdateByCredId(credId, Updates.set("didYouAgreeRentWithLandlord", if(radioValue == "YesTheLandlord") true else false))
  }

  def insertRentDates(credId: CredId, rentDates: String): Future[Option[RaldUserAnswers]] = {
    findAndUpdateByCredId(credId, Updates.set("agreedRentDate", rentDates))
  }

  def insertRentPeriod(credId: CredId, hasAnotherRentPeriod: String): Future[Option[RaldUserAnswers]] = {
    hasAnotherRentPeriod match {
      case "Yes" => findAndUpdateByCredId(credId, Updates.set("hasAnotherRentPeriod", true))
      case _ => findAndUpdateByCredId(credId, Updates.set("hasAnotherRentPeriod", false))
    }
  }
    
  def insertWhatYourRentIncludes(
                                  credId: CredId,
                                  livingAccommodationRadio: String,
                                  rentPartAddressRadio: String,
                                  rentEmptyShellRadio: String,
                                  rentIncBusinessRatesRadio: String,
                                  rentIncWaterChargesRadio: String,
                                  rentIncServiceRadio:String
                                ): Future[Option[RaldUserAnswers]] = {
    
    def radioConvert(value: String): Boolean = {
      value match {
        case value if value.contains("Yes") => true
        case _ => false
      }
    }

    val updates = Seq(
      Updates.set("whatYourRentIncludes.livingAccommodation", radioConvert(livingAccommodationRadio)),
      Updates.set("whatYourRentIncludes.rentPartAddress", radioConvert(rentPartAddressRadio)),
      Updates.set("whatYourRentIncludes.rentEmptyShell", radioConvert(rentEmptyShellRadio)),
      Updates.set("whatYourRentIncludes.rentIncBusinessRates", radioConvert(rentIncBusinessRatesRadio)),
      Updates.set("whatYourRentIncludes.rentIncWaterCharges", radioConvert(rentIncWaterChargesRadio)),
      Updates.set("whatYourRentIncludes.rentIncService", radioConvert(rentIncServiceRadio))
    )

    findAndUpdateByCredId(credId, updates: _*)
  }

  def insertRentAgreeStartDates(credId: CredId, agreedDates: String, startPayingDate: String): Future[Option[RaldUserAnswers]] = {
    val updates: Seq[Bson] = Seq(Updates.set("rentDatesAgreeStart.agreedDate", agreedDates),
      Updates.set("rentDatesAgreeStart.startPayingDate", startPayingDate))
    findAndUpdateByCredId(credId, updates: _*)
  }

  def insertHowManyParkingSpacesOrGaragesIncludedInRent(credId: CredId, uncoveredSpaces: Int, coveredSpaces: Int, garages:Int): Future[Option[RaldUserAnswers]] = {
    val updates = Seq(
      Updates.set("howManyParkingSpacesOrGaragesIncludedInRent.uncoveredSpaces", uncoveredSpaces.toString()),
      Updates.set("howManyParkingSpacesOrGaragesIncludedInRent.coveredSpaces", coveredSpaces.toString()),
      Updates.set("howManyParkingSpacesOrGaragesIncludedInRent.garages", garages.toString())
    )
    findAndUpdateByCredId(credId, updates: _*)
  }


  def findByCredId(credId: CredId): Future[Option[RaldUserAnswers]] = {
    collection.find(
      equal("credId.value", credId.value)
    ).headOption()
  }

  def insertDoesYourRentIncludeParking(credId: CredId, radioValue: String): Future[Option[RaldUserAnswers]] = {
    radioValue match {
      case "Yes" => findAndUpdateByCredId(credId, Updates.set("doesYourRentIncludeParking", true))
      case _ => findAndUpdateByCredId(credId, Updates.set("doesYourRentIncludeParking", false))
    }
  }
}

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

import org.mongodb.scala.SingleObservableFuture
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport
import uk.gov.hmrc.ngrraldfrontend.helpers.{TestData, TestSupport}
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.NewAgreement
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{Landlord, RaldUserAnswers, RentBasedOn}

class RaldRepoSpec extends TestSupport with TestData
  with DefaultPlayMongoRepositorySupport[RaldUserAnswers] {
  override val repository: RaldRepo = app.injector.instanceOf[RaldRepo]
  val credId2: CredId = CredId("123456")

  override def beforeEach(): Unit = {
    await(repository.collection.drop().toFuture())
    await(repository.ensureIndexes())
    await(repository.upsertRaldUserAnswers(RaldUserAnswers(credId, NewAgreement, property)))
  }

  "repository" can {
    "save a new PropertyLinkingUserAnswer" when {
      "correct LookUpAddresses has been supplied" in {
        val isSuccessful = await(
          repository.upsertRaldUserAnswers(RaldUserAnswers(credId2, NewAgreement, property)))
        isSuccessful shouldBe true
        val actual = await(repository.findByCredId(credId2))
        actual shouldBe Some(RaldUserAnswers(credId2, NewAgreement, property))
      }
      "missing credId" in {
        val missingCredId = RaldUserAnswers(credId = CredId(null), NewAgreement, selectedProperty = property)
        val exception = intercept[IllegalStateException] {
          await(repository.upsertRaldUserAnswers(missingCredId))
        }
        exception.getMessage contains "Rald user answers has not been inserted" shouldBe true
      }
    }
    
    "insert type of agreement successfully" in {
      await(
        repository.insertTypeOfAgreement(
          credId, "Written"
        ))
      val actual = await(repository.findByCredId(credId))
      actual shouldBe Some(RaldUserAnswers(credId, NewAgreement, property, whatTypeOfAgreement = Some("Written")))
    }

    "insert type of lease renewal successfully" in {
      await(repository.insertTypeOfRenewal(credId, "RenewedAgreement"))
      val actual = await(repository.findByCredId(credId))
      actual shouldBe Some(RaldUserAnswers(credId, NewAgreement, property, whatTypeOfRenewal = Some("RenewedAgreement")))
    }

    "insert landlord with other description successfully" in {
      val landlordName = "John Doe"
      val landlordType = "OtherRelationship"
      val landlordOther = Some("Other description")

      await(repository.insertLandlord(credId, landlordName = landlordName, landLordType = landlordType, landlordOther = landlordOther))
      val actual = await(repository.findByCredId(credId))
      actual shouldBe Some(RaldUserAnswers(credId, NewAgreement, property, landlord = Some(Landlord("John Doe", "OtherRelationship", Some("Other description")))))
    }

    "insert landlord without other description successfully" in {
      val landlordName = "John Doe"
      val landlordType = "OtherRelationship"

      await(repository.insertLandlord(credId, landlordName = landlordName, landLordType = landlordType, landlordOther = None))
      val actual = await(repository.findByCredId(credId))
      actual shouldBe Some(RaldUserAnswers(credId, NewAgreement, property,  landlord = Some(Landlord("John Doe", "OtherRelationship", None))))
    }

    "insert rent based on with other desc successfully" in {
      await(
        repository.insertRentBased(
          credId, "Other", Some("The rent agree")
        ))
      val actual = await(repository.findByCredId(credId))
      actual shouldBe Some(RaldUserAnswers(credId, NewAgreement, property, rentBasedOn = Some(RentBasedOn("Other", Some("The rent agree")))))
    }

    "insert rent based on without other desc successfully" in {
      await(repository.insertRentBased(credId, "Other", Some("The rent agree")))
      //Testing if other description value has been deleted
      await(
        repository.insertRentBased(
          credId, "PercentageTurnover", None
        ))
      val actual = await(repository.findByCredId(credId))
      actual shouldBe Some(RaldUserAnswers(credId, NewAgreement, property, rentBasedOn = Some(RentBasedOn("PercentageTurnover", None))))
    }

    "insert rent amount successfully" in {
      val annualRent = 10000.99

      await(repository.insertAnnualRent(credId, annualRent))
      val actual = await(repository.findByCredId(credId))
      actual shouldBe Some(RaldUserAnswers(credId, NewAgreement, property, rentAmount = Some("10000.99")))
    }

    "credId doesn't exist in mongoDB" in {
      val actual = await(repository.findByCredId(credId2))
      actual mustBe None
    }
  }
}




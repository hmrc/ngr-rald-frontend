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
import uk.gov.hmrc.ngrraldfrontend.models.RaldUserAnswers
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId

class RaldRepoSpec extends TestSupport with TestData

  with DefaultPlayMongoRepositorySupport[RaldUserAnswers] {
  override val repository: RaldRepo = app.injector.instanceOf[RaldRepo]

  override def beforeEach(): Unit = {
    await(repository.collection.drop().toFuture())
    await(repository.ensureIndexes())
  }

  "repository" can {
    "save a new PropertyLinkingUserAnswer" when {
      "correct LookUpAddresses has been supplied" in {
        val isSuccessful = await(
          repository.upsertRaldUserAnswers(RaldUserAnswers(credId, NewAgreement, property)))
        isSuccessful shouldBe true
        val actual = await(repository.findByCredId(credId))
        actual shouldBe Some(RaldUserAnswers(credId, NewAgreement, property))
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
      val initialize = await(
        repository.upsertRaldUserAnswers(RaldUserAnswers(
          credId,
          NewAgreement,
          property,
          None
        )))
      initialize
      val isSuccessful = await(
        repository.insertTypeOfAgreement(
          credId, "Written"
        ))
      val actual = await(repository.findByCredId(credId))
      actual shouldBe Some(RaldUserAnswers(credId, NewAgreement, property, whatTypeOfAgreement = Some("Written")))
    }
    "credId doesn't exist in mongoDB" in {
      val actual = await(repository.findByCredId(credId))
      actual mustBe None
    }
  }
}



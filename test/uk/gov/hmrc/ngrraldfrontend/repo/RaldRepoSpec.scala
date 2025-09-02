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
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.components.*

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

    "insert agreed rent change successfully" in {
      val agreedValue = "Yes"

      await(repository.insertAgreedRentChange(credId, agreedRentChange = agreedValue))
      val actual = await(repository.findByCredId(credId))
      actual shouldBe Some(RaldUserAnswers(credId, NewAgreement, property, agreedRentChange = Some(agreedValue)))
    }

    "insert agreement with open end date and break clause info successfully" in {
      val agreementStart = "12-12-2026"
      val openEndedRadio = "NoOpenEnded"
      val openEndedDate = Some("12-12-2026")
      val breakClauseRadio = "YesBreakClause"
      val breakClauseInfo = Some("break clause info")


      await(repository.insertAgreement(
        credId,
        agreementStart = agreementStart,
        openEndedRadio = openEndedRadio,
        openEndedDate = openEndedDate,
        breakClauseRadio = breakClauseRadio,
        breakClauseInfo = breakClauseInfo,
      ))

      val actual = await(repository.findByCredId(credId))
      actual shouldBe
        Some(
          RaldUserAnswers(
            credId,
            NewAgreement,
            property,
            agreement =
              Some(
                Agreement(
                  agreementStart = agreementStart,
                  isOpenEnded = false,
                  openEndedDate = openEndedDate,
                  haveBreakClause = true,
                  breakClauseInfo = breakClauseInfo
                )
              )
            )
        )
    }

    "insert agreement with open end date successfully" in {
      val agreementStart = "12-12-2026"
      val openEndedRadio = "NoOpenEnded"
      val openEndedDate = Some("12-12-2026")
      val breakClauseRadio = "NoBreakClause"



      await(repository.insertAgreement(
        credId,
        agreementStart = agreementStart,
        openEndedRadio = openEndedRadio,
        openEndedDate = openEndedDate,
        breakClauseRadio = breakClauseRadio,
        breakClauseInfo = None
      ))

      val actual = await(repository.findByCredId(credId))
      actual shouldBe
        Some(
          RaldUserAnswers(
            credId,
            NewAgreement,
            property,
            agreement =
              Some(
                Agreement(
                  agreementStart = agreementStart,
                  isOpenEnded = false,
                  openEndedDate = openEndedDate,
                  haveBreakClause = false,
                  breakClauseInfo = None
                )
              )
          )
        )
    }

    "insert agreement with break clause info successfully" in {
      val agreementStart = "12-12-2026"
      val openEndedRadio = "YesOpenEnded"
      val openEndedDate = None
      val breakClauseRadio = "YesBreakClause"
      val breakClauseInfo = Some("break clause info")


      await(repository.insertAgreement(
        credId,
        agreementStart = agreementStart,
        openEndedRadio = openEndedRadio,
        openEndedDate = None,
        breakClauseRadio = breakClauseRadio,
        breakClauseInfo = breakClauseInfo,
      ))

      val actual = await(repository.findByCredId(credId))
      actual shouldBe
        Some(
          RaldUserAnswers(
            credId,
            NewAgreement,
            property,
            agreement =
              Some(
                Agreement(
                  agreementStart = agreementStart,
                  isOpenEnded = true,
                  openEndedDate = None,
                  haveBreakClause = true,
                  breakClauseInfo = breakClauseInfo
                )
              )
          )
        )
    }

    "insert agreement with no break clause info and no open end date successfully" in {
      val agreementStart = "12-12-2026"
      val openEndedRadio = "YesOpenEnded"
      val breakClauseRadio = "NoBreakClause"
      val breakClauseInfo = None


      await(repository.insertAgreement(
        credId,
        agreementStart = agreementStart,
        openEndedRadio = openEndedRadio,
        openEndedDate = None,
        breakClauseRadio = breakClauseRadio,
        breakClauseInfo = breakClauseInfo,
      ))

      val actual = await(repository.findByCredId(credId))
      actual shouldBe
        Some(
          RaldUserAnswers(
            credId,
            NewAgreement,
            property,
            agreement =
              Some(
                Agreement(
                  agreementStart = agreementStart,
                  isOpenEnded = true,
                  openEndedDate = None,
                  haveBreakClause = false,
                  breakClauseInfo = None
                )
              )
          )
        )
    }

    "handle non-yesPayedRent value correctly by setting boolean to false" in {
      val result = await(repository.insertProvideDetailsOfFirstSecondRentPeriod(
        credId = credId,
        firstDateStart = "2025-01-01",
        firstDateEnd = "2025-01-31",
        firstRentPeriodRadio = "noRentPaid",
        firstRentPeriodAmount = None,
        secondDateStart = "2025-02-01",
        secondDateEnd = "2025-02-28",
        secondHowMuchIsRent = BigDecimal(1000)
      ))

      val actual = await(repository.findByCredId(credId))

     result mustBe actual
    }


    "handle yesPayedRent value correctly by setting boolean to true and take first rent period amount" in {
      val expected = RaldUserAnswers(
        credId,
        NewAgreement,
        property,
        provideDetailsOfFirstSecondRentPeriod = Some(ProvideDetailsOfFirstSecondRentPeriod(
          firstDateStart = "2025-01-01",
          firstDateEnd = "2025-01-31",
          firstRentPeriodRadio = true,
          firstRentPeriodAmount = Some("1000"),
          secondDateStart = "2025-02-01",
          secondDateEnd = "2025-02-28",
          secondHowMuchIsRent = "1000"
        ))
      )

      await(repository.insertProvideDetailsOfFirstSecondRentPeriod(
        credId = credId,
        firstDateStart = "2025-01-01",
        firstDateEnd = "2025-01-31",
        firstRentPeriodRadio = "yesPayedRent",
        firstRentPeriodAmount = Some(1000),
        secondDateStart = "2025-02-01",
        secondDateEnd = "2025-02-28",
        secondHowMuchIsRent = BigDecimal(1000)
      ))

      val actual = await(repository.findByCredId(credId))

      actual mustBe Some(expected)
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

    "insert agreement verbal with end date successfully" in {
      await(
        repository.insertAgreementVerbal(
          credId, "2025-10-30", true, Some("2027-11-30")
        ))
      val actual = await(repository.findByCredId(credId))
      actual shouldBe Some(RaldUserAnswers(credId, NewAgreement, property, agreementVerbal = Some(AgreementVerbal("2025-10-30", true, Some("2027-11-30")))))
    }

    "insert agreement verbal without end date successfully" in {
      await(repository.insertAgreementVerbal(credId, "2025-10-30", true, Some("2027-11-30")))
      //Testing if end date value has been deleted
      await(
        repository.insertAgreementVerbal(
          credId, "2025-10-30", false, None
        ))
      val actual = await(repository.findByCredId(credId))
      actual shouldBe Some(RaldUserAnswers(credId, NewAgreement, property, agreementVerbal = Some(AgreementVerbal("2025-10-30", false, None))))
    }

    "insert AgreedRentChange successfully" in {
      val agreedRentChange = "Yes"

      await(repository.insertAgreedRentChange(credId, agreedRentChange))
      val actual = await(repository.findByCredId(credId))
      actual shouldBe Some(RaldUserAnswers(credId, NewAgreement, property, agreedRentChange = Some("Yes")))
    }

    "insert hasRentFreePeriod successfully with Yes" in {
      val hasRentFreePeriod = "Yes"

      await(repository.insertHasRentFreePeriod(credId, hasRentFreePeriod))
      val actual = await(repository.findByCredId(credId))
      actual shouldBe Some(RaldUserAnswers(credId, NewAgreement, property, hasRentFreePeriod = Some(true)))
    }

    "insert Did You Agree Rent With Landlord successfully with true" in {
      val yes = "YesTheLandlord"

      await(repository.insertDidYouAgreeRentWithLandlord(credId, yes))
      val actual = await(repository.findByCredId(credId))
      actual shouldBe Some(RaldUserAnswers(credId, NewAgreement, property, didYouAgreeRentWithLandlord = Some(true)))
    }

    "insert Did You Agree Rent With Landlord successfully with false" in {
      val no = "NoACourtSet"

      await(repository.insertDidYouAgreeRentWithLandlord(credId, no))
      val actual = await(repository.findByCredId(credId))
      actual shouldBe Some(RaldUserAnswers(credId, NewAgreement, property, didYouAgreeRentWithLandlord = Some(false)))
    }

    "insert hasRentFreePeriod successfully with No" in {
      val hasRentFreePeriod = "No"

      await(repository.insertHasRentFreePeriod(credId, hasRentFreePeriod))
      val actual = await(repository.findByCredId(credId))
      actual shouldBe Some(RaldUserAnswers(credId, NewAgreement, property, hasRentFreePeriod = Some(false)))
    }

    "insert rent date successfully" in {
      val rentDate = "2025-4-30"

      await(repository.insertRentDates(credId, rentDate))
      val actual = await(repository.findByCredId(credId))
      actual shouldBe Some(RaldUserAnswers(credId, NewAgreement, property, agreedRentDate = Some(rentDate)))
    }

    "insert what your rent includes with all radios selected as yes" in {
      val radio1yes = livingAccommodationYes
      val radio2yes = rentPartAddressYes
      val radio3yes = rentEmptyShellYes
      val radio4yes = rentIncBusinessRatesYes
      val radio5yes = rentIncWaterChargesYes
      val radio6yes = rentIncServiceYes

      await(repository.insertWhatYourRentIncludes(
        credId,
        radio1yes.toString,
        radio2yes.toString,
        radio3yes.toString,
        radio4yes.toString,
        radio5yes.toString,
        radio6yes.toString,
      ))
      val actual = await(repository.findByCredId(credId))
      actual shouldBe Some(RaldUserAnswers(credId, NewAgreement, property, whatYourRentIncludes = Some(WhatYourRentIncludes(
        true,
        true,
        true,
        true,
        true,
        true
      ))))
    }

    "insert what your rent includes with all radios selected as no" in {
      val radio1yes = livingAccommodationNo
      val radio2yes = rentPartAddressNo
      val radio3yes = rentEmptyShellNo
      val radio4yes = rentIncBusinessRatesNo
      val radio5yes = rentIncWaterChargesNo
      val radio6yes = rentIncServiceNo

      await(repository.insertWhatYourRentIncludes(
        credId,
        radio1yes.toString,
        radio2yes.toString,
        radio3yes.toString,
        radio4yes.toString,
        radio5yes.toString,
        radio6yes.toString,
      ))
      val actual = await(repository.findByCredId(credId))
      actual shouldBe Some(RaldUserAnswers(credId, NewAgreement, property, whatYourRentIncludes = Some(WhatYourRentIncludes(
        false,
        false,
        false,
        false,
        false,
        false
      ))))
    }

    "credId doesn't exist in mongoDB" in {
      val actual = await(repository.findByCredId(credId2))
      actual mustBe None
    }
  }
}




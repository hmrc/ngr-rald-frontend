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

package uk.gov.hmrc.ngrraldfrontend.services

import org.scalatest.TryValues.convertTryToSuccessOrFailure
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Key, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Content
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.registration.*
import uk.gov.hmrc.ngrraldfrontend.pages.*

class CheckAnswersSpec extends ViewBaseSpec {

  def extractText(content: Content): String = content match {
    case Text(value) => value
    case HtmlContent(html) => html.toString.replaceAll("<[^>]*>", "")
    case other => fail(s"Unexpected content type: $other")
  }

  "buildRow" should {
    "create a row with correct label, value, and link" in {
      val href = Call("GET", "/test-url")
      val row = CheckAnswers.buildRow(
        labelKey = "checkAnswers.landlord.fullName",
        value = "John Doe",
        linkId = "landlord-full-name",
        href = href,
        hiddenKey = "landlord-full-name"
      )

      row.titleMessageKey mustBe "checkAnswers.landlord.fullName"
      row.value mustBe Seq("John Doe")
      row.changeLink mustBe defined
      row.changeLink.get.href mustBe href
      row.changeLink.get.linkId mustBe "landlord-full-name"
    }
  }

  "yesNo" should {
    "return service.yes when true" in {
      CheckAnswers.yesNo(true) mustBe messages("service.yes")
    }
    "return service.no when false" in {
      CheckAnswers.yesNo(false) mustBe messages("service.no")
    }
  }

  "createLandlordSummaryRows" should {
    "return rows with landlord details when data is present" in {
      val landlord = Landlord("John Doe", hasRelationship = true, landlordRelationship = Some("Family"))
      val userAnswers: UserAnswers = UserAnswers(CredId("cred-123")).set(LandlordPage, landlord).get
      val summaryList = CheckAnswers.createLandlordSummaryRows("cred-123", Some(userAnswers))

      summaryList.rows.size mustBe 3
      summaryList.rows.map(_.key) must contain allOf(
        Key(Text("Landlords full name"), ""),
        Key(Text("Do you have a relationship with the landlord other than as a tenant?"), ""),
        Key(Text("Relationship with the landlord"), "")
      )
    }

    "return rows with 'not provided' when data is missing" in {
      val summaryList = CheckAnswers.createLandlordSummaryRows("cred-123", None)

      summaryList.rows.foreach { row =>
        extractText(row.value.content) mustBe messages("service.notProvided")
      }
    }
  }

  "createAgreementDetailsRows" should {
    "return rows with agreement details when data is present" in {
      val agreement = Agreement(
        agreementStart = NGRDate("1", "1", "2025").makeString,
        isOpenEnded = false,
        openEndedDate = Some(NGRDate("1", "1", "2025").makeString),
        haveBreakClause = true,
        breakClauseInfo = Some("Break clause details")
      )

      val userAnswers = UserAnswers(CredId("cred-123"))
        .set(WhatTypeOfAgreementPage, "LeaseOrTenancy").success.value
        .set(AgreementPage, agreement).success.value


      val summaryListOpt = CheckAnswers.createAgreementDetailsRows("cred-123", Some(userAnswers))

      summaryListOpt mustBe defined
      val summaryList = summaryListOpt.get

      summaryList.rows.size mustBe 6

      val keys = summaryList.rows.map(row => extractText(row.key.content))
      keys must contain allOf(
        messages("checkAnswers.agreement.whatTypeOfAgreement"),
        messages("checkAnswers.agreement.startDate"),
        messages("checkAnswers.agreement.isOpenEnded"),
        messages("checkAnswers.agreement.endDate"),
        messages("checkAnswers.agreement.breakClause"),
        messages("checkAnswers.agreement.breakClauseDetails")
      )

      val values = summaryList.rows.map(row => extractText(row.value.content))
      values must contain(messages("whatTypeOfAgreement.LeaseOrTenancy"))
      values must contain(NGRDate.formatDate(agreement.agreementStart))
      values must contain(messages("agreementVerbal.no"))
      values must contain(NGRDate.formatDate(agreement.openEndedDate.get))
      values must contain("Yes")
      values must contain("Break clause details")
    }
  }

  "return only agreement type row when other details are missing" in {
    val userAnswers = UserAnswers(CredId("cred-123"))
      .set(WhatTypeOfAgreementPage, "Verbal")
      .get

    val summaryListOpt = CheckAnswers.createAgreementDetailsRows("cred-123", Some(userAnswers))

    summaryListOpt mustBe defined
    val summaryList = summaryListOpt.get

    summaryList.rows.size mustBe 1
    extractText(summaryList.rows.head.key.content) mustBe messages("checkAnswers.agreement.whatTypeOfAgreement")
    extractText(summaryList.rows.head.value.content) mustBe messages("whatTypeOfAgreement.verbal")
  }

  "createRentRows" should {
    "return rows with rent details when data is present" in {
      val userAnswers = UserAnswers(CredId("cred-123"))
        .set(WhatIsYourRentBasedOnPage, RentBasedOn("Other",  Some("Reason for being other"))).success.value
        .set(RentDatesAgreePage, NGRDate("1", "1", "2020").makeString).success.value
        .set(DidYouAgreeRentWithLandlordPage, true).success.value
        .set(AgreedRentChangePage, true).success.value
        .set(RentInterimPage, true).success.value
        .set(HowMuchIsTotalAnnualRentPage, BigDecimal(12000)).success.value
        .set(CheckRentFreePeriodPage, true).success.value
        .set(RentFreePeriodPage, RentFreePeriod(months = 2, reasons = "Was not in the country")).success.value
        .set(RentDatesAgreeStartPage, RentDatesAgreeStart(NGRDate("1", "1", "2020").makeString, NGRDate("1", "1", "2021").makeString)).success.value
      val summaryList = CheckAnswers.createRentRows("cred-123", Some(userAnswers))
      summaryList.rows.size must be >= 3
      val keys = summaryList.rows.map(_.key.content).map {
        case Text(text) => text
        case HtmlContent(html) => html.toString
      }
      keys must contain(messages("checkAnswers.rent.whatIsYourRentBasedOn"))
      keys must contain(messages("checkAnswers.rent.otherReason"))
      keys must contain(messages("checkAnswers.rents.whenDidYouAgree"))
      keys must contain(messages("checkAnswers.rents.whenDidYouAgree"))
      keys must contain(messages("checkAnswers.rent.didYouAgreeRentWithLandlord"))
      keys must contain(messages("checkAnswers.rent.agreedRentChange"))
      keys must contain(messages("checkAnswers.rent.checkRentPeriod"))
      keys must contain(messages("checkAnswers.rent.rentInterim"))
      keys must contain(messages("checkAnswers.rent.totalAnnualRent"))
      keys must contain(messages("checkAnswers.rents.rentFreePeriod"))
      keys must contain(messages("checkAnswers.rents.rentFreePeriodReason"))
      keys must contain(messages("checkAnswers.rents.startPayingDate"))

      val values = summaryList.rows.map(_.value.content).map {
        case Text(text) => text
        case HtmlContent(html) => extractText(HtmlContent(html))
      }

      values must contain allOf(
        messages("whatIsYourRentBasedOn.other"),
        "Reason for being other",
        "1 January 2020",
        "2 months",
        "Was not in the country",
        messages("Yes"),
        "£12000",
      )
    }
  }
  "createRentRows" should {
    "return empty rows when no data is provided" in {
      val summaryList = CheckAnswers.createRentRows("cred-123", None)
      summaryList.rows.size mustBe 0
    }
  }

  "createWhatYourRentIncludesRows" should {
    "return rows with all rent includes details when data is present" in {
      val whatYourRentIncludes = WhatYourRentIncludes(
        livingAccommodation = true,
        bedroomNumbers = Some(3),
        rentPartAddress = false,
        rentEmptyShell = false,
        rentIncBusinessRates = Some(true),
        rentIncWaterCharges = Some(false),
        rentIncService = Some(true)
      )

      val parkingSpacesIncluded = HowManyParkingSpacesOrGarages(uncoveredSpaces = 5, coveredSpaces = 6, garages = 0)
      val parkingSpacesNotIncluded = ParkingSpacesOrGaragesNotIncludedInYourRent(uncoveredSpaces = 100, coveredSpaces = 0, garages = 20,totalCost =  BigDecimal(12.0), agreementDate = NGRDate("12", "12", "2020").makeString)

      val userAnswers = UserAnswers(CredId("cred-123"))
        .set(WhatYourRentIncludesPage, whatYourRentIncludes).success.value
        .set(DoesYourRentIncludeParkingPage, true).success.value
        .set(HowManyParkingSpacesOrGaragesIncludedInRentPage, parkingSpacesIncluded).success.value
        .set(DoYouPayExtraForParkingSpacesPage, false).success.value
        .set(ParkingSpacesOrGaragesNotIncludedInYourRentPage, parkingSpacesNotIncluded).success.value

      val summaryList = CheckAnswers.createWhatYourRentIncludesRows("cred-123", Some(userAnswers))
      summaryList.rows.size must be >= 8 // livingAccommodation, bedroomNumbers, rentPartAddress, etc.

      val keys = summaryList.rows.map(row => extractText(row.key.content))
      keys must contain(messages("checkAnswers.whatYourRentIncludes.livingAccommodation"))
      keys must contain(messages("checkAnswers.whatYourRentIncludes.bedroomNumbers"))
      //keys must contain(messages("checkAnswers.parking.doesYourRentIncludeParking"))

      val values = summaryList.rows.map(row => extractText(row.value.content))
      values must contain(messages("service.yes")) // livingAccommodation true
      values must contain("3") // bedroomNumbers
      values must contain(messages("service.no")) // doYouPayExtraForParkingSpaces false
      values.exists(_.contains("100")) mustBe true // uncoveredSpaces
    }
  }
  "createWhatYourRentIncludesRows return rows with 'not provided' when data is missing" in {
    val summaryList = CheckAnswers.createWhatYourRentIncludesRows("cred-123", None)
    summaryList.rows.size mustBe 3 // livingAccommodation, rentPartAddress, rentEmptyShell always present
    summaryList.rows.foreach { row =>
      extractText(row.value.content) mustBe messages("service.notProvided")
    }
  }
  "createWhatYourRentIncludesRows return only livingAccommodation row when other details are missing" in {
    val whatYourRentIncludes = WhatYourRentIncludes(
      livingAccommodation = true,
      bedroomNumbers = None,
      rentPartAddress = false,
      rentEmptyShell = false,
      rentIncBusinessRates = None,
      rentIncWaterCharges = None,
      rentIncService = None
    )
    val userAnswers = UserAnswers(CredId("cred-123")).set(WhatYourRentIncludesPage, whatYourRentIncludes).get
    val summaryList = CheckAnswers.createWhatYourRentIncludesRows("cred-123", Some(userAnswers))
    val livingRow = summaryList.rows.find(row => extractText(row.key.content) == messages("checkAnswers.whatYourRentIncludes.livingAccommodation"))
    livingRow mustBe defined
    extractText(livingRow.get.value.content) mustBe messages("service.yes")
  }
  "createRepairsAndInsurance" should {
    "return rows with all repairs and insurance details when data is present" in {
      val repairsAndInsurance = RepairsAndInsurance(
        internalRepairs = "YouAndLandlord",
        externalRepairs = "Landlord",
        buildingInsurance = "YouAndLandlord"
      )

      val userAnswers = UserAnswers(CredId("cred-123"))
        .set(RepairsAndInsurancePage, repairsAndInsurance)
        .get

      val summaryList = CheckAnswers.createRepairsAndInsurance("cred-123", Some(userAnswers))

      summaryList.rows.size mustBe 3

      val keys = summaryList.rows.map(row => extractText(row.key.content))
      keys must contain allOf(
        messages("checkAnswers.repairsAndInsurance.internalRepairs"),
        messages("checkAnswers.repairsAndInsurance.externalRepairs"),
        messages("checkAnswers.repairsAndInsurance.buildingInsurance")
      )

      val values = summaryList.rows.map(row => extractText(row.value.content))
      values must contain(messages("repairsAndInsurance.radio.youAndLandlord"))
      values must contain(messages("repairsAndInsurance.radio.landlord"))
    }
  }
  "createRepairsAndInsurance return rows with 'not provided' when data is missing" in {
    val summaryList = CheckAnswers.createRepairsAndInsurance("cred-123", None)

    summaryList.rows.size mustBe 3
    summaryList.rows.foreach { row =>
      extractText(row.value.content) mustBe messages("service.notProvided")
    }
  }

  "createRentReviewRows" should {
    "return rows with all rent review details when both sets of data are present" in {
      val rentReview = RentReview(
        hasIncludeRentReview = true,
        rentReviewYears = Some(3),
        rentReviewMonths = Some(6),
        canRentGoDown = false
      )

      val rentDetails = RentReviewDetails(
        annualRentAmount = BigDecimal(15000),
        whatHappensAtRentReview = "OnlyGoUp",
        startDate = "2024-01-01",
        hasAgreedNewRent = true,
        whoAgreed = Some("Arbitrator")
      )

      val userAnswers = UserAnswers(CredId("cred-123"))
        .set(RentReviewPage, rentReview).success.value
        .set(RentReviewDetailsPage, rentDetails).success.value

      val summaryList = CheckAnswers.createRentReviewRows("cred-123", Some(userAnswers))

      summaryList.rows.size must be >= 6

      val keys = summaryList.rows.map(row => extractText(row.key.content))
      keys must contain(messages("checkAnswers.rentReview.hasIncludeRentReview"))
      keys must contain(messages("checkAnswers.rentReviewDetails.annualAmount"))
      keys must contain(messages("checkAnswers.rentReviewDetails.whatHappensAtRentReview"))

      val values = summaryList.rows.map(row => extractText(row.value.content))
      values must contain("Yes")
      values.exists(_.contains("Every 3 years and 6 months")) mustBe true
      values must contain(messages("rentReviewDetails.whatHappensAtRentReview.radio2.text"))
      values.exists(_.contains("£ 15000")) mustBe true
      values must contain(messages("service.yes")) // hasAgreedNewRent
      values must contain(messages("rentReviewDetails.whoAgreed.radio1.text"))
    }
  }
  "return rows for rent review only when details are missing" in {
    val rentReview = RentReview(hasIncludeRentReview = true, rentReviewYears = Some(2), rentReviewMonths = Some(0), canRentGoDown = true)
    val userAnswers = UserAnswers(CredId("cred-123")).set(RentReviewPage, rentReview).get

    val summaryList = CheckAnswers.createRentReviewRows("cred-123", Some(userAnswers))

    summaryList.rows.size must be >= 3
    extractText(summaryList.rows.head.key.content) mustBe messages("checkAnswers.rentReview.hasIncludeRentReview")
    extractText(summaryList.rows.head.value.content) mustBe "Yes"
  }
  "return rows for rent review details only when rent review is missing" in {
    val rentDetails = RentReviewDetails(
      annualRentAmount = BigDecimal(20000),
      whatHappensAtRentReview = "CanGoDown",
      startDate = "2024-06-01",
      hasAgreedNewRent = false,
      whoAgreed = Some("Other")
    )

    val userAnswers = UserAnswers(CredId("cred-123")).set(RentReviewDetailsPage, rentDetails).get

    val summaryList = CheckAnswers.createRentReviewRows("cred-123", Some(userAnswers))

    summaryList.rows.size must be >= 4
    extractText(summaryList.rows.head.key.content) mustBe messages("checkAnswers.rentReviewDetails.annualAmount")
    extractText(summaryList.rows.head.value.content) must include("£ 20000")
  }
  "createRentReviewRows return empty rows when no data is provided" in {
    val summaryList = CheckAnswers.createRentReviewRows("cred-123", None)
    summaryList.rows.size mustBe 0
  }

  "createRepairsAndFittingOut" should {
    "return rows with repairs and fitting out details when data is present" in {
      val aboutRepairs = AboutRepairsAndFittingOut(date = "2024-01", cost = BigDecimal(5000))

      val userAnswers = UserAnswers(CredId("cred-123"))
        .set(RepairsAndFittingOutPage, true).success.value
        .set(AboutRepairsAndFittingOutPage, aboutRepairs).success.value

      val summaryListOpt = CheckAnswers.createRepairsAndFittingOut("cred-123", Some(userAnswers))

      summaryListOpt mustBe defined
      val summaryList = summaryListOpt.get

      summaryList.rows.size mustBe 3

      val keys = summaryList.rows.map(row => extractText(row.key.content))
      keys must contain allOf(
        messages("checkAnswers.repairsAndFittingOut.repairsAndFittingOut"),
        messages("checkAnswers.repairsAndFittingOut.date"),
        messages("checkAnswers.repairsAndFittingOut.cost")
      )

      val values = summaryList.rows.map(row => extractText(row.value.content))
      values must contain(messages("service.yes"))
      values must contain(NGRMonthYear.formatYearMonth("2024-01"))
      values.exists(_.contains("£5000")) mustBe true
    }
  }
  "createRepairsAndFittingOut return only repairsAndFittingOut row when other details are missing" in {
    val userAnswers = UserAnswers(CredId("cred-123")).set(RepairsAndFittingOutPage, false).get

    val summaryListOpt = CheckAnswers.createRepairsAndFittingOut("cred-123", Some(userAnswers))

    summaryListOpt mustBe defined
    val summaryList = summaryListOpt.get

    summaryList.rows.size mustBe 1
    extractText(summaryList.rows.head.key.content) mustBe messages("checkAnswers.repairsAndFittingOut.repairsAndFittingOut")
    extractText(summaryList.rows.head.value.content) mustBe messages("service.no")
  }
  "createRepairsAndFittingOut return None when no data is provided" in {
    val summaryListOpt = CheckAnswers.createRepairsAndFittingOut("cred-123", None)
    summaryListOpt mustBe None
  }

  "createPaymentRows" should {
    "return rows with all payment details when data is present" in {
      val paymentAdvance = MoneyYouPaidInAdvanceToLandlord(amount = BigDecimal(2500), date = NGRDate("12", "12", "2020").makeString)

      val userAnswers = UserAnswers(CredId("cred-123"))
        .set(DidYouGetMoneyFromLandlordPage, true).success.value
        .set(DidYouPayAnyMoneyToLandlordPage, false).success.value
        .set(MoneyYouPaidInAdvanceToLandlordPage, paymentAdvance).success.value

      val summaryListOpt = CheckAnswers.createPaymentRows("cred-123", Some(userAnswers))

      summaryListOpt mustBe defined
      val summaryList = summaryListOpt.get

      summaryList.rows.size mustBe 4

      val keys = summaryList.rows.map(row => extractText(row.key.content))
      keys must contain allOf(
        messages("checkAnswers.payments.didYouGetMoneyFromLandlord"),
        messages("checkAnswers.payments.didYouPayAnyMoneyToLandlord"),
        messages("checkAnswers.payments.moneyYouPaidInAdvanceToLandlord.amount"),
        messages("checkAnswers.payments.moneyYouPaidInAdvanceToLandlord.date")
      )

      val values = summaryList.rows.map(row => extractText(row.value.content))
      values must contain(messages("service.yes")) // gotMoney true
      values must contain(messages("service.no")) // paidMoney false
      values.exists(_.contains("£2500")) mustBe true
      values must contain(NGRDate.formatDate(paymentAdvance.date))
    }
  }
  "createPaymentRows return only gotMoney row when other details are missing" in {
    val userAnswers = UserAnswers(CredId("cred-123")).set(DidYouGetMoneyFromLandlordPage, true).get

    val summaryListOpt = CheckAnswers.createPaymentRows("cred-123", Some(userAnswers))

    summaryListOpt mustBe defined
    val summaryList = summaryListOpt.get

    summaryList.rows.size mustBe 1
    extractText(summaryList.rows.head.key.content) mustBe messages("checkAnswers.payments.didYouGetMoneyFromLandlord")
    extractText(summaryList.rows.head.value.content) mustBe messages("service.yes")
  }
  "createPaymentRows return None when no data is provided" in {
    val summaryListOpt = CheckAnswers.createPaymentRows("cred-123", None)
    summaryListOpt mustBe None
  }

  "createBreakClauseRows" should {
    "return a row with break clause details when data is present" in {
      val userAnswers = UserAnswers(CredId("cred-123")).set(ConfirmBreakClausePage, true).get

      val summaryListOpt = CheckAnswers.createBreakClauseRows("cred-123", Some(userAnswers))

      summaryListOpt mustBe defined
      val summaryList = summaryListOpt.get

      summaryList.rows.size mustBe 1
      extractText(summaryList.rows.head.key.content) mustBe messages("checkAnswers.breakClause.confirmBreakClause")
      extractText(summaryList.rows.head.value.content) mustBe messages("service.yes")
    }
  }
  "createBreakClauseRows return None when no break clause data is provided" in {
    val summaryListOpt = CheckAnswers.createBreakClauseRows("cred-123", None)
    summaryListOpt mustBe None
  }

  "createOtherDetailsRow" should {
    "return rows with other details when data is present" in {
      val otherDetails = HasAnythingElseAffectedTheRent(radio = true, reason = Some("Special discount applied"))

      val userAnswers = UserAnswers(CredId("cred-123")).set(HasAnythingElseAffectedTheRentPage, otherDetails).get

      val summaryList = CheckAnswers.createOtherDetailsRow("cred-123", Some(userAnswers))

      summaryList.rows.size mustBe 2

      val keys = summaryList.rows.map(row => extractText(row.key.content))
      keys must contain allOf(
        messages("checkAnswers.Otherdetails.hasAnyAffectedRent"),
        messages("checkAnswers.Otherdetails.reason")
      )

      val values = summaryList.rows.map(row => extractText(row.value.content))
      values must contain(messages("service.yes"))
      values must contain("Special discount applied")
    }
  }
  "createOtherDetailsRow return only hasAnyAffectedRent row when reason is missing" in {
    val otherDetails = HasAnythingElseAffectedTheRent(radio = false, reason = None)
    val userAnswers = UserAnswers(CredId("cred-123")).set(HasAnythingElseAffectedTheRentPage, otherDetails).get

    val summaryList = CheckAnswers.createOtherDetailsRow("cred-123", Some(userAnswers))

    summaryList.rows.size mustBe 1
    extractText(summaryList.rows.head.key.content) mustBe messages("checkAnswers.Otherdetails.hasAnyAffectedRent")
    extractText(summaryList.rows.head.value.content) mustBe messages("service.no")
  }
  "createOtherDetailsRow return empty SummaryList when no other details data is provided" in {
    val summaryList = CheckAnswers.createOtherDetailsRow("cred-123", None)
    summaryList.rows.size mustBe 0
  }


  "createLeaseRenewalsSummaryRows" should {
    "return a row with lease renewal details when data is present" in {
      val userAnswers = UserAnswers(CredId("cred-123"))
        .set(WhatTypeOfLeaseRenewalPage, "RenewedAgreement")
        .get
      val summaryListOpt = CheckAnswers.createLeaseRenewalsSummaryRows("cred-123", Some(userAnswers))

      summaryListOpt mustBe defined
      val summaryList = summaryListOpt.get

      summaryList.rows.size mustBe 1
      val row = summaryList.rows.head
      row.key.content match {
        case Text(text) => text mustBe messages("checkAnswers.leaseRenewal.typeOfLeaseRenewal")
        case _ => fail("Expected Text content for key")
      }
      summaryList.rows.foreach { row =>
        extractText(row.value.content) mustBe messages("typeOfLeaseRenewal.option1")
      }
    }
  }

  "createLeaseRenewalsSummaryRows" should {
    "return None when data is missing" in {
      val summaryListOpt = CheckAnswers.createLeaseRenewalsSummaryRows("cred-123", None)
      summaryListOpt mustBe None
    }
  }

}
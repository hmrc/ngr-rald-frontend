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
import uk.gov.hmrc.ngrraldfrontend.helpers.{TestData, ViewBaseSpec}
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.Incentive.{YesLumpSum, YesRentFreePeriod}
import uk.gov.hmrc.ngrraldfrontend.models.registration.*
import uk.gov.hmrc.ngrraldfrontend.pages.*
import uk.gov.hmrc.ngrraldfrontend.services.CheckAnswers.{createBreakClauseRows, createFirstRentPeriodRow, createRentPeriodsSummaryLists}

import java.time.LocalDate

class CheckAnswersSpec extends ViewBaseSpec with TestData {

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
        Key(Text("Landlord's full name"), ""),
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


    "return SummaryList with all rows when all answers are present" in {
      val agreement = Agreement(
        agreementStart = NGRDate("1", "1", "2025").makeString,
        isOpenEnded = false,
        openEndedDate = Some(NGRDate("1", "1", "2025").makeString),
        haveBreakClause = true,
        breakClauseInfo = Some("Break clause details")
      )

      val answers = UserAnswers(CredId("1234"))
        .set(WhatTypeOfAgreementPage, "Written")
        .flatMap(_.set(AgreementPage, agreement))
        .success.value

      val result = CheckAnswers.createAgreementDetailsRows("1234", Some(answers))

      result mustBe defined
      val rows = result.get.rows
      rows.size mustBe 6
      rows.map(_.key.content.asHtml.body) must contain(messages("checkAnswers.agreement.whatTypeOfAgreement"))
    }

    "return SummaryList with only agreement type row when only that answer is present" in {
      val answers = UserAnswers(CredId("1234"))
        .set(WhatTypeOfAgreementPage, "Verbal")
        .success.value

      val result = CheckAnswers.createAgreementDetailsRows("1234", Some(answers))

      result mustBe defined
      result.get.rows.size mustBe 1
      result.get.rows.head.value.content.asHtml.body must include(messages("whatTypeOfAgreement.verbal"))
    }


    "include verbal agreement rows when verbal agreement details are present" in {
      val answers = UserAnswers(CredId("1234"))
        .set(WhatTypeOfAgreementPage, "Verbal")
        .flatMap(_.set(AgreementVerbalPage, agreementVerbalModel))
        .success.value
      val result = CheckAnswers.createAgreementDetailsRows("1234", Some(answers))

      result mustBe defined
      val rows = result.get.rows

      rows.size mustBe 3
      val valuesHtml = rows.map(_.value.content.asHtml.body)
      valuesHtml.exists(_.contains(NGRDate.formatDate(LocalDate.of(2025, 1, 1).toString))) mustBe true
      println(Console.YELLOW + "" + Console.RESET)

      val expectedOpenEndedText = if (agreementVerbalModel.openEnded) {
        messages("agreementVerbal.yes")
      } else {
        messages("agreementVerbal.no")
      }

      valuesHtml.exists(_.contains(expectedOpenEndedText)) mustBe true

    }


    "include end date and break clause details when provided in written agreement" in {
      val agreement = Agreement(
        agreementStart = NGRDate("1", "1", "2025").makeString,
        isOpenEnded = false,
        openEndedDate = Some(NGRDate("1", "1", "2025").makeString),
        haveBreakClause = true,
        breakClauseInfo = Some("Break clause details")
      )

      val answers = UserAnswers(CredId("1234"))
        .set(WhatTypeOfAgreementPage, "Written")
        .flatMap(_.set(AgreementPage, agreement))
        .success.value

      val result = CheckAnswers.createAgreementDetailsRows("1234", Some(answers))

      result mustBe defined
      val rows = result.get.rows


      rows.map(_.value.content.asHtml.body) must contain(
        """<span id="checkanswers.agreement.startdate-id">1 January 2025</span>"""
      )

      rows.mkString must include("Break clause details")
    }

    "return None when agreement type is missing" in {
      val answers = UserAnswers(CredId("1234"))

      val result = CheckAnswers.createAgreementDetailsRows("1234", Some(answers))

      result mustBe None
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
        .set(WhatIsYourRentBasedOnPage, RentBasedOn("Other", Some("Reason for being other"))).success.value
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
        "£12,000",
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
      val parkingSpacesNotIncluded = ParkingSpacesOrGaragesNotIncludedInYourRent(uncoveredSpaces = 100, coveredSpaces = 0, garages = 20, totalCost = BigDecimal(12.0), agreementDate = NGRDate("12", "12", "2020").makeString)

      val userAnswers = UserAnswers(CredId("cred-123"))
        .set(WhatYourRentIncludesPage, whatYourRentIncludes).success.value
        .set(DoesYourRentIncludeParkingPage, true).success.value
        .set(HowManyParkingSpacesOrGaragesIncludedInRentPage, parkingSpacesIncluded).success.value
        .set(DoYouPayExtraForParkingSpacesPage, false).success.value
        .set(ParkingSpacesOrGaragesNotIncludedInYourRentPage, parkingSpacesNotIncluded).success.value

      val summaryList = CheckAnswers.createWhatYourRentIncludesRows("cred-123", Some(userAnswers))
      summaryList.rows.size must be >= 8

      val keys = summaryList.rows.map(row => extractText(row.key.content))
      keys must contain(messages("checkAnswers.whatYourRentIncludes.livingAccommodation"))
      keys must contain(messages("checkAnswers.whatYourRentIncludes.bedroomNumbers"))
      keys must contain(messages("checkAnswers.whatYourRentIncludes.doesYourRentIncludeParking"))

      val values = summaryList.rows.map(row => extractText(row.value.content))
      values must contain(messages("service.yes"))
      values must contain("3")
      values must contain(messages("service.no"))
      values.exists(_.contains("100")) mustBe true
    }
  }
  "createWhatYourRentIncludesRows return rows with 'not provided' when data is missing" in {
    val summaryList = CheckAnswers.createWhatYourRentIncludesRows("cred-123", None)
    summaryList.rows.size mustBe 3
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
      values.exists(_.contains("£15,000")) mustBe true
      values must contain(messages("service.yes"))
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
    extractText(summaryList.rows.head.value.content) must include("£20,000")
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
      values.exists(_.contains("£5,000")) mustBe true
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

    "return gotMoney = Yes and paidMoney = No when explicitly set, with no extra details" in {
      val userAnswers = UserAnswers(CredId("cred-123"))
        .set(DidYouGetMoneyFromLandlordPage, true).success.value
        .set(DidYouPayAnyMoneyToLandlordPage, false).success.value

      val summaryListOpt = CheckAnswers.createPaymentRows("cred-123", Some(userAnswers))

      summaryListOpt mustBe defined
      val summaryList = summaryListOpt.get

      summaryList.rows.size mustBe 2
      val keys = summaryList.rows.map(row => extractText(row.key.content))
      keys must contain allOf(
        messages("checkAnswers.payments.didYouGetMoneyFromLandlord"),
        messages("checkAnswers.payments.didYouPayAnyMoneyToLandlord")
      )

      val values = summaryList.rows.map(row => extractText(row.value.content))
      values must contain(messages("service.yes"))
      values must contain(messages("service.no"))
    }

    "return gotMoney rows including amount and date when lease details are present" in {
      val lease = MoneyToTakeOnTheLease(BigDecimal(1000), NGRDate("01","01","2021").makeString)

      val userAnswers = UserAnswers(CredId("cred-123"))
        .set(DidYouGetMoneyFromLandlordPage, true).success.value
        .set(DidYouPayAnyMoneyToLandlordPage, false).success.value
        .set(MoneyToTakeOnTheLeasePage, lease).success.value

      val summaryListOpt = CheckAnswers.createPaymentRows("cred-123", Some(userAnswers))

      summaryListOpt mustBe defined
      val summaryList = summaryListOpt.get

      summaryList.rows.size mustBe 4
      val keys = summaryList.rows.map(row => extractText(row.key.content))
      keys must contain allOf(
        messages("checkAnswers.payments.didYouGetMoneyFromLandlord"),
        messages("checkAnswers.payments.didYouPayAnyMoneyToLandlord"),
        messages("checkAnswers.payments.didYouGetMoneyFromLandlord.amount"),
        messages("checkAnswers.payments.didYouGetMoneyFromLandlord.date")
      )
    }

    "return paidMoney rows including amount and date when advance payment details are present" in {
      val advance = MoneyYouPaidInAdvanceToLandlord(BigDecimal(500), NGRDate("05","05","2021").makeString)

      val userAnswers = UserAnswers(CredId("cred-123"))
        .set(DidYouGetMoneyFromLandlordPage, false).success.value
        .set(DidYouPayAnyMoneyToLandlordPage, true).success.value
        .set(MoneyYouPaidInAdvanceToLandlordPage, advance).success.value

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
    }

    "return all rows when both lease and advance payment details are present" in {
      val lease = MoneyToTakeOnTheLease(BigDecimal(1000), NGRDate("01","01","2021").makeString)
      val advance = MoneyYouPaidInAdvanceToLandlord(BigDecimal(500), NGRDate("05","05","2021").makeString)

      val userAnswers = UserAnswers(CredId("cred-123"))
        .set(DidYouGetMoneyFromLandlordPage, true).success.value
        .set(MoneyToTakeOnTheLeasePage, lease).success.value
        .set(DidYouPayAnyMoneyToLandlordPage, true).success.value
        .set(MoneyYouPaidInAdvanceToLandlordPage, advance).success.value

      val summaryListOpt = CheckAnswers.createPaymentRows("cred-123", Some(userAnswers))

      summaryListOpt mustBe defined
      val summaryList = summaryListOpt.get

      summaryList.rows.size mustBe 6
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


  "createBreakClauseRows" must {

    "return None when no break clause data exists" in {
      val result = createBreakClauseRows("credId", Some(UserAnswers(CredId("credId"))))
      result mustBe None
    }

    "return Some SummaryList with correct rows when all data exists" in {
      val confirmBreakClause = true
      val incentiveDetails = DidYouGetIncentiveForNotTriggeringBreakClause(checkBox = Set(YesRentFreePeriod, YesLumpSum))
      val rentFreePeriod = AboutTheRentFreePeriod(months = 2, date = NGRDate("01", "01", "2025").makeString)
      val lumpSum = BigDecimal(7500.00)

      val userAnswers = UserAnswers(CredId("credId"))
        .set(ConfirmBreakClausePage, confirmBreakClause).success.value
        .set(DidYouGetIncentiveForNotTriggeringBreakClausePage, incentiveDetails).success.value
        .set(HowMuchWasTheLumpSumPage, lumpSum).success.value
        .set(AboutTheRentFreePeriodPage, rentFreePeriod).success.value

      val result = createBreakClauseRows("credId", Some(userAnswers))

      result mustBe defined
      val summaryList = result.get

      summaryList.rows.size mustBe 5

      summaryList.rows.head.value.content.asHtml.toString must include("Yes")
      summaryList.rows(1).value.content.asHtml.toString must include(messages("didYouGetIncentiveForNotTriggeringBreakClause.checkbox"))
      summaryList.rows(2).value.content.asHtml.toString must include("£7,500")
      summaryList.rows(3).value.content.asHtml.toString must include("2 months")
      summaryList.rows(4).value.content.asHtml.toString must include("1 January 2025")
    }

    "handle single checkbox selection correctly" in {
      val incentiveDetails = DidYouGetIncentiveForNotTriggeringBreakClause(checkBox = Set(YesRentFreePeriod))
      val userAnswers = UserAnswers(CredId("credId"))
        .set(DidYouGetIncentiveForNotTriggeringBreakClausePage, incentiveDetails).success.value

      val result = createBreakClauseRows("credId", Some(userAnswers))

      result mustBe defined
      val summaryList = result.get
      summaryList.rows.head.value.content.asHtml.toString must include(messages("didYouGetIncentiveForNotTriggeringBreakClause.checkbox1"))
    }

    "format months correctly for singular value" in {
      val rentFreePeriod = AboutTheRentFreePeriod(months = 1, date = NGRDate("01", "01", "2025").makeString)
      val userAnswers = UserAnswers(CredId("credId"))
        .set(AboutTheRentFreePeriodPage, rentFreePeriod).success.value

      val result = createBreakClauseRows("credId", Some(userAnswers))

      result mustBe defined
      val summaryList = result.get
      summaryList.rows.head.value.content.asHtml.toString must include("1 month")
    }
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


  "createFirstRentPeriodRow" must {

    "return None when ProvideDetailsOfFirstRentPeriodPage is missing" in {
      val result = createFirstRentPeriodRow("credId", Some(UserAnswers(CredId("credId"))))
      result mustBe None
    }

    "return Some SummaryList with correct rows when rent period exists" in {
      val rentPeriodDetail = ProvideDetailsOfFirstRentPeriod(
        startDate = LocalDate.parse("2020-01-01"),
        endDate = LocalDate.parse("2020-06-30"),
        isRentPayablePeriod = true,
        rentPeriodAmount = Some(BigDecimal(1200.0))
      )

      val userAnswers = UserAnswers(CredId("credId"))
        .set(ProvideDetailsOfFirstRentPeriodPage, rentPeriodDetail).success.value

      val result = createFirstRentPeriodRow("credId", Some(userAnswers))

      result mustBe defined
      val summaryList = result.get

      summaryList.rows.size mustBe 4

      summaryList.rows.head.value.content.asHtml.toString must include("1 January 2020")
      summaryList.rows(1).value.content.asHtml.toString must include("30 June 2020")
      summaryList.rows(2).value.content.asHtml.toString must include("Yes")
      summaryList.rows(3).value.content.asHtml.toString must include("£1,200")
    }

    "exclude amount row when rentPeriodAmount is None" in {
      val rentPeriodDetail = ProvideDetailsOfFirstRentPeriod(
        startDate = LocalDate.parse("2020-01-01"),
        endDate = LocalDate.parse("2020-06-30"),
        isRentPayablePeriod = false,
        rentPeriodAmount = None
      )

      val userAnswers = UserAnswers(CredId("credId"))
        .set(ProvideDetailsOfFirstRentPeriodPage, rentPeriodDetail).success.value

      val result = createFirstRentPeriodRow("credId", Some(userAnswers))

      result mustBe defined
      val summaryList = result.get

      summaryList.rows.size mustBe 3
      summaryList.rows(2).value.content.asHtml.toString must include("No")
    }
  }


  "createRentPeriodsSummaryLists" should {

    "return None when rent periods details are missing" in {
      val result = createRentPeriodsSummaryLists("credId", Some(UserAnswers(CredId("credId"))))
      result mustBe None
    }

    "return Some with correct SummaryLists when rent periods details exist" in {
      val rentPeriodsDetails = Seq(
        DetailsOfRentPeriod(NGRDate("12", "12", "2020").makeString, BigDecimal(800.0)),
        DetailsOfRentPeriod(NGRDate("13", "5", "2021").makeString, BigDecimal(500.0))
      )

      val userAnswers: UserAnswers = UserAnswers(CredId("credId")).set(ProvideDetailsOfSecondRentPeriodPage, rentPeriodsDetails).success.value

      val result = createRentPeriodsSummaryLists("credId", Some(userAnswers))

      result mustBe defined
      val summaryLists = result.get
      summaryLists.size mustBe 2

      val firstSummaryList = summaryLists.head
      firstSummaryList.rows.head.key.content.asHtml.toString must include("End date")
      firstSummaryList.rows.head.value.content.asHtml.toString must include("12 December 2020")
      firstSummaryList.rows(1).key.content.asHtml.toString must include("Rent for this period (excluding VAT)")
      firstSummaryList.rows(1).value.content.asHtml.toString must include("£800")

      val lastSummaryList = summaryLists.last
      lastSummaryList.rows.head.key.content.asHtml.toString must include("End date")
      lastSummaryList.rows.head.value.content.asHtml.toString must include("13 May 2021")
      lastSummaryList.rows(1).key.content.asHtml.toString must include("Rent for this period (excluding VAT)")
      lastSummaryList.rows(1).value.content.asHtml.toString must include("£500")
    }
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
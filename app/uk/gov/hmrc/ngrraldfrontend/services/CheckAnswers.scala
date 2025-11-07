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

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.NGRSummaryListRow.summarise
import uk.gov.hmrc.ngrraldfrontend.pages.*
import play.api.mvc.Call

object CheckAnswers {

  def buildRow(labelKey: String, value: String, linkId: String, href: Call, hiddenKey: String)(implicit messages: Messages): NGRSummaryListRow =
    NGRSummaryListRow(
      titleMessageKey = labelKey,
      captionKey = None,
      value = Seq(value),
      changeLink = Some(Link(
        href = href,
        linkId = linkId,
        messageKey = "service.change",
        visuallyHiddenMessageKey = Some(hiddenKey)
      ))
    )

  def createLeaseRenewalsSummaryRows(credId: String, userAnswers: Option[UserAnswers])
                                    (implicit messages: Messages): Option[SummaryList] = {
    val answers = userAnswers.getOrElse(UserAnswers(credId))
    val leaseRenewal = answers.get(WhatTypeOfLeaseRenewalPage)

    leaseRenewal.map { value =>
      val displayValue = value match {
        case "RenewedAgreement" => messages("typeOfLeaseRenewal.option1")
        case "SurrenderAndRenewal" => messages("typeOfLeaseRenewal.option2")
        case _ => messages("service.notProvided")
      }

      val row = buildRow(
        labelKey = "checkAnswers.leaseRenewal.typeOfLeaseRenewal",
        value = displayValue,
        linkId = "property-address",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatTypeOfLeaseRenewalController.show(CheckMode),
        hiddenKey =  "property-address"
      )
      SummaryList(Seq(summarise(row)))
    }
  }

  def createLandlordSummaryRows(credId: String, userAnswers: Option[UserAnswers])
                               (implicit messages: Messages): SummaryList = {
    val answers = userAnswers.getOrElse(UserAnswers(credId))
    val landlord = answers.get(LandlordPage)

    val nameRow = buildRow(
      labelKey = "checkAnswers.landlord.fullName",
      value = landlord.map(_.landlordName).getOrElse(messages("service.notProvided")),
      linkId = "landlord-full-name",
      href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.LandlordController.show(CheckMode),
      hiddenKey = "landlord-full-name"
    )

    val relationshipRow = buildRow(
      labelKey = "checkAnswers.landlord.relationship",
      value = landlord.map(l => if (l.hasRelationship) messages("service.yes") else messages("service.no"))
        .getOrElse(messages("service.notProvided")),
      linkId = "landlord-relationship",
      href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.LandlordController.show(CheckMode),
      hiddenKey = "landlord-relationship"
    )

    val relationshipReasonRow = landlord.flatMap(_.landlordRelationship).map { reason =>
      buildRow(
        labelKey = "checkAnswers.landlord.relationship.reason",
        value = reason,
        linkId = "landlord-relationship-reason",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.LandlordController.show(CheckMode),
        hiddenKey = "landlord-relationship-reason"
      )
    }

    val rows = Seq(nameRow, relationshipRow) ++ relationshipReasonRow.toSeq

    SummaryList(rows.map(summarise))
  }

  def createAgreementDetailsRows(credId: String, userAnswers: Option[UserAnswers])
                                (implicit messages: Messages): Option[SummaryList] = {
    val answers = userAnswers.getOrElse(UserAnswers(credId))
    val agreementTypeOpt = answers.get(WhatTypeOfAgreementPage)

    agreementTypeOpt.map { agreementType =>
      val displayValue = agreementType match {
        case "LeaseOrTenancy" => messages("whatTypeOfAgreement.LeaseOrTenancy")
        case "Written" => messages("whatTypeOfAgreement.written")
        case "Verbal" => messages("whatTypeOfAgreement.verbal")
        case _ => messages("service.notProvided")
      }

      val row = buildRow(
        labelKey = "checkAnswers.agreement.whatTypeOfAgreement",
        value = displayValue,
        linkId = "what-type-of-agreement",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatTypeOfAgreementController.show(CheckMode),
        hiddenKey = "what-type-of-agreement"
      )

      SummaryList(Seq(summarise(row)))
    }
  }


  def createRentRows(credId: String, userAnswers: Option[UserAnswers])
                    (implicit messages: Messages): SummaryList = {
    val answers = userAnswers.getOrElse(UserAnswers(credId))

    val rentBasedOn = answers.get(WhatIsYourRentBasedOnPage).map { value =>
      val rentType = value.rentBased match {
        case "OpenMarket" => messages("whatIsYourRentBasedOn.openMarket")
        case "PercentageOpenMarket" => messages("whatIsYourRentBasedOn.percentageOpenMarket")
        case "Turnover" => messages("whatIsYourRentBasedOn.turnover")
        case "PercentageTurnover" => messages("whatIsYourRentBasedOn.percentageTurnover")
        case "TotalOccupancyCost" => messages("whatIsYourRentBasedOn.totalOccupancyCost")
        case "Indexation" => messages("whatIsYourRentBasedOn.indexation")
        case "Other" => messages("whatIsYourRentBasedOn.other")
        case _ => messages("service.notProvided")
      }

      buildRow(
        labelKey = "checkAnswers.rent.whatIsYourRentBasedOn",
        value = rentType,
        linkId = "what-is-your-rent-based-on",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatIsYourRentBasedOnController.show(CheckMode),
        hiddenKey = "what-is-your-rent-based-on"
      )
    }

    val otherReason = answers.get(WhatIsYourRentBasedOnPage).flatMap(_.otherDesc).map { desc =>
      buildRow(
        labelKey = "checkAnswers.rent.otherReason",
        value = desc,
        linkId = "agreed-rent-change",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatIsYourRentBasedOnController.show(CheckMode),
        hiddenKey = "agreed-rent-change"
      )
    }

    val agreedRentChange = answers.get(AgreedRentChangePage).map { value =>
      buildRow(
        labelKey = "checkAnswers.rent.agreedRentChange",
        value = if (value) messages("service.yes") else messages("service.no"),
        linkId = "agreed-rent-change",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreedRentChangeController.show(CheckMode),
        hiddenKey = "agreed-rent-change"
      )
    }

    val didYouAgreeRentWithLandlord = answers.get(DidYouAgreeRentWithLandlordPage).map { value =>
      buildRow(
        labelKey = "checkAnswers.rent.didYouAgreeRentWithLandlord",
        value = if (value) messages("service.yes") else messages("service.no"),
        linkId = "did-you-agree-rent-with-landlord",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouAgreeRentWithLandlordController.show(CheckMode),
        hiddenKey = "did-you-agree-rent-with-landlord"
      )
    }

    val rentInterim = answers.get(RentInterimPage).map { value =>
      buildRow(
        labelKey = "checkAnswers.rent.rentInterim",
        value = if (value) messages("service.yes") else messages("service.no"),
        linkId = "rent-interim",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentInterimController.show(CheckMode),
        hiddenKey = "rent-interim"
      )
    }

    val checkRentPeriod = answers.get(CheckRentFreePeriodPage).map { value =>
      buildRow(
        labelKey = "checkAnswers.rent.checkRentPeriod",
        value = if (value) messages("service.yes") else messages("service.no"),
        linkId = "check-rent-free-period",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckRentFreePeriodController.show(CheckMode),
        hiddenKey = "check-rent-free-period"
      )
    }

    val rows = Seq(
      rentBasedOn,
      otherReason,
      agreedRentChange,
      didYouAgreeRentWithLandlord,
      rentInterim,
      checkRentPeriod
    ).flatten.map(summarise)

    SummaryList(rows)
  }

  def createRentPeriodRow(credId: String, userAnswers: Option[UserAnswers])
                         (implicit messages: Messages): Option[SummaryList] = {
    val answers = userAnswers.getOrElse(UserAnswers(credId))
    val rentPeriod = answers.get(ProvideDetailsOfFirstSecondRentPeriodPage)
    val link = uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatTypeOfAgreementController.show(CheckMode)

    rentPeriod.map { value =>
      val startRow = buildRow(
        labelKey = "checkAnswers.rentPeriod.provideDetailsOfFirstSecondRentPeriod.start",
        value = value.firstDateStart,
        linkId = "provide-details-of-first-period-start",
        href = link,
        hiddenKey = "provide-details-of-first-period-start"
      )

      val endRow = buildRow(
        labelKey = "checkAnswers.rentPeriod.provideDetailsOfFirstSecondRentPeriod.end",
        value = value.firstDateEnd,
        linkId = "provide-details-of-first-period-end",
        href = link,
        hiddenKey = "provide-details-of-first-period-end"
      )

      SummaryList(Seq(startRow, endRow).map(summarise))
    }
  }

  def createWhatYourRentIncludesRows(credId: String, userAnswers: Option[UserAnswers])(implicit messages: Messages): SummaryList = {
    val answers = userAnswers.getOrElse(UserAnswers(credId))
    val whatYourRentIncludesOpt = answers.get(WhatYourRentIncludesPage)
    val link = uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatYourRentIncludesController.show(CheckMode)

    def yesNo(value: Boolean): String = if (value) messages("service.yes") else messages("service.no")

    val livingAccommodationRow = buildRow(
      labelKey = "checkAnswers.whatYourRentIncludes.livingAccommodation",
      value = whatYourRentIncludesOpt.map(v => yesNo(v.livingAccommodation)).getOrElse(messages("service.notProvided")),
      linkId = "what-you-rent-includes",
      href = link,
      hiddenKey = "what-you-rent-includes"
    )

    val bedroomNumbersRow = whatYourRentIncludesOpt.flatMap(_.bedroomNumbers).map { value =>
      buildRow(
        labelKey = "checkAnswers.whatYourRentIncludes.bedroomNumbers",
        value = value.toString,
        linkId = "bedroom-numbers",
        href = link,
        hiddenKey = "bedroom-numbers"
      )
    }

    val rentPartAddressRow = buildRow(
      labelKey = "checkAnswers.whatYourRentIncludes.rentPartAddress",
      value = whatYourRentIncludesOpt.map(v => yesNo(v.rentPartAddress)).getOrElse(messages("service.notProvided")),
      linkId = "rent-part-address",
      href = link,
      hiddenKey = "rent-part-address"
    )

    val rentEmptyShellRow = buildRow(
      labelKey = "checkAnswers.whatYourRentIncludes.rentEmptyShell",
      value = whatYourRentIncludesOpt.map(v => yesNo(v.rentEmptyShell)).getOrElse(messages("service.notProvided")),
      linkId = "rent-empty-shell",
      href = link,
      hiddenKey = "rent-empty-shell"
    )

    val rentIncBusinessRatesRow = whatYourRentIncludesOpt.flatMap(_.rentIncBusinessRates).map { value =>
      buildRow(
        labelKey = "checkAnswers.whatYourRentIncludes.rentIncBusinessRates",
        value = yesNo(value),
        linkId = "rent-inc-business-rates",
        href = link,
        hiddenKey = "rent-inc-business-rates"
      )
    }

    val rentIncWaterChargesRow = whatYourRentIncludesOpt.flatMap(_.rentIncWaterCharges).map { value =>
      buildRow(
        labelKey = "checkAnswers.whatYourRentIncludes.rentIncWaterCharges",
        value = yesNo(value),
        linkId = "rent-inc-water-charges",
        href = link,
        hiddenKey = "rent-inc-water-charges"
      )
    }

    val rentIncServiceRow = whatYourRentIncludesOpt.flatMap(_.rentIncService).map { value =>
      buildRow(
        labelKey = "checkAnswers.whatYourRentIncludes.rentIncService",
        value = yesNo(value),
        linkId = "rent-in-service",
        href = link,
        hiddenKey = "rent-in-service"
      )
    }

    val rows = Seq(
      Some(livingAccommodationRow),
      bedroomNumbersRow,
      Some(rentPartAddressRow),
      Some(rentEmptyShellRow),
      rentIncBusinessRatesRow,
      rentIncWaterChargesRow,
      rentIncServiceRow
    ).flatten.map(summarise)

    SummaryList(rows)
  }

  def createRepairsAndInsurance(credId: String, userAnswers: Option[UserAnswers])(implicit messages: Messages): SummaryList = {
    val answers = userAnswers.getOrElse(UserAnswers(credId))
    val valueOpt = answers.get(RepairsAndInsurancePage)
    val link = uk.gov.hmrc.ngrraldfrontend.controllers.routes.RepairsAndInsuranceController.show(CheckMode)

    def convertAnswer(answer: String): String =
      answer match{
        case "YouAndLandlord" => messages("repairsAndInsurance.radio.youAndLandlord")
        case "Landlord" => messages("repairsAndInsurance.radio.landlord")
        case message => message
      }
      
    val internalRepairs = buildRow(
      labelKey = "checkAnswers.repairsAndInsurance.internalRepairs",
      value = valueOpt.map(v => convertAnswer(v.internalRepairs)).getOrElse(messages("service.notProvided")),
      linkId = "internal-repairs",
      href = link,
      hiddenKey = "internal-repairs"
    )

    val externalRepairs = buildRow(
      labelKey = "checkAnswers.repairsAndInsurance.externalRepairs",
      value = valueOpt.map(v => convertAnswer(v.externalRepairs)).getOrElse(messages("service.notProvided")),
      linkId = "external-repairs",
      href = link,
      hiddenKey = "external-repairs"
    )

    val buildingInsurance = buildRow(
      labelKey = "checkAnswers.repairsAndInsurance.buildingInsurance",
      value = valueOpt.map(v => convertAnswer(v.buildingInsurance)).getOrElse(messages("service.notProvided")),
      linkId = "building-insurance",
      href = link,
      hiddenKey = "building-insurance"
    )

    SummaryList(Seq(internalRepairs, externalRepairs, buildingInsurance).map(summarise))
  }


  def createRentReviewRows(credId: String, userAnswers: Option[UserAnswers])(implicit messages: Messages): SummaryList = {
    val answers = userAnswers.getOrElse(UserAnswers(credId))
    val rentReview = answers.get(RentReviewPage)
    val rentDetails = answers.get(RentReviewDetailsPage)
    val rentReviewLink = uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentReviewController.show(CheckMode)
    val rentDetailsLink = uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentReviewDetailsController.show(CheckMode)

    val hasIncludeRentReview = buildRow(
      labelKey = "checkAnswers.rentReview.hasIncludeRentReview",
      value = rentReview.map(value => if (value.hasIncludeRentReview) "Yes" else "No").getOrElse(messages("service.notProvided")),
      linkId = "has-include-rent-review",
      href = rentReviewLink,
      hiddenKey = "has-include-rent-review"
    )

    val howOftenReviewed = rentReview.map { value =>
      buildRow(
        labelKey = "checkAnswers.whatYourRentIncludes.rentIncService",
        value = s"${value.rentReviewYears.getOrElse(0)} years ${value.rentReviewMonths.getOrElse(0)} months",
        linkId = "how-often-reviewed",
        href = rentReviewLink,
        hiddenKey = "how-often-reviewed"
      )
    }.toSeq

    val canRentGoDown = rentReview.map { r =>
      buildRow(
        labelKey = "checkAnswers.whatYourRentIncludes.rentIncService",
        value = if (r.canRentGoDown) messages("service.yes") else messages("service.no"),
        linkId = "can-rent-go-down",
        href = rentReviewLink,
        hiddenKey = "can-rent-go-down"
      )
    }.toSeq

    val annualAmount = buildRow(
      labelKey = "checkAnswers.rentReviewDetails.annualAmount",
      value = rentDetails.map(d => s"£ ${d.annualRentAmount}").getOrElse(messages("service.notProvided")),
      linkId = "annual-amount",
      href = rentDetailsLink,
      hiddenKey = "annual-amount"
    )

    val whatHappensAtRentReview = buildRow(
      labelKey = "checkAnswers.rentReviewDetails.whatHappensAtRentReview",
      value = rentDetails.map(_.whatHappensAtRentReview match {
        case "OnlyGoUp" => messages("rentReviewDetails.whatHappensAtRentReview.radio2.text")
        case _          => messages("rentReviewDetails.whatHappensAtRentReview.radio1.text")
      }).getOrElse(messages("service.notProvided")),
      linkId = "what-happens-at-rent-review",
      href = rentDetailsLink,
      hiddenKey = "what-happens-at-rent-review"
    )

    val startDate = buildRow(
      labelKey = "checkAnswers.rentReviewDetails.startDate",
      value = rentDetails.map(_.startDate).getOrElse(messages("service.notProvided")),
      linkId = "start-date",
      href = rentDetailsLink,
      hiddenKey = "start-date"
    )

    val hasAgreedNewRent = buildRow(
      labelKey = "checkAnswers.rentReviewDetails.hasAgreedNewRent",
      value = rentDetails.map(d => if (d.hasAgreedNewRent) messages("service.yes") else messages("service.no")).getOrElse(messages("service.notProvided")),
      linkId = "has-agreed-new-rent",
      href = rentDetailsLink,
      hiddenKey = "has-agreed-new-rent"
    )

    val whoAgreed = rentDetails.flatMap(_.whoAgreed).map {
      case "Arbitrator" => messages("rentReviewDetails.whoAgreed.radio1.text")
      case _            => messages("rentReviewDetails.whoAgreed.radio2.text")
    }.getOrElse(messages("service.notProvided"))

    val whoAgreedRow = buildRow(
      labelKey = "checkAnswers.rentReviewDetails.whoAgreed",
      value = whoAgreed,
      linkId = "who-agreed",
      href = rentDetailsLink,
      hiddenKey = "who-agreed"
    )

    val rows = (rentReview, rentDetails) match {
      case (Some(rentReview), None) if !rentReview.hasIncludeRentReview =>
        Seq(hasIncludeRentReview) ++ canRentGoDown
      case (Some(_), None) =>
        Seq(hasIncludeRentReview) ++ howOftenReviewed ++ canRentGoDown
      case (None, Some(rentDetails)) if rentDetails.whoAgreed.nonEmpty =>
        Seq(annualAmount, whatHappensAtRentReview, startDate, hasAgreedNewRent, whoAgreedRow)
      case (None, Some(_)) =>
        Seq(annualAmount, whatHappensAtRentReview, startDate, hasAgreedNewRent)
      case (None, None) =>
        Seq.empty
    }

    SummaryList(rows.map(summarise))
  }

  def createPaymentRows(credId: String, userAnswers: Option[UserAnswers])(implicit messages: Messages): Option[SummaryList] = {
    val answers = userAnswers.getOrElse(UserAnswers(credId))
    val gotMoney = answers.get(DidYouGetMoneyFromLandlordPage)
    val paidMoney = answers.get(DidYouPayAnyMoneyToLandlordPage)

    val gotMoneyRow = gotMoney.map { value =>
      buildRow(
        labelKey = "checkAnswers.payments.didYouGetMoneyFromLandlord",
        value = if (value) messages("service.yes") else messages("service.no"),
        linkId = "did-you-get-money-from-landlord",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouGetMoneyFromLandlordController.show(CheckMode),
        hiddenKey = "did-you-get-money-from-landlord"
      )
    }

    val paidMoneyRow = paidMoney.map { value =>
      buildRow(
        labelKey = "checkAnswers.payments.didYouPayAnyMoneyToLandlord",
        value = if (value) messages("service.yes") else messages("service.no"),
        linkId = "did-you-pay-money-to-landlord",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouPayAnyMoneyToLandlordController.show(CheckMode),
        hiddenKey = "did-you-pay-money-to-landlord"
      )
    }

    val rows = gotMoneyRow.toSeq ++ paidMoneyRow.toSeq

    if (rows.nonEmpty) Some(SummaryList(rows.map(summarise))) else None
  }
}
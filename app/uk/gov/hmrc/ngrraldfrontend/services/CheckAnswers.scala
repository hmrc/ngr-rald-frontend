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
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.NGRSummaryListRow.summarise
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.pages.*

import java.time.format.DateTimeFormatter
import java.util.Locale

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

  def yesNo(value: Boolean)(implicit messages: Messages): String = if (value) messages("service.yes") else messages("service.no")

  def createLandlordSummaryRows(credId: String, userAnswers: Option[UserAnswers])
                               (implicit messages: Messages): SummaryList = {
    val answers = userAnswers.getOrElse(UserAnswers(CredId(credId)))
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
      value = landlord.map(value => yesNo(value.hasRelationship))
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
    SummaryList(rows.map(summarise), classes = "govuk-!-margin-bottom-9")
  }

  def createAgreementDetailsRows(credId: String, userAnswers: Option[UserAnswers])
                                (implicit messages: Messages): Option[SummaryList] = {
    val answers = userAnswers.getOrElse(UserAnswers(CredId(credId)))
    val agreementTypeOpt = answers.get(WhatTypeOfAgreementPage)
    val agreement = answers.get(AgreementPage)

    agreementTypeOpt.map { agreementType =>
      val displayValue = agreementType match {
        case "LeaseOrTenancy" => messages("whatTypeOfAgreement.LeaseOrTenancy")
        case "Written" => messages("whatTypeOfAgreement.written")
        case "Verbal" => messages("whatTypeOfAgreement.verbal")
        case _ => messages("service.notProvided")
      }

      val agreementTypeRow = buildRow(
        labelKey = "checkAnswers.agreement.whatTypeOfAgreement",
        value = displayValue,
        linkId = "what-type-of-agreement",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatTypeOfAgreementController.show(CheckMode),
        hiddenKey = "what-type-of-agreement"
      )

      val agreementStartDate = agreement.map { value =>
        buildRow(
          labelKey = "checkAnswers.agreement.startDate",
          value = NGRDate.formatDate(value.agreementStart),
          linkId = "agreement-start-date",
          href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreementController.show(CheckMode),
          hiddenKey = "agreement-start-date"
        )
      }.toSeq

      val agreementOpenEnded = agreement.map { value =>
        buildRow(
          labelKey = "checkAnswers.agreement.isOpenEnded",
          value = value.isOpenEnded match
            case true => Messages("agreementVerbal.yes")
            case false => Messages("agreementVerbal.no"),
          linkId = "is-open-ended",
          href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreementController.show(CheckMode),
          hiddenKey = "is-open-ended"
        )
      }.toSeq

      val agreementEndDate = agreement.flatMap { value =>
        value.openEndedDate.map { date =>
          buildRow(
            labelKey = "checkAnswers.agreement.endDate",
            value = NGRDate.formatDate(date),
            linkId = "is-open-ended",
            href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreementController.show(CheckMode),
            hiddenKey = "is-open-ended"
          )
        }
      }

      val breakClause = agreement.map { value =>
        buildRow(
          labelKey = "checkAnswers.agreement.breakClause",
          value = yesNo(value.haveBreakClause),
          linkId = "break-clause",
          href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreementController.show(CheckMode),
          hiddenKey = "break-clause"
        )
      }.toSeq

      val breakClauseDetails = agreement.flatMap { value =>
        value.breakClauseInfo.map { info =>
          buildRow(
            labelKey = "checkAnswers.agreement.breakClauseDetails",
            value = info,
            linkId = "break-clause-details",
            href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreementController.show(CheckMode),
            hiddenKey = "break-clause-details"
          )
        }
      }
      val rows = Seq(agreementTypeRow) ++ agreementStartDate ++ agreementOpenEnded ++ agreementEndDate ++ breakClause ++ breakClauseDetails
      SummaryList(rows.map(summarise), classes = "govuk-!-margin-bottom-9")
    }
  }

  def createLeaseRenewalsSummaryRows(credId: String, userAnswers: Option[UserAnswers])
                                    (implicit messages: Messages): Option[SummaryList] = {
    val answers = userAnswers.getOrElse(UserAnswers(CredId(credId)))
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
        hiddenKey = "property-address"
      )
      SummaryList(rows = Seq(summarise(row)), classes = "govuk-!-margin-bottom-9")
    }
  }

  def createRentRows(credId: String, userAnswers: Option[UserAnswers])
                    (implicit messages: Messages): SummaryList = {
    val answers = userAnswers.getOrElse(UserAnswers(CredId(credId)))

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
        value = yesNo(value),
        linkId = "agreed-rent-change",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreedRentChangeController.show(CheckMode),
        hiddenKey = "agreed-rent-change"
      )
    }

    val rentDatesAgreeRow = answers.get(RentDatesAgreePage).map { value =>
      buildRow(
        labelKey = "checkAnswers.rents.whenDidYouAgree",
        value = NGRDate.formatDate(value),
        linkId = "when-did-you-agree",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentDatesAgreeController.show(CheckMode),
        hiddenKey = "when-did-you-agree"
      )
    }

    val rentDatesAgreeStartRow = answers.get(RentDatesAgreeStartPage).map { value =>
      buildRow(
        labelKey = "checkAnswers.rents.whenDidYouAgree",
        value = NGRDate.formatDate(value.agreedDate),
        linkId = "when-did-you-agree",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentDatesAgreeStartController.show(CheckMode),
        hiddenKey = "when-did-you-agree"
      )
    }


    val didYouAgreeRentWithLandlord = answers.get(DidYouAgreeRentWithLandlordPage).map { value =>
      buildRow(
        labelKey = "checkAnswers.rent.didYouAgreeRentWithLandlord",
        value = yesNo(value),
        linkId = "did-you-agree-rent-with-landlord",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouAgreeRentWithLandlordController.show(CheckMode),
        hiddenKey = "did-you-agree-rent-with-landlord"
      )
    }

    val rentInterim = answers.get(RentInterimPage).map { value =>
      buildRow(
        labelKey = "checkAnswers.rent.rentInterim",
        value = yesNo(value),
        linkId = "rent-interim",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentInterimController.show(CheckMode),
        hiddenKey = "rent-interim"
      )
    }

    val totalAnualRent = answers.get(HowMuchIsTotalAnnualRentPage).map { value =>
      buildRow(
        labelKey = "checkAnswers.rent.totalAnnualRent",
        value = s"£$value",
        linkId = "how-much-is-total-annual-rent",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowMuchIsTotalAnnualRentController.show(CheckMode),
        hiddenKey = "how-much-is-total-annual-rent"
      )
    }

    val checkRentPeriod = answers.get(CheckRentFreePeriodPage).map { value =>
      buildRow(
        labelKey = "checkAnswers.rent.checkRentPeriod",
        value = yesNo(value),
        linkId = "check-rent-free-period",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckRentFreePeriodController.show(CheckMode),
        hiddenKey = "check-rent-free-period"
      )
    }

    val rentFreePeriodMonths = answers.get(RentFreePeriodPage).map { value =>
      buildRow(
        labelKey = "checkAnswers.rents.rentFreePeriod",
        value = s"${value.months.toString} months",
        linkId = "rent-free-period-months",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentFreePeriodController.show(CheckMode),
        hiddenKey = "rent-free-period-months"
      )
    }

    val rentFreePeriodReason = answers.get(RentFreePeriodPage).map { value =>
      buildRow(
        labelKey = "checkAnswers.rents.rentFreePeriodReason",
        value = value.reasons,
        linkId = "rent-free-period-reason",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentFreePeriodController.show(CheckMode),
        hiddenKey = "rent-free-period-reason"
      )
    }

    val rentDatesPaymentStartRow = answers.get(RentDatesAgreeStartPage).map { value =>
      buildRow(
        labelKey = "checkAnswers.rents.startPayingDate",
        value = NGRDate.formatDate(value.startPayingDate),
        linkId = "start-paying-date",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentDatesAgreeStartController.show(CheckMode),
        hiddenKey = "start-paying-date"
      )
    }

    val rows = Seq(
      rentBasedOn,
      otherReason,
      agreedRentChange,
      rentDatesAgreeRow,
      rentDatesAgreeStartRow,
      didYouAgreeRentWithLandlord,
      rentInterim,
      totalAnualRent,
      checkRentPeriod,
      rentFreePeriodMonths,
      rentFreePeriodReason,
      rentDatesPaymentStartRow
    ).flatten.map(summarise)

    SummaryList(rows, classes = "govuk-!-margin-bottom-9")
  }

  def createRentPeriodRow(credId: String, userAnswers: Option[UserAnswers])
                         (implicit messages: Messages): Option[SummaryList] = {
    val answers = userAnswers.getOrElse(UserAnswers(CredId(credId)))
    val rentPeriod = answers.get(ProvideDetailsOfFirstRentPeriodPage)
    val link = uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatTypeOfAgreementController.show(CheckMode)
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)

    rentPeriod.map { value =>
      val startRow = buildRow(
        labelKey = "checkAnswers.rentPeriod.provideDetailsOfFirstRentPeriod.start",
        value = value.startDate.format(formatter),
        linkId = "provide-details-of-first-period-start",
        href = link,
        hiddenKey = "provide-details-of-first-period-start"
      )

      val endRow = buildRow(
        labelKey = "checkAnswers.rentPeriod.provideDetailsOfFirstRentPeriod.end",
        value = value.endDate.format(formatter),
        linkId = "provide-details-of-first-period-end",
        href = link,
        hiddenKey = "provide-details-of-first-period-end"
      )

      val isRentPayablePeriod = buildRow(
        labelKey = "checkAnswers.rentPeriod.provideDetailsOfFirstSecondRentPeriod.isRentPayablePeriod",
        value = yesNo(value.isRentPayablePeriod),
        linkId = "provide-details-of-first-period-payable",
        href = link,
        hiddenKey = "provide-details-of-first-period-payable"
      ) 
      SummaryList(Seq(startRow, endRow, isRentPayablePeriod).map(summarise), classes = "govuk-!-margin-bottom-9")
    }
  }

  def createWhatYourRentIncludesRows(credId: String, userAnswers: Option[UserAnswers])(implicit messages: Messages): SummaryList = {
    val answers = userAnswers.getOrElse(UserAnswers(CredId(credId)))
    val whatYourRentIncludesOpt = answers.get(WhatYourRentIncludesPage)
    val link = uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatYourRentIncludesController.show(CheckMode)

    val doesYourRentIncludeParkingValue = answers.get(DoesYourRentIncludeParkingPage)
    val howManyParkingSpacesOrGaragesIncludedInRentValue = answers.get(HowManyParkingSpacesOrGaragesIncludedInRentPage)
    val doYouPayExtraForParkingSpacesValue = answers.get(DoYouPayExtraForParkingSpacesPage)
    val parkingSpacesOrGaragesNotIncludedInYourRentPageValue = answers.get(ParkingSpacesOrGaragesNotIncludedInYourRentPage)

    val livingAccommodationRow = buildRow(
      labelKey = "checkAnswers.whatYourRentIncludes.livingAccommodation",
      value = whatYourRentIncludesOpt.map(value => yesNo(value.livingAccommodation)).getOrElse(messages("service.notProvided")),
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
      value = whatYourRentIncludesOpt.map(value => yesNo(value.rentPartAddress)).getOrElse(messages("service.notProvided")),
      linkId = "rent-part-address",
      href = link,
      hiddenKey = "rent-part-address"
    )

    val rentEmptyShellRow = buildRow(
      labelKey = "checkAnswers.whatYourRentIncludes.rentEmptyShell",
      value = whatYourRentIncludesOpt.map(value => yesNo(value.rentEmptyShell)).getOrElse(messages("service.notProvided")),
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

    val doesYourRentIncludeParking = doesYourRentIncludeParkingValue.map { value =>
      buildRow(
        labelKey = "checkAnswers.whatYourRentIncludes.doesYourRentIncludeParking",
        value = yesNo(value),
        linkId = "landlord-full-name",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.DoesYourRentIncludeParkingController.show(CheckMode),
        hiddenKey = "landlord-full-name"
      )
    }

    val howManyUncoveredSpacesIncludedInRent = howManyParkingSpacesOrGaragesIncludedInRentValue.map {
      value =>
        buildRow(
          labelKey = "checkAnswers.whatYourRentIncludes.howManyUncoveredSpacesIncludedInRent",
          value = value.uncoveredSpaces.toString,
          linkId = "landlord-relationship-reason",
          href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowManyParkingSpacesOrGaragesIncludedInRentController.show(CheckMode),
          hiddenKey = "landlord-relationship-reason"
        )
    }

    val howManyCoveredSpacesIncludedInRent = howManyParkingSpacesOrGaragesIncludedInRentValue.map {
      value =>
        buildRow(
          labelKey = "checkAnswers.whatYourRentIncludes.howManyCoveredSpacesIncludedInRent",
          value = value.coveredSpaces.toString,
          linkId = "landlord-relationship-reason",
          href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowManyParkingSpacesOrGaragesIncludedInRentController.show(CheckMode),
          hiddenKey = "landlord-relationship-reason"
        )
    }

    val howManyGaragesIncludedInRent = howManyParkingSpacesOrGaragesIncludedInRentValue.map {
      value =>
        buildRow(
          labelKey = "checkAnswers.whatYourRentIncludes.howManyGaragesIncludedInRent",
          value = value.garages.toString,
          linkId = "landlord-relationship-reason",
          href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowManyParkingSpacesOrGaragesIncludedInRentController.show(CheckMode),
          hiddenKey = "landlord-relationship-reason"
        )
    }

    val doYouPayExtraForParkingSpaces = doYouPayExtraForParkingSpacesValue.map{
      value =>
        buildRow(
          labelKey = "checkAnswers.whatYourRentIncludes.doYouPayExtraForParkingSpaces",
          value = yesNo(value),
          linkId = "landlord-full-name",
          href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.DoYouPayExtraForParkingSpacesController.show(CheckMode),
          hiddenKey = "landlord-full-name"
        )
    }



    val parkingSpacesOrGaragesNotIncludedInYourRent = parkingSpacesOrGaragesNotIncludedInYourRentPageValue.map {
      value =>
        buildRow(
          labelKey = "checkAnswers.whatYourRentIncludes.parkingSpacesOrGaragesNotIncludedInYourRent",
          value =
            s"""
              ${messages("parkingSpacesOrGaragesNotIncludedInYourRent.uncoveredSpaces.label")} ${value.uncoveredSpaces}
              ${messages("parkingSpacesOrGaragesNotIncludedInYourRent.coveredSpaces.label")} ${value.coveredSpaces}
              ${messages("parkingSpacesOrGaragesNotIncludedInYourRent.garages.label")} ${value.garages}
              """,
          linkId = "landlord-relationship-reason",
          href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.ParkingSpacesOrGaragesNotIncludedInYourRentController.show(CheckMode),
          hiddenKey = "landlord-relationship-reason"
        )
    }

    val howManyUncoveredSpacesNotIncludedInRent = parkingSpacesOrGaragesNotIncludedInYourRentPageValue.map {
      value =>
        buildRow(
          labelKey = "checkAnswers.whatYourRentIncludes.howManyUncoveredSpacesNotIncludedInRent",
          value = value.uncoveredSpaces.toString,
          linkId = "landlord-relationship-reason",
          href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.ParkingSpacesOrGaragesNotIncludedInYourRentController.show(CheckMode),
          hiddenKey = "landlord-relationship-reason"
        )
    }

    val howManyCoveredSpacesNotIncludedInRent = parkingSpacesOrGaragesNotIncludedInYourRentPageValue.map {
      value =>
        buildRow(
          labelKey = "checkAnswers.whatYourRentIncludes.howManyCoveredSpacesNotIncludedInRent",
          value = value.coveredSpaces.toString,
          linkId = "landlord-relationship-reason",
          href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.ParkingSpacesOrGaragesNotIncludedInYourRentController.show(CheckMode),
          hiddenKey = "landlord-relationship-reason"
        )
    }

    val howManyGaragesNotIncludedInRent = parkingSpacesOrGaragesNotIncludedInYourRentPageValue.map {
      value =>
        buildRow(
          labelKey = "checkAnswers.whatYourRentIncludes.howManyGaragesNotIncludedInRent",
          value = value.garages.toString,
          linkId = "landlord-relationship-reason",
          href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.ParkingSpacesOrGaragesNotIncludedInYourRentController.show(CheckMode),
          hiddenKey = "landlord-relationship-reason"
        )
    }

    val totalCost = parkingSpacesOrGaragesNotIncludedInYourRentPageValue.map {
      value =>
        buildRow(
          labelKey = "checkAnswers.whatYourRentIncludes.parkingSpacesOrGaragesNotIncludedInYourRent.totalCost",
          value = s"£${value.totalCost.toString()}",
          linkId = "landlord-relationship-reason",
          href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.ParkingSpacesOrGaragesNotIncludedInYourRentController.show(CheckMode),
          hiddenKey = "landlord-relationship-reason"
        )
    }

    val agreementDate = parkingSpacesOrGaragesNotIncludedInYourRentPageValue.map {
      value =>
        buildRow(
          labelKey = "checkAnswers.whatYourRentIncludes.parkingSpacesOrGaragesNotIncludedInYourRent.agreementDate",
          value = NGRDate.formatDate(value.agreementDate),
          linkId = "landlord-relationship-reason",
          href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.ParkingSpacesOrGaragesNotIncludedInYourRentController.show(CheckMode),
          hiddenKey = "landlord-relationship-reason"
        )
    }

    val rows = Seq(
      Some(livingAccommodationRow),
      bedroomNumbersRow,
      Some(rentPartAddressRow),
      Some(rentEmptyShellRow),
      rentIncBusinessRatesRow,
      rentIncWaterChargesRow,
      rentIncServiceRow,
      doesYourRentIncludeParking,
      howManyUncoveredSpacesIncludedInRent,
      howManyCoveredSpacesIncludedInRent,
      howManyGaragesIncludedInRent,
      doYouPayExtraForParkingSpaces,
      howManyUncoveredSpacesNotIncludedInRent,
      howManyCoveredSpacesNotIncludedInRent,
      howManyGaragesNotIncludedInRent,
      totalCost,
      agreementDate
    ).flatten.map(summarise)

    SummaryList(rows, classes = "govuk-!-margin-bottom-9")
  }

  def createRepairsAndInsurance(credId: String, userAnswers: Option[UserAnswers])(implicit messages: Messages): SummaryList = {
    val answers = userAnswers.getOrElse(UserAnswers(CredId(credId)))
    val valueOpt = answers.get(RepairsAndInsurancePage)
    val link = uk.gov.hmrc.ngrraldfrontend.controllers.routes.RepairsAndInsuranceController.show(CheckMode)

    def convertAnswer(answer: String): String =
      answer match {
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

    SummaryList(Seq(internalRepairs, externalRepairs, buildingInsurance).map(summarise), classes = "govuk-!-margin-bottom-9")
  }


  def createRentReviewRows(credId: String, userAnswers: Option[UserAnswers])(implicit messages: Messages): SummaryList = {
    val answers = userAnswers.getOrElse(UserAnswers(CredId(credId)))
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
        labelKey = "checkAnswers.rentReview.howOftenReviewed",
        value = s"Every ${value.rentReviewYears.getOrElse(0)} years and ${value.rentReviewMonths.getOrElse(0)} months",
        linkId = "how-often-reviewed",
        href = rentReviewLink,
        hiddenKey = "how-often-reviewed"
      )
    }.toSeq

    val canRentGoDown = rentReview.map { r =>
      buildRow(
        labelKey = "checkAnswers.whatYourRentIncludes.rentIncService",
        value = yesNo(r.canRentGoDown),
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
        case _ => messages("rentReviewDetails.whatHappensAtRentReview.radio1.text")
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
      value = rentDetails.map(d => yesNo(d.hasAgreedNewRent)).getOrElse(messages("service.notProvided")),
      linkId = "has-agreed-new-rent",
      href = rentDetailsLink,
      hiddenKey = "has-agreed-new-rent"
    )

    val whoAgreed = rentDetails.flatMap(_.whoAgreed).map {
      case "Arbitrator" => messages("rentReviewDetails.whoAgreed.radio1.text")
      case _ => messages("rentReviewDetails.whoAgreed.radio2.text")
    }.getOrElse(messages("service.notProvided"))

    val whoAgreedRow = buildRow(
      labelKey = "checkAnswers.rentReviewDetails.whoAgreed",
      value = whoAgreed,
      linkId = "who-agreed",
      href = rentDetailsLink,
      hiddenKey = "who-agreed"
    )

    val rows = (rentReview, rentDetails) match {
      case (Some(review), Some(details)) =>
        Seq(hasIncludeRentReview) ++ howOftenReviewed ++ canRentGoDown ++
          Seq(annualAmount, whatHappensAtRentReview, startDate, hasAgreedNewRent, whoAgreedRow)
      case (Some(review), None) if !review.hasIncludeRentReview =>
        Seq(hasIncludeRentReview) ++ canRentGoDown
      case (Some(_), None) =>
        Seq(hasIncludeRentReview) ++ howOftenReviewed ++ canRentGoDown
      case (None, Some(details)) if details.whoAgreed.nonEmpty =>
        Seq(annualAmount, whatHappensAtRentReview, startDate, hasAgreedNewRent, whoAgreedRow)
      case (None, Some(_)) =>
        Seq(annualAmount, whatHappensAtRentReview, startDate, hasAgreedNewRent)
      case (None, None) =>
        Seq.empty
    }
    SummaryList(rows.map(summarise), classes = "govuk-!-margin-bottom-9")
  }

  def createRepairsAndFittingOut(credId: String, userAnswers: Option[UserAnswers])(implicit messages: Messages): Option[SummaryList] = {
    val answers = userAnswers.getOrElse(UserAnswers(CredId(credId)))
    val repairsAndFittingOut = answers.get(RepairsAndFittingOutPage)
    val aboutRepairsAndFittingOut = answers.get(AboutRepairsAndFittingOutPage)

    val repairsAndFittingOutRow = repairsAndFittingOut.map { value =>
      buildRow(
        labelKey = "checkAnswers.repairsAndFittingOut.repairsAndFittingOut",
        value = yesNo(value),
        linkId = "did-you-get-money-from-landlord",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.RepairsAndFittingOutController.show(CheckMode),
        hiddenKey = "did-you-get-money-from-landlord"
      )
    }

    val aboutRepairsAndFittingOutRow = aboutRepairsAndFittingOut.map { value =>
      buildRow(
        labelKey = "checkAnswers.repairsAndFittingOut.date",
        value = NGRMonthYear.formatYearMonth(value.date),
        linkId = "did-you-pay-money-to-landlord",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.AboutRepairsAndFittingOutController.show(CheckMode),
        hiddenKey = "did-you-pay-money-to-landlord"
      )
    }

    val aboutRepairsAndFittingOutCostRow = aboutRepairsAndFittingOut.map { value =>
      buildRow(
        labelKey = "checkAnswers.repairsAndFittingOut.cost",
        value = s"£${value.cost}",
        linkId = "did-you-pay-money-to-landlord",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.AboutRepairsAndFittingOutController.show(CheckMode),
        hiddenKey = "did-you-pay-money-to-landlord"
      )
    }

    val rows = repairsAndFittingOutRow.toSeq ++ aboutRepairsAndFittingOutRow.toSeq ++ aboutRepairsAndFittingOutCostRow.toSeq

    if (rows.nonEmpty) Some(SummaryList(rows.map(summarise), classes = "govuk-!-margin-bottom-9")) else None
  }

  def createPaymentRows(credId: String, userAnswers: Option[UserAnswers])(implicit messages: Messages): Option[SummaryList] = {
    val answers = userAnswers.getOrElse(UserAnswers(CredId(credId)))
    val gotMoney = answers.get(DidYouGetMoneyFromLandlordPage)
    val paidMoney = answers.get(DidYouPayAnyMoneyToLandlordPage)
    val moneyYouPaidInAdvanceToLandlord = answers.get(MoneyYouPaidInAdvanceToLandlordPage)

    val gotMoneyRow = gotMoney.map { value =>
      buildRow(
        labelKey = "checkAnswers.payments.didYouGetMoneyFromLandlord",
        value = yesNo(value),
        linkId = "did-you-get-money-from-landlord",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouGetMoneyFromLandlordController.show(CheckMode),
        hiddenKey = "did-you-get-money-from-landlord"
      )
    }

    val paidMoneyRow = paidMoney.map { value =>
      buildRow(
        labelKey = "checkAnswers.payments.didYouPayAnyMoneyToLandlord",
        value = yesNo(value),
        linkId = "did-you-pay-money-to-landlord",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouPayAnyMoneyToLandlordController.show(CheckMode),
        hiddenKey = "did-you-pay-money-to-landlord"
      )
    }

    val moneyYouPaidInAdvanceToLandlordAmountRow = moneyYouPaidInAdvanceToLandlord.map { value =>
      buildRow(
        labelKey = "checkAnswers.payments.moneyYouPaidInAdvanceToLandlord.amount",
        value = s"£${value.amount.toString()}",
        linkId = "money-you-paid-in-advance-to-landlord-amount",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.MoneyYouPaidInAdvanceToLandlordController.show(CheckMode),
        hiddenKey = "money-you-paid-in-advance-to-landlord-amount"
      )
    }

    val moneyYouPaidInAdvanceToLandlordDateRow = moneyYouPaidInAdvanceToLandlord.map { value =>
      buildRow(
        labelKey = "checkAnswers.payments.moneyYouPaidInAdvanceToLandlord.date",
        value = NGRDate.formatDate(value.date),
        linkId = "money-you-paid-in-advance-to-landlord-amount",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.MoneyYouPaidInAdvanceToLandlordController.show(CheckMode),
        hiddenKey = "money-you-paid-in-advance-to-landlord-amount"
      )
    }


    val rows = gotMoneyRow.toSeq ++ paidMoneyRow.toSeq ++ moneyYouPaidInAdvanceToLandlordAmountRow.toSeq ++ moneyYouPaidInAdvanceToLandlordDateRow.toSeq

    if (rows.nonEmpty) Some(SummaryList(rows.map(summarise), classes = "govuk-!-margin-bottom-9")) else None
  }

  def createBreakClauseRows(credId: String, userAnswers: Option[UserAnswers])
                           (implicit messages: Messages): Option[SummaryList] = {
    val answers = userAnswers.getOrElse(UserAnswers(CredId(credId)))
    val confirmBreakClause = answers.get(ConfirmBreakClausePage)

    confirmBreakClause.map { value =>
      val confirmBreakClause = buildRow(
        labelKey = "checkAnswers.breakClause.confirmBreakClause",
        value = yesNo(value),
        linkId = "property-address",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.ConfirmBreakClauseController.show(CheckMode),
        hiddenKey = "property-address"
      )
      SummaryList(Seq(summarise(confirmBreakClause)), classes = "govuk-!-margin-bottom-9")
    }
  }

  def createOtherDetailsRow(credId: String, userAnswers: Option[UserAnswers])
                           (implicit messages: Messages): SummaryList = {
    val answers = userAnswers.getOrElse(UserAnswers(CredId(credId)))
    val hasAnythingElseAffectedTheRent = answers.get(HasAnythingElseAffectedTheRentPage)

    val hasAnyAffectedRent = hasAnythingElseAffectedTheRent.map{value =>
      buildRow(
        labelKey = "checkAnswers.Otherdetails.hasAnyAffectedRent",
        value =  yesNo(value.radio),
        linkId = "landlord-full-name",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.HasAnythingElseAffectedTheRentController.show(CheckMode),
        hiddenKey = "landlord-full-name"
      )
    }.toSeq

    val reason = hasAnythingElseAffectedTheRent.flatMap(_.reason).map { reason =>
      buildRow(
        labelKey = "checkAnswers.Otherdetails.reason",
        value = reason,
        linkId = "other-details-reason",
        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.HasAnythingElseAffectedTheRentController.show(CheckMode),
        hiddenKey = "other-details-reason"
      )
    }.toSeq

    val rows = hasAnyAffectedRent ++ reason
    SummaryList(rows.map(summarise), classes = "govuk-!-margin-bottom-9")
  }
}
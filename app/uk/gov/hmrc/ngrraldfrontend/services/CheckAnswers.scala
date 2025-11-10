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

object CheckAnswers {

  def createLeaseRenewalsSummaryRows(credId: String, userAnswers: Option[UserAnswers])
                                    (implicit messages: Messages): Option[SummaryList] = {
    userAnswers.getOrElse(UserAnswers(credId)).get(WhatTypeOfLeaseRenewalPage) match {
      case Some(value) => Some(
        SummaryList(
          rows = Seq(
            NGRSummaryListRow(
              messages("checkAnswers.leaseRenewal.typeOfLeaseRenewal"),
              None,
              Seq(value match {
                case "RenewedAgreement" => messages("typeOfLeaseRenewal.option1")
                case "SurrenderAndRenewal" => messages("typeOfLeaseRenewal.option2")
              }),
              changeLink = Some(
                Link(
                  href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatTypeOfLeaseRenewalController.show(CheckMode),
                  linkId = "property-address",
                  messageKey = "service.change",
                  visuallyHiddenMessageKey = Some("property-address")
                )
              )
            )
          ).map(summarise)
        )
      )
      case None => None
    }
  }

  def createLandlordSummaryRows(credId: String, userAnswers: Option[UserAnswers])
                               (implicit messages: Messages): SummaryList = {
    SummaryList(
      rows = Seq(
        NGRSummaryListRow(
          messages("checkAnswers.landlord.fullName"),
          None,
          Seq(userAnswers.getOrElse(UserAnswers(credId)).get(LandlordPage).map(value => value.landlordName).getOrElse("THIS NEEDS CHANGING")), //TODO Handle if their is no answer better
          changeLink = Some(
            Link(
              href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.LandlordController.show(CheckMode),
              linkId = "what-type-of-lease-renewal",
              messageKey = "service.change",
              visuallyHiddenMessageKey = Some("what-type-of-lease-renewal")
            )
          )
        ),
        NGRSummaryListRow(
          messages("checkAnswers.landlord.relationship"),
          None,
          Seq(userAnswers.getOrElse(UserAnswers(credId)).get(LandlordPage).map {
            value => if (value.hasRelationship) {
              messages("service.yes")
            } else messages("service.no")
          }.getOrElse("THIS NEEDS CHANGING")
          ), //TODO Handle if their is no answer better
          changeLink = Some(
            Link(
              href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.LandlordController.show(CheckMode),
              linkId = "what-type-of-lease-renewal",
              messageKey = "service.change",
              visuallyHiddenMessageKey = Some("what-type-of-lease-renewal")
            )
          )
        )
      ).map(summarise) ++
        userAnswers.getOrElse(UserAnswers(credId)).get(LandlordPage)
          .map { value =>
            value.landlordRelationship match {
              case Some(relationship) =>
                Seq(
                  NGRSummaryListRow(
                    messages("checkAnswers.landlord.relationship.reason"),
                    None,
                    Seq(relationship),
                    changeLink = Some(
                      Link(
                        href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.LandlordController.show(CheckMode),
                        linkId = "landlord-relationship-reason",
                        messageKey = "service.change",
                        visuallyHiddenMessageKey = Some("landlord-relationship-reason")
                      )
                    )
                  )
                )
              case None => Seq.empty
            }
          }
          .getOrElse(Seq.empty)
          .map(summarise)
    )
  }

  def createAgreementDetailsRows(credId: String, userAnswers: Option[UserAnswers])
                                (implicit messages: Messages): Option[SummaryList] = {
    userAnswers.getOrElse(UserAnswers(credId)).get(WhatTypeOfAgreementPage) match {
      case Some(value) => Some(
        SummaryList(
          rows = Seq(
            NGRSummaryListRow(
              messages("checkAnswers.agreement.whatTypeOfAgreement"),
              None,
              Seq(userAnswers.getOrElse(UserAnswers(credId)).get(WhatTypeOfAgreementPage).map {
                case "LeaseOrTenancy" => messages("whatTypeOfAgreement.LeaseOrTenancy")
                case "Written" => messages("whatTypeOfAgreement.written")
                case "Verbal" => messages("whatTypeOfAgreement.verbal")
              }.getOrElse("THIS NEEDS CHANGING")), //TODO Handle if their is no answer better
              changeLink = Some(
                Link(
                  href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatTypeOfAgreementController.show(CheckMode),
                  linkId = "what-type-of-agreement",
                  messageKey = "service.change",
                  visuallyHiddenMessageKey = Some("what-type-of-agreement")
                )
              )
            ),
          ).map(summarise)
        )
      )
      case None => None
    }
  }

  def createRentRows(credId: String, userAnswers: Option[UserAnswers])
                    (implicit messages: Messages): SummaryList = {
    val whatIsYourRentBasedOn = NGRSummaryListRow(
      messages("checkAnswers.rent.whatIsYourRentBasedOn"),
      None,
      Seq(userAnswers.getOrElse(UserAnswers(credId)).get(WhatIsYourRentBasedOnPage).map(_.rentBased match {
        case "OpenMarket" => messages("whatIsYourRentBasedOn.openMarket")
        case "PercentageOpenMarket" => messages("whatIsYourRentBasedOn.percentageOpenMarket")
        case "Turnover" => messages("whatIsYourRentBasedOn.turnover")
        case "PercentageTurnover" => messages("whatIsYourRentBasedOn.percentageTurnover")
        case "TotalOccupancyCost" => messages("whatIsYourRentBasedOn.totalOccupancyCost")
        case "Indexation" => messages("whatIsYourRentBasedOn.indexation")
        case "Other" => messages("whatIsYourRentBasedOn.other") //TODO ADD THE OTHER REASON IF THEY HAVE IT
      }).getOrElse("THIS NEEDS CHANGING")), //TODO Handle if their is no answer better
      changeLink = Some(
        Link(
          href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatIsYourRentBasedOnController.show(CheckMode),
          linkId = "what-is-your-rent-based-on",
          messageKey = "service.change",
          visuallyHiddenMessageKey = Some("what-is-your-rent-based-on")
        )
      )
    )

    val other: Seq[NGRSummaryListRow] = userAnswers.getOrElse(UserAnswers(credId)).get(WhatIsYourRentBasedOnPage).map{ value =>
      value.otherDesc match {
        case Some(value) => NGRSummaryListRow(
          messages("checkAnswers.rent.agreedRentChange"),
          None,
          Seq(value),
          changeLink = Some(Link(
            href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreedRentChangeController.show(CheckMode),
            linkId = "agreed-rent-change",
            messageKey = "service.change",
            visuallyHiddenMessageKey = Some("agreed-rent-change")
          ))
        )
      }
    }.toSeq

    val agreedRentChange: Seq[NGRSummaryListRow] = userAnswers.getOrElse(UserAnswers(credId)).get(AgreedRentChangePage).map{ value =>
      NGRSummaryListRow(
        messages("checkAnswers.rent.agreedRentChange"),
        None,
        Seq(if (value) messages("service.yes") else messages("service.no")),
        changeLink = Some(Link(
          href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreedRentChangeController.show(CheckMode),
          linkId = "agreed-rent-change",
          messageKey = "service.change",
          visuallyHiddenMessageKey = Some("agreed-rent-change")
        ))
      )
    }.toSeq

    val didYouAgreeRentWithLandlord: Seq[NGRSummaryListRow] = userAnswers.getOrElse(UserAnswers(credId)).get(DidYouAgreeRentWithLandlordPage).map{ value =>
      NGRSummaryListRow(
        messages("checkAnswers.rent.didYouAgreeRentWithLandlord"),
        None,
        Seq(if (value) messages("service.yes") else messages("service.no")),
        changeLink = Some(Link(
          href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouAgreeRentWithLandlordController.show(CheckMode),
          linkId = "did-you-agree-ren-wit-landlord",
          messageKey = "service.change",
          visuallyHiddenMessageKey = Some("did-you-agree-ren-wit-landlord")
        ))
      )
    }.toSeq

    val rentInterim: Seq[NGRSummaryListRow] = userAnswers.getOrElse(UserAnswers(credId)).get(RentInterimPage).map{ value =>
      NGRSummaryListRow(
        messages("checkAnswers.rent.rentInterim"),
        None,
        Seq(if (value) messages("service.yes") else messages("service.no")),
        changeLink = Some(Link(
          href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentInterimController.show(CheckMode),
          linkId = "rent-interim",
          messageKey = "service.change",
          visuallyHiddenMessageKey = Some("rent-interim")
        ))
      )
    }.toSeq

    val checkRentPeriod: Seq[NGRSummaryListRow] = userAnswers.getOrElse(UserAnswers(credId)).get(CheckRentFreePeriodPage).map{ value =>
      NGRSummaryListRow(
        messages("checkAnswers.rent.checkRentPeriod"),
        None,
        Seq(if (value) messages("service.yes") else messages("service.no")),
        changeLink = Some(Link(
          href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckRentFreePeriodController.show(CheckMode),
          linkId = "check-rent-free-period",
          messageKey = "service.change",
          visuallyHiddenMessageKey = Some("check-rent-free-period")
        ))
      )
    }.toSeq

    SummaryList(
      rows = Seq(whatIsYourRentBasedOn).map(summarise)
        ++ agreedRentChange.map(summarise)
        ++ didYouAgreeRentWithLandlord.map(summarise)
        ++ rentInterim.map(summarise)
        ++ checkRentPeriod.map(summarise)
    )
  }

  def createRentPeriodRow(credId: String, userAnswers: Option[UserAnswers])
                         (implicit messages: Messages): Option[SummaryList] = {
    val link = uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatTypeOfAgreementController.show(CheckMode)
    userAnswers.getOrElse(UserAnswers(credId)).get(ProvideDetailsOfFirstSecondRentPeriodPage) match {
      case Some(value) =>
        Some(
          SummaryList(
            rows = Seq(
              NGRSummaryListRow(
                messages("checkAnswers.rentPeriod.provideDetailsOfFirstSecondRentPeriod.start"),
                None,
                Seq(value.firstDateStart),
                changeLink = Some(
                  Link(
                    href = link,
                    linkId = "provide-details-of-first-period-start",
                    messageKey = "service.change",
                    visuallyHiddenMessageKey = Some("provide-details-of-first-period-start")
                  )
                )
              ),
              NGRSummaryListRow(
                messages("checkAnswers.rentPeriod.provideDetailsOfFirstSecondRentPeriod.end"),
                None,
                Seq(value.firstDateEnd),
                changeLink = Some(
                  Link(
                    href = link,
                    linkId = "provide-details-of-first-period-end",
                    messageKey = "service.change",
                    visuallyHiddenMessageKey = Some("provide-details-of-first-period-end")
                  )
                )
              )
            ).map(summarise)
          )
        )
      case None => None
    }
  }

  def createWhatYourRentIncludesRows(credId: String, userAnswers: Option[UserAnswers])(implicit messages: Messages): SummaryList = {
    val answers = userAnswers.getOrElse(UserAnswers(credId))
    val whatYourRentIncludesOpt = answers.get(WhatYourRentIncludesPage)
    val link = uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatYourRentIncludesController.show(CheckMode)
    val livingAccommodationRow = NGRSummaryListRow(
      messages("checkAnswers.whatYourRentIncludes.livingAccommodation"),
      None,
      Seq(whatYourRentIncludesOpt.map(_.livingAccommodation).map {
        case true => messages("service.yes")
        case false => messages("service.no")
      }.getOrElse(messages("service.notProvided"))),
      changeLink = Some(Link(
        href = link,
        linkId = "what-you-rent-includes",
        messageKey = "service.change",
        visuallyHiddenMessageKey = Some("what-you-rent-includes")
      ))
    )

    val bedroomNumbers: Seq[NGRSummaryListRow] = whatYourRentIncludesOpt.flatMap(_.bedroomNumbers).map { value =>
      NGRSummaryListRow(
        messages("checkAnswers.whatYourRentIncludes.bedroomNumbers"),
        None,
        Seq(value.toString),
        changeLink = Some(Link(
          href = link,
          linkId = "bedroom-numbers",
          messageKey = "service.change",
          visuallyHiddenMessageKey = Some("bedroom-numbers")
        ))
      )
    }.toSeq


    val rentPartAddressRow = Seq(
      NGRSummaryListRow(
        messages("checkAnswers.whatYourRentIncludes.rentPartAddress"),
        None,
        Seq(whatYourRentIncludesOpt.map(_.rentPartAddress).map {
          case true => messages("service.yes")
          case false => messages("service.no")
        }.getOrElse(messages("service.notProvided"))),
        changeLink = Some(Link(
          href = link,
          linkId = "rent-part-address",
          messageKey = "service.change",
          visuallyHiddenMessageKey = Some("rent-part-address")
        ))
      )
    )

    val rentEmptyShell = Seq(
      NGRSummaryListRow(
        messages("checkAnswers.whatYourRentIncludes.rentEmptyShell"),
        None,
        Seq(whatYourRentIncludesOpt.map(_.rentEmptyShell).map {
          case true => messages("service.yes")
          case false => messages("service.no")
        }.getOrElse(messages("service.notProvided"))),
        changeLink = Some(Link(
          href = link,
          linkId = "rent-empty-shell",
          messageKey = "service.change",
          visuallyHiddenMessageKey = Some("rent-empty-shell")
        ))
      )
    )

    val rentIncBusinessRates: Seq[NGRSummaryListRow] = whatYourRentIncludesOpt.flatMap(_.rentIncBusinessRates).map { value =>
      NGRSummaryListRow(
        messages("checkAnswers.whatYourRentIncludes.rentIncBusinessRates"),
        None,
        Seq(if (value) messages("service.yes") else messages("service.no")),
        changeLink = Some(Link(
          href = link,
          linkId = "rent-inc-business-rates",
          messageKey = "service.change",
          visuallyHiddenMessageKey = Some("rent-inc-business-rates")
        ))
      )
    }.toSeq

    val rentIncWaterCharges: Seq[NGRSummaryListRow] = whatYourRentIncludesOpt.flatMap(_.rentIncWaterCharges).map { value =>
      NGRSummaryListRow(
        messages("checkAnswers.whatYourRentIncludes.rentIncWaterCharges"),
        None,
        Seq(if (value) messages("service.yes") else messages("service.no")),
        changeLink = Some(Link(
          href = link,
          linkId = "rent-inc-water-charges",
          messageKey = "service.change",
          visuallyHiddenMessageKey = Some("rent-inc-water-charges")
        ))
      )
    }.toSeq

    val rentIncService: Seq[NGRSummaryListRow] = whatYourRentIncludesOpt.flatMap(_.rentIncService).map { value =>
      NGRSummaryListRow(
        messages("checkAnswers.whatYourRentIncludes.rentIncService"),
        None,
        Seq(if (value) messages("service.yes") else messages("service.no")),
        changeLink = Some(Link(
          href = link,
          linkId = "rent-in-service",
          messageKey = "service.change",
          visuallyHiddenMessageKey = Some("rent-in-service")
        ))
      )
    }.toSeq
    SummaryList(Seq(livingAccommodationRow).map(summarise)
      ++ bedroomNumbers.map(summarise)
      ++ rentPartAddressRow.map(summarise)
      ++ rentEmptyShell.map(summarise)
      ++ rentIncBusinessRates.map(summarise)
      ++ rentIncWaterCharges.map(summarise)
      ++ rentIncService.map(summarise))
  }

  //THIS IS NOT OPTIONAL
  def createRepairsAndInsurance(credId: String, userAnswers: Option[UserAnswers])(implicit messages: Messages): SummaryList = {
    val value = userAnswers.getOrElse(UserAnswers(credId)).get(RepairsAndInsurancePage)
    val link = uk.gov.hmrc.ngrraldfrontend.controllers.routes.RepairsAndInsuranceController.show(CheckMode)

    def answerConverter(answer: String): String = {
      if (answer == "YouAndLandlord") {
        messages("repairsAndInsurance.radio.youAndLandlord")
      } else answer
    }
    SummaryList( rows =
      Seq(
        NGRSummaryListRow(
          messages("checkAnswers.repairsAndInsurance.internalRepairs"),
          None,
          Seq(value.map(value => answerConverter(value.internalRepairs)).getOrElse(messages("service.notProvided"))),
          changeLink = Some(Link(
            href = link,
            linkId = "internal-repairs",
            messageKey = "service.change",
            visuallyHiddenMessageKey = Some("internal-repairs")
          ))
        ),
        NGRSummaryListRow(
          messages("checkAnswers.repairsAndInsurance.externalRepairs"),
          None,
          Seq(value.map(value => answerConverter(value.externalRepairs)).getOrElse(messages("service.notProvided"))),
          changeLink = Some(Link(
            href = link,
            linkId = "external-repairs",
            messageKey = "service.change",
            visuallyHiddenMessageKey = Some("external-repairs")
          ))
        ),
        NGRSummaryListRow(
          messages("checkAnswers.repairsAndInsurance.buildingInsurance"),
          None,
          Seq(value.map(value => answerConverter(value.buildingInsurance)).getOrElse(messages("service.notProvided"))),
          changeLink = Some(Link(
            href = link,
            linkId = "building-insurance",
            messageKey = "service.change",
            visuallyHiddenMessageKey = Some("building-insurance")
          ))
        )
      ).map(summarise)
    )
  }


  def createRentReviewRows(credId: String, userAnswers: Option[UserAnswers])(implicit messages: Messages): SummaryList = {
    val rentReviewPageAnswers = userAnswers.getOrElse(UserAnswers(credId)).get(RentReviewPage)
    val rentDetailsPageAnswers = userAnswers.getOrElse(UserAnswers(credId)).get(RentReviewDetailsPage)
    val rentReviewlink = uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentReviewController.show(CheckMode)
    val rentDetailslink = uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentReviewDetailsController.show(CheckMode)

    val annualAmount: Seq[NGRSummaryListRow] = Seq(
      NGRSummaryListRow(
        messages("checkAnswers.rentReviewDetails.annualAmount"),
        None,
        Seq(rentDetailsPageAnswers.map(value => s"£ ${value.annualRentAmount.toString()}").getOrElse(messages("service.notProvided"))),
        changeLink = Some(Link(
          href = rentDetailslink,
          linkId = "annual-amount",
          messageKey = "service.change",
          visuallyHiddenMessageKey = Some("annual-amount")
        ))
      )
    )

    val whatHappensAtRentReview: Seq[NGRSummaryListRow] = Seq(
      NGRSummaryListRow(
        messages("checkAnswers.rentReviewDetails.whatHappensAtRentReview"),
        None,
        Seq(rentDetailsPageAnswers.map(value => value.whatHappensAtRentReview match {case "OnlyGoUp" => messages("rentReviewDetails.whatHappensAtRentReview.radio2.text")case _ => messages("rentReviewDetails.whatHappensAtRentReview.radio1.text") }).getOrElse(messages("service.notProvided"))),
        changeLink = Some(Link(
          href = rentDetailslink,
          linkId = "what-happens-at-rent-review",
          messageKey = "service.change",
          visuallyHiddenMessageKey = Some("what-happens-at-rent-review")
        ))
      )
    )

    val startDate: Seq[NGRSummaryListRow] = Seq(
      NGRSummaryListRow(
        messages("checkAnswers.rentReviewDetails.startDate"),
        None,
        Seq(rentDetailsPageAnswers.map(_.startDate).getOrElse(messages("service.notProvided"))),
        changeLink = Some(Link(
          href = rentDetailslink,
          linkId = "start-date",
          messageKey = "service.change",
          visuallyHiddenMessageKey = Some("start-date")
        ))
      )
    )

    val hasAgreedNewRent: Seq[NGRSummaryListRow] = Seq(
      NGRSummaryListRow(
        messages("checkAnswers.rentReviewDetails.hasAgreedNewRent"),
        None,
        Seq(rentDetailsPageAnswers.map(value => if(value.hasAgreedNewRent) messages("service.yes") else messages("service.no")).getOrElse(messages("service.notProvided"))),
        changeLink = Some(Link(
          href = rentDetailslink,
          linkId = "has-agreed-new-rent",
          messageKey = "service.change",
          visuallyHiddenMessageKey = Some("has-agreed-new-rent")
        ))
      )
    )

    val whoAgreed: Seq[NGRSummaryListRow] = Seq(
      NGRSummaryListRow(
        messages("checkAnswers.rentReviewDetails.whoAgreed"),
        None,
        Seq(rentDetailsPageAnswers.map(_.whoAgreed.map{value => if (value == "Arbitrator") {messages("rentReviewDetails.whoAgreed.radio1.text")} else messages("rentReviewDetails.whoAgreed.radio2.text")}.getOrElse(messages("service.notProvided"))).getOrElse(messages("service.notProvided"))),
        changeLink = Some(Link(
          href = rentDetailslink,
          linkId = "who-agreed",
          messageKey = "service.change",
          visuallyHiddenMessageKey = Some("who-agreed")
        ))
      )
    )

    val hasIncludeRentReview: Seq[NGRSummaryListRow] = Seq(
      NGRSummaryListRow(
        messages("checkAnswers.rentReview.hasIncludeRentReview"),
        None,
        Seq(rentReviewPageAnswers.map(value => if (value.hasIncludeRentReview) "Yes" else "No").getOrElse(messages("service.notProvided"))),
        changeLink = Some(Link(
          href = rentReviewlink,
          linkId = "has-include-rent-review",
          messageKey = "service.change",
          visuallyHiddenMessageKey = Some("has-include-rent-review")
        ))
      )
    )

    val howOftenReviewed: Seq[NGRSummaryListRow] = rentReviewPageAnswers.map { value =>
      NGRSummaryListRow(
        messages("checkAnswers.whatYourRentIncludes.rentIncService"),
        None,
        Seq(s"${value.rentReviewYears.map(_.toString).getOrElse("")} years ${value.rentReviewMonths.map(_.toString).getOrElse("")} months"),
        changeLink = Some(Link(
          href = rentReviewlink,
          linkId = "how-often-reviewed",
          messageKey = messages("service.change"),
          visuallyHiddenMessageKey = Some("how-often-reviewed")
        ))
      )
    }.toSeq

    val canRentGoDown: Seq[NGRSummaryListRow] = rentReviewPageAnswers.map { value =>
      NGRSummaryListRow(
        messages("checkAnswers.whatYourRentIncludes.rentIncService"),
        None,
        value = Seq(if (value.canRentGoDown) messages("service.yes") else messages("service.no")),
        changeLink = Some(Link(
          href = rentReviewlink,
          linkId = "can-rent-go-down",
          messageKey = messages("service.change"),
          visuallyHiddenMessageKey = Some(messages("can-rent-go-down"))
        ))
      )
    }.toSeq

    (rentReviewPageAnswers, rentDetailsPageAnswers) match {
      case (Some(value), None) if !value.hasIncludeRentReview =>
        SummaryList( hasIncludeRentReview.map(summarise) ++ canRentGoDown.map(summarise))
      case (Some(_), None) =>
        SummaryList(hasIncludeRentReview.map(summarise) ++ howOftenReviewed.map(summarise) ++ canRentGoDown.map(summarise))
      case (None, Some(value)) if value.whoAgreed.nonEmpty => SummaryList(annualAmount.map(summarise) ++ whatHappensAtRentReview.map(summarise) ++ startDate.map(summarise) ++ hasAgreedNewRent.map(summarise) ++ whoAgreed.map(summarise))
      case (None, Some(_))  => SummaryList(annualAmount.map(summarise) ++ whatHappensAtRentReview.map(summarise) ++ startDate.map(summarise) ++ hasAgreedNewRent.map(summarise))
      case (None, None) => SummaryList(Seq.empty)
    }
  }

  def createPaymentRows(credId: String, userAnswers: Option[UserAnswers])(implicit messages: Messages): Option[SummaryList] = {
    val didYouGetMoneyFromLandlord = userAnswers.getOrElse(UserAnswers(credId)).get(DidYouGetMoneyFromLandlordPage)
    val didYouPayAnyMoneyToLandlord = userAnswers.getOrElse(UserAnswers(credId)).get(DidYouPayAnyMoneyToLandlordPage)

    def didYouGetMoneyFromLandlordSummary(value: Boolean): Seq[NGRSummaryListRow] = {
      Seq(
        NGRSummaryListRow(
          messages("checkAnswers.payments.didYouGetMoneyFromLandlord"),
          None,
          Seq(if (value) "Yes" else messages("didYouAgreeRentWithLandlord.no")),
          changeLink = Some(Link(
            href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouAgreeRentWithLandlordController.show(CheckMode),
            linkId = "building-insurance",
            messageKey = "service.change",
            visuallyHiddenMessageKey = Some("building-insurance")
          ))
        )
      )
    }

    def didYouPayAnyMoneyToLandlordSummary(value: Boolean): Seq[NGRSummaryListRow] = {
      Seq(
        NGRSummaryListRow(
          messages("checkAnswers.payments.didYouPayAnyMoneyToLandlord"),
          None,
          Seq(if (value) "Yes" else "No"),
          changeLink = Some(Link(
            href = uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouPayAnyMoneyToLandlordController.show(CheckMode),
            linkId = "building-insurance",
            messageKey = "service.change",
            visuallyHiddenMessageKey = Some("building-insurance")
          ))
        )
      )
    }

    (didYouGetMoneyFromLandlord, didYouPayAnyMoneyToLandlord) match {
      case (Some(didYouGetMoneyFromLandlord), Some(didYouPayAnyMoneyToLandlord)) =>
        Some(
          SummaryList(
            rows =
              didYouGetMoneyFromLandlordSummary(didYouGetMoneyFromLandlord).map(summarise) ++
                didYouPayAnyMoneyToLandlordSummary(didYouPayAnyMoneyToLandlord).map(summarise)
          )
        )
      case (Some(didYouGetMoneyFromLandlord), None) =>
        Some(
          SummaryList(
            rows =
              didYouGetMoneyFromLandlordSummary(didYouGetMoneyFromLandlord).map(summarise)
          )
        )
      case (None, Some(didYouPayAnyMoneyToLandlord)) =>
        Some(
          SummaryList(
            rows =
              didYouPayAnyMoneyToLandlordSummary(didYouPayAnyMoneyToLandlord).map(summarise)
          )
        )
      case (None, None) => None
    }
  }
}
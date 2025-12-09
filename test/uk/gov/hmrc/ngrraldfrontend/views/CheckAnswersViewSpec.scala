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

package uk.gov.hmrc.ngrraldfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.json.Json
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.UserAnswers
import uk.gov.hmrc.ngrraldfrontend.services.CheckAnswers.*
import uk.gov.hmrc.ngrraldfrontend.views.html.CheckAnswersView

class CheckAnswersViewSpec extends ViewBaseSpec {


  lazy val view: CheckAnswersView = inject[CheckAnswersView]
  val yes = "Yes"
  val no = "No"

  val heading = "Check your answers"
  val title = s"$heading - GOV.UK"

  val landlordLabel = "Landlord details"
  val landlordFullName = "Landlord's full name"
  val landlordName = "Jake"

  val landlordRelationship = "Do you have a relationship with the landlord other than as a tenant?"
  val landlordRelationshipReason = "Relationship with the landlord"

  val agreementDetailsLabel = "Agreement details"
  val whatTypeOfAgreement = "What type of agreement do you have?"
  val agreementStartDate = "Agreement start date"
  val agreementIsOpenEnded = "Is your agreement open-ended?"
  val agreementEndDate = "Agreement end date"
  val agreementBreakClause = "Does your agreement have a break clause?"
  val agreementBreakClauseDetails = "Break clause details"

  val rentLabel = "Rent details"
  val whatIsYourRentBasedOn = "What is your rent based on?"
  val otherReason = "Can you tell us how your rent was agreed?"
  val agreedRentChange = "Do you have a stepped rent?"
  val didYouAgreeRentWithLandlord = "Did you agree the rent with your landlord or their agent?"
  val whenDidYouAgree = "When did you agree your rent?"
  val totalAnnualRent = "Total annual rent"
  val rentCheckRentPeriod = "Do you have a rent-free period at the start of your agreement?"
  val rentFreePeriod = "How many months is your rent-free period?"
  val rentFreePeriodReason = "Why do you have a rent-free period?"
  val startPayingDate = "When will you start paying rent?"

  val whatYourRentIncludesLabel = "What your rent includes details"
  val livingAccommodation = "Does your rent include any living accommodation?"
  val bedroomNumbers = "How many bedrooms does the living accommodation have?"
  val rentPartAddress = "Is the rent you pay for all of this property?"
  val rentEmptyShell = "Is your rent for an empty shell building?"
  val checkRentPeriod = "Do you have a rent-free period at the start of your agreement?"
  val rentIncBusinessRates = "Does your rent include business rates?"
  val rentIncWaterCharges = "Does your rent include water charges?"
  val rentIncService = "Does your rent include service charges?"
  val doesYourRentIncludeParking = "Does your rent include parking spaces or garages?"
  val howManyUncoveredSpacesIncludedInRent = "Uncovered spaces included in you rent"
  val howManyCoveredSpacesIncludedInRent = "Covered spaces included in you rent"
  val howManyGaragesIncludedInRent = "Garages included in you rent"
  val howManyUncoveredSpacesNotIncludedInRent = "Uncovered spaces not included in your rent"
  val howManyCoveredSpacesNotIncludedInRent = "Covered spaces not included in your rent"
  val howManyGaragesNotIncludedInRent = "Garages not included in your rent"
  val doYouPayExtraForParkingSpaces = "Do you pay extra for parking spaces or garages that are not included in your rent?"
  val parkingSpacesOrGaragesNotIncludedInYourRent = "How many parking spaces or garages do you pay extra for?"
  val totalCost = "How much extra do you pay for parking and garages (excluding VAT)?"
  val agreementDate = "When was this payment agreed for parking and garages?"

  val repairsAndInsuranceLabel = "Repairs and insurance details"
  val internalRepairs = "Who pays for internal repairs?"
  val externalRepairs = "Who pays for external repairs?"
  val buildingInsurance = "Who pays for building insurance repairs?"

  val rentReviewLabel = "Rent review details"
  val hasIncludeRentReview = "Does your agreement include a rent review?"
  val howOftenReviewed = "How often is your rent reviewed?"
  val canRentGoDown = "Can the rent go down when it is reviewed?"

  val repairsAndFittingOutLabel = "Repairs and fitting out"
  val repairsAndFittingOut = "Have you done any repairs or fitting out in the property?"
  val repairsAndFittingOutDate = "When did you do the repairs or fitting out?"
  val repairsAndFittingOutCost = "How much did the repairs or fitting out cost (excluding VAT)?"

  val paymentsLabel = "Payments"
  val didYouGetMoneyFromLandlord = "Did you get any money from the landlord or previous tenant to take on the lease?"
  val didYouPayAnyMoneyToLandlord = "Did you pay any money in advance to the landlord?"
  val moneyYouPaidInAdvanceToLandlordAmount = "How much money did you pay in advance to the landlord (excluding VAT)?"
  val moneyYouPaidInAdvanceToLandlordDate = "When did you pay the money?"

  val otherDetailsLabel = "Other details"
  val hasAnyAffectedRent = "Has anything else affected the rent?"
  val otherDetailsReason = "Can you tell us what else has affected the rent?"


  val saveButton = "Continue"

  object Selectors {
    val navTitle = "head > title"
    val heading = "#main-content > div > div.govuk-grid-column-two-thirds > form > h1"
    val landlordLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > h2:nth-child(2)"
    val leaseRenewalDetailsLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > h2:nth-child(2)"
    val whatLeaseRenewal = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(3) > div:nth-child(1) > dt"
    val landlordFullName = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(3) > div:nth-child(1) > dt"
    val landlordRelationship = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(3) > div:nth-child(2) > dt"
    val landlordRelationshipReason = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(3) > div:nth-child(3) > dt"

    val agreementDetailsLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > h2:nth-child(4)"
    val whatTypeOfAgreement = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(5) > div:nth-child(1) > dt"
    val agreementStartDate = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(5) > div:nth-child(2) > dt"
    val agreementIsOpenEnded = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(5) > div:nth-child(3) > dt"
    val agreementBreakClause = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(5) > div:nth-child(4) > dt"
    val agreementBreakClauseDetails = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(5) > div:nth-child(5) > dt"

    val rentLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > h2:nth-child(6)"
    val whatIsYourRentBasedOn = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(7) > div:nth-child(1) > dt"
    val otherReason = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(7) > div:nth-child(2) > dt"
    val agreedRentChange = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(7) > div:nth-child(3) > dt"
    val whenDidYouAgree = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(7) > div:nth-child(8) > dt"
    val totalAnnualRent = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(7) > div:nth-child(4) > dt"
    val rentCheckRentPeriod = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(7) > div:nth-child(5) > dt"
    val rentFreePeriod = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(7) > div:nth-child(6) > dt"
    val rentFreePeriodReason = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(7) > div:nth-child(7) > dt"
    val startPayingDate = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(7) > div:nth-child(9) > dt"

    val whatYourRentIncludesLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > h2:nth-child(8)"
    val livingAccommodation = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(9) > div:nth-child(1) > dt"
    val bedroomNumbers = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(9) > div:nth-child(2) > dt"
    val rentPartAddress = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(9) > div:nth-child(3) > dt"
    val rentEmptyShell = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(9) > div:nth-child(4) > dt"

    val rentIncBusinessRates = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(9) > div:nth-child(5) > dt"
    val rentIncWaterCharges = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(9) > div:nth-child(6) > dt"
    val rentIncService = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(9) > div:nth-child(7) > dt"
    val doesYourRentIncludeParking = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(9) > div:nth-child(8) > dt"
    val howManyUncoveredSpacesIncludedInRent = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(9) > div:nth-child(9) > dt"
    val howManyCoveredSpacesIncludedInRent = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(9) > div:nth-child(10) > dt"
    val howManyGaragesIncludedInRent = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(9) > div:nth-child(11) > dt"
    val doYouPayExtraForParkingSpaces = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(9) > div:nth-child(12) > dt"
    val howManyUncoveredSpacesNotIncludedInRent = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(9) > div:nth-child(13) > dt"
    val howManyCoveredSpacesNotIncludedInRent = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(9) > div:nth-child(14) > dt"
    val howManyGaragesNotIncludedInRent = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(9) > div:nth-child(15) > dt"
    val parkingSpacesOrGaragesNotIncludedInYourRent = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(9) > div:nth-child(16) > dt"
    val totalCost = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(9) > div:nth-child(16) > dt"
    val agreementDate = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(9) > div:nth-child(17) > dt"

    val repairsAndInsuranceLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > h2:nth-child(10)"
    val internalRepairs = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(11) > div:nth-child(1) > dt"
    val externalRepairs = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(11) > div:nth-child(2) > dt"
    val buildingInsurance = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(11) > div:nth-child(3) > dt"

    val rentReviewLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > h2:nth-child(12)"
    val hasIncludeRentReview = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(13) > div:nth-child(1) > dt"
    val howOftenReviewed = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(13) > div:nth-child(2) > dt"
    val canRentGoDown = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(13) > div:nth-child(3) > dt"

    val repairsAndFittingOutLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > h2:nth-child(14)"
    val repairsAndFittingOut = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(15) > div:nth-child(1) > dt"
    val repairsAndFittingOutDate = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(15) > div:nth-child(2) > dt"
    val repairsAndFittingOutCost = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(15) > div:nth-child(3) > dt"

    val paymentsLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > h2:nth-child(16)"
    val didYouGetMoneyFromLandlord = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(17) > div:nth-child(1) > dt"
    val didYouPayAnyMoneyToLandlord = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(17) > div:nth-child(2) > dt"
    val moneyYouPaidInAdvanceToLandlordAmount = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(15) > div:nth-child(3) > dt"
    val moneyYouPaidInAdvanceToLandlordDate = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(17) > div:nth-child(4) > dt"

    val otherDetailsLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > h2:nth-child(18)"
    val hasAnyAffectedRent = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(19) > div:nth-child(1) > dt"
    val otherDetailsReason = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(19) > div:nth-child(2) > dt"

    val landlordFullNameAnswer = "#checkanswers\\.landlord\\.fullname-id"
    val landlordFullNameChangeButton = "#landlord-full-name"
    val landlordRelationshipAnswer = "#checkanswers\\.landlord\\.relationship-id"
    val landlordRelationshipChangeButton = "#landlord-relationship"
    val landlordRelationshipReasonAnswer = "#checkanswers\\.landlord\\.relationship\\.reason-id"
    val landlordRelationshipReasonChangeButton = "#landlord-relationship-reason"
    val whatTypeOfAgreementAnswer = "#checkanswers\\.agreement\\.whattypeofagreement-id"
    val whatTypeOfAgreementChangeButton = "#what-type-of-agreement"
    val agreementStartDateAnswer = "#checkanswers\\.agreement\\.startdate-id"
    val agreementStartDateChangeButton = "#agreement-start-date"
    val agreementIsOpenEndedAnswer = "#checkanswers\\.agreement\\.isopenended-id"
    val agreementIsOpenChangeButton = "#is-open-ended"
    val agreementBreakClauseAnswer = "#checkanswers\\.agreement\\.breakclause-id"
    val agreementBreakClauseChangeButton = "#break-clause"
    val agreementBreakClauseDetailsAnswer = "#checkanswers\\.agreement\\.breakclausedetails-id"
    val agreementBreakClauseDetailsChangeButton = "#break-clause-details"
    val whatIsYourRentBasedOnAnswer = "#checkanswers\\.rent\\.whatisyourrentbasedon-id"
    val whatIsYourRentBasedOnChangeButton = "#what-is-your-rent-based-on"
    val otherReasonAnswer = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(7) > div:nth-child(2) > dd.govuk-summary-list__value"
    val otherReasonChangeButton = "#agreed-rent-change"
    val agreedRentChangeAnswer = "#checkanswers\\.rent\\.agreedrentchange-id"
    val agreedRentChangeChangeButton = "#agreed-rent-change"
    val whenDidYouAgreeAnswer = "#checkanswers\\.rents\\.whendidyouagree-id"
    val whenDidYouAgreeChangeButton = "#when-did-you-agree"
    val totalAnnualRentAnswer = "#checkanswers\\.rent\\.totalannualrent-id"
    val totalAnnualRentChangeButton = "#how-much-is-total-annual-rent"
    val rentCheckRentPeriodAnswer = "#checkanswers\\.rent\\.checkrentperiod-id"
    val rentCheckRentPeriodChangeButton = "#check-rent-free-period"
    val rentFreePeriodAnswer = "#checkanswers\\.rents\\.rentfreeperiod-id"
    val rentFreePeriodChangeButton = "#rent-free-period-months"
    val rentFreePeriodReasonAnswer = "#checkanswers\\.rents\\.rentfreeperiodreason-id"
    val rentFreePeriodReasonChangeButton = "#rent-free-period-reason"
    val startPayingDateAnswer = "#checkanswers\\.rents\\.startpayingdate-id"
    val startPayingDateChangeButton = "#start-paying-date"
    val livingAccommodationAnswer = "#checkanswers\\.whatyourrentincludes\\.livingaccommodation-id"
    val livingAccommodationChangeButton = "#living-accommodation"
    val bedroomNumbersAnswer = "#checkanswers\\.whatyourrentincludes\\.bedroomnumbers-id"
    val bedroomNumbersChangeButton = "#bedroom-numbers"
    val rentPartAddressAnswer = "#checkanswers\\.whatyourrentincludes\\.rentpartaddress-id"
    val rentPartAddressChangeButton = "#rent-part-address"
    val rentEmptyShellAnswer = "#checkanswers\\.whatyourrentincludes\\.rentemptyshell-id"
    val rentEmptyShellChangeButton = "#rent-empty-shell"
    val rentIncBusinessRatesAnswer = "#checkanswers\\.whatyourrentincludes\\.rentincbusinessrates-id"
    val rentIncBusinessRatesChangeButton = "#rent-inc-business-rates"
    val rentIncWaterChargesAnswer = "#checkanswers\\.whatyourrentincludes\\.rentincwatercharges-id"
    val rentIncWaterChargesChangeButton = "#rent-inc-water-charges"
    val rentIncServiceAnswer = "#checkanswers\\.whatyourrentincludes\\.rentincservice-id"
    val rentIncServiceChangeButton = "#rent-inc-service"
    val doesYourRentIncludeParkingAnswer = "#checkanswers\\.whatyourrentincludes\\.doesyourrentincludeparking-id"
    val doesYourRentIncludeParkingChangeButton = "#rent-inc-parking"
    val howManyUncoveredSpacesIncludedInRentAnswer = "#checkanswers\\.whatyourrentincludes\\.howmanyuncoveredspacesincludedinrent-id"
    val howManyUncoveredSpacesIncludedInRentChangeButton = "#how-many-uncovered-spaces-included-in-rent"
    val howManyCoveredSpacesIncludedInRentAnswer = "#checkanswers\\.whatyourrentincludes\\.howmanycoveredspacesincludedinrent-id"
    val howManyCoveredSpacesIncludedInRentChangeButton = "#how-many-covered-spaces-included-in-rent"
    val Answer = "#checkanswers\\.whatyourrentincludes\\.howmanygaragesincludedinrent-id"
    val ChangeButton = "#landlord-relationship-reason"
    val howManyGaragesIncludedInRentAnswer = "#checkanswers\\.whatyourrentincludes\\.howmanygaragesincludedinrent-id"
    val howManyGaragesIncludedInRentChangeButton = "#how-many-covered-spaces-included-in-rent"
    val doYouPayExtraForParkingSpacesAnswer = "#checkanswers\\.whatyourrentincludes\\.doyoupayextraforparkingspaces-id"
    val doYouPayExtraForParkingSpacesButton = "#do-you-pay-extra-for-parking-spaces"
    val howManyUncoveredSpacesNotIncludedInRentAnswer = "#checkanswers\\.whatyourrentincludes\\.howmanyuncoveredspacesnotincludedinrent-id"
    val howManyUncoveredSpacesNotIncludedInRentChangeButton = "#how-many-uncovered-spaces-not-included-in-rent"
    val howManyCoveredSpacesNotIncludedInRentAnswer = "#checkanswers\\.whatyourrentincludes\\.howmanycoveredspacesnotincludedinrent-id"
    val howManyCoveredSpacesNotIncludedInRentChangeButton = "#how-many-covered-spaces-not-included-in-rent"
    val howManyGaragesNotIncludedInRentAnswer = "#checkanswers\\.whatyourrentincludes\\.howmanygaragesnotincludedinrent-id"
    val howManyGaragesNotIncludedInRentChangeButton = "#how-many-garages-not-included-in-rent"
    val totalCostAnswer = "#checkanswers\\.whatyourrentincludes\\.parkingspacesorgaragesnotincludedinyourrent\\.totalcost-id"
    val totalCostChangeButton = "#parking-spaces-or-garages-not-included-in-your-rent-value"
    val agreementDateAnswer = "#checkanswers\\.whatyourrentincludes\\.parkingspacesorgaragesnotincludedinyourrent\\.agreementdate-id"
    val agreementDateChangeButton = "#parking-spaces-or-garages-not-included-in-your-rent-value"
    val internalRepairsAnswer = "#checkanswers\\.repairsandinsurance\\.internalrepairs-id"
    val internalRepairsChangeButton = "#internal-repairs"
    val externalRepairsAnswer = "#checkanswers\\.repairsandinsurance\\.externalrepairs-id"
    val externalRepairsChangeButton = "#external-repairs"
    val buildingInsuranceAnswer = "#checkanswers\\.repairsandinsurance\\.buildinginsurance-id"
    val buildingInsuranceChangeButton = "#building-insurance"
    val repairsAndFittingOutAnswer = "#checkanswers\\.repairsandfittingout\\.repairsandfittingout-id"
    val repairsAndFittingOutChangeButton = "#repairs-and-fitting-out"
    val repairsAndFittingOutDateAnswer = "#checkanswers\\.repairsandfittingout\\.date-id"
    val repairsAndFittingOutDateChangeButton = "#repairs-and-fitting-out-date"
    val repairsAndFittingOutCostAnswer = "#checkanswers\\.repairsandfittingout\\.cost-id"
    val repairsAndFittingOutCostChangeButton = "#repairs-and-fitting-out-cost"
    val didYouGetMoneyFromLandlordAnswer = "#checkanswers\\.payments\\.didyougetmoneyfromlandlord-id"
    val didYouGetMoneyFromLandlordChangeButton = "#did-you-get-money-from-landlord"
    val didYouPayAnyMoneyToLandlordAnswer = "#checkanswers\\.payments\\.didyoupayanymoneytolandlord-id"
    val didYouPayAnyMoneyToLandlordChangeButton = "#did-you-pay-money-to-landlord"
    val moneyYouPaidInAdvanceToLandlordAmountAnswer = "#checkanswers\\.payments\\.moneyyoupaidinadvancetolandlord\\.amount-id"
    val moneyYouPaidInAdvanceToLandlordAmountChangeButton = "#money-you-paid-in-advance-to-landlord-amount"
    val moneyYouPaidInAdvanceToLandlordDateAnswer = "#checkanswers\\.payments\\.moneyyoupaidinadvancetolandlord\\.date-id"
    val moneyYouPaidInAdvanceToLandlordDateChangeButton = "#money-you-paid-in-advance-to-landlord-amount"
    val hasAnyAffectedRentAnswer = "#checkanswers\\.otherdetails\\.hasanyaffectedrent-id"
    val hasAnyAffectedRentChangeButton = "#other-details"
    val otherDetailsReasonAnswer = "#checkanswers\\.otherdetails\\.reason-id"
    val otherDetailsReasonChangeButton = "#other-details-reason"

    val firstRentPeriodLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > h2:nth-child(10)"
    val firstPeriodStartDate = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(11) > div:nth-child(1) > dt"
    val firstPeriodEndDate = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(11) > div:nth-child(2) > dt"
    val firstPeriodDoYouPay = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(11) > div:nth-child(3) > dt"
    val firstPeriodPayAmount = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(11) > div:nth-child(4) > dt"

    val secondRentPeriodLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > h2:nth-child(12)"
    val secondPeriodEndDate = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(13) > div:nth-child(1) > dt"
    val secondPeriodAmount = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(13) > div:nth-child(2) > dt"

    val thirdRentPeriodLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > h2:nth-child(14)"
    val thirdPeriodEndDate = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(15) > div:nth-child(1) > dt"
    val thirdPeriodAmount = "#main-content > div > div.govuk-grid-column-two-thirds > form > dl:nth-child(15) > div:nth-child(2) > dt"
    val saveButton = "#continue"
  }

  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"

  val fullNewAgreementUserAnswers: UserAnswers = UserAnswers(
    testCredId,
    data = Json.obj(
      "tellUsAboutYourNewAgreement" -> "NewAgreement",
      "landlord" -> Json.obj(
        "landlordName" -> "Jake",
        "hasRelationship" -> true,
        "landlordRelationship" -> "Parent"
      ),
      "whatTypeOfAgreement" -> "LeaseOrTenancy",
      "agreement" -> Json.obj(
        "agreementStart" -> "2020-12-12",
        "isOpenEnded" -> true,
        "haveBreakClause" -> true,
        "breakClauseInfo" -> "break clause reason"
      ),
      "whatIsYourRentBasedOn" -> Json.obj(
        "rentBased" -> "Other",
        "otherDesc" -> "Other reason"
      ),
      "agreedRentChange" -> false,
      "howMuchIsTotalAnnualRentPage" -> 12.0,
      "checkRentFreePeriod" -> true,
      "rentFreePeriod" -> Json.obj(
        "months" -> 1,
        "reasons" -> "rent-free period reason"
      ),
      "rentDatesAgreeStart" -> Json.obj(
        "agreedDate" -> "2020-12-12",
        "startPayingDate" -> "2020-12-13"
      ),
      "whatYourRentIncludes" -> Json.obj(
        "livingAccommodation" -> true,
        "rentPartAddress" -> false,
        "rentEmptyShell" -> true,
        "rentIncBusinessRates" -> false,
        "rentIncWaterCharges" -> true,
        "rentIncService" -> false,
        "bedroomNumbers" -> 1
      ),
      "doesYourRentIncludeParking" -> true,
      "howManyParkingSpacesOrGaragesIncludedInRent" -> Json.obj(
        "uncoveredSpaces" -> 1,
        "coveredSpaces" -> 2,
        "garages" -> 3
      ),
      "doYouPayExtraForParkingSpacesNotIncludedInRent" -> true,
      "parkingSpacesOrGaragesNotIncludedInYourRent" -> Json.obj(
        "uncoveredSpaces" -> 1,
        "coveredSpaces" -> 2,
        "garages" -> 3,
        "totalCost" -> 12.0,
        "agreementDate" -> "2020-12-12"
      ),
      "repairsAndInsurance" -> Json.obj(
        "internalRepairs" -> "You",
        "externalRepairs" -> "Landlord",
        "buildingInsurance" -> "YouAndLandlord"
      ),
      "rentReview" -> Json.obj(
        "hasIncludeRentReview" -> true,
        "rentReviewMonths" -> 2,
        "rentReviewYears" -> 1,
        "canRentGoDown" -> true
      ),
      "repairsAndFittingOutPage" -> true,
      "aboutRepairsAndFittingOutPage" -> Json.obj(
        "cost" -> 12.0,
        "date" -> "2020-01"
      ),
      "didYouGetMoneyFromLandlord" -> false,
      "didYouPayAnyMoneyToLandlord" -> true,
      "moneyYouPaidInAdvanceToLandlord" -> Json.obj(
        "amount" -> 12.0,
        "date" -> "2020-12-12"
      ),
      "hasAnythingElseAffectedTheRent" -> Json.obj(
        "radio" -> true,
        "reason" -> "something else"
      )
    )
  )


  "CheckAnswersView new agreement" must {
    val checkAnswersView = view(
      selectedPropertyAddress = address,
      leaseRenewalsSummary = createLeaseRenewalsSummaryRows(fullNewAgreementUserAnswers),
      landlordSummary = createLandlordSummaryRows(fullNewAgreementUserAnswers),
      agreementDetailsSummary = createAgreementDetailsRows(fullNewAgreementUserAnswers),
      rentSummary = createRentRows(fullNewAgreementUserAnswers),
      firstRentPeriod = createFirstRentPeriodRow(fullNewAgreementUserAnswers),
      rentPeriods = createRentPeriodsSummaryLists(fullNewAgreementUserAnswers),
      whatYourRentIncludesSummary = createWhatYourRentIncludesRows(fullNewAgreementUserAnswers),
      repairsAndInsurance = createRepairsAndInsurance(fullNewAgreementUserAnswers),
      rentReview = createRentReviewRows(fullNewAgreementUserAnswers),
      repairsAndFittingOutSummary = createRepairsAndFittingOut(fullNewAgreementUserAnswers),
      payments = createPaymentRows(fullNewAgreementUserAnswers),
      breakClause = createBreakClauseRows(fullNewAgreementUserAnswers),
      otherDetailsSummary = createOtherDetailsRow(fullNewAgreementUserAnswers),
      isRentReviewed = false
    )

    lazy implicit val document: Document = Jsoup.parse(checkAnswersView.body)
    val htmlApply = view.apply(
      address,
      leaseRenewalsSummary = createLeaseRenewalsSummaryRows(fullNewAgreementUserAnswers),
      landlordSummary = createLandlordSummaryRows(fullNewAgreementUserAnswers),
      agreementDetailsSummary = createAgreementDetailsRows(fullNewAgreementUserAnswers),
      rentSummary = createRentRows(fullNewAgreementUserAnswers),
      firstRentPeriod = createFirstRentPeriodRow(fullNewAgreementUserAnswers),
      rentPeriods = createRentPeriodsSummaryLists(fullNewAgreementUserAnswers),
      whatYourRentIncludesSummary = createWhatYourRentIncludesRows(fullNewAgreementUserAnswers),
      repairsAndInsurance = createRepairsAndInsurance(fullNewAgreementUserAnswers),
      rentReview = createRentReviewRows(fullNewAgreementUserAnswers),
      repairsAndFittingOutSummary = createRepairsAndFittingOut(fullNewAgreementUserAnswers),
      payments = createPaymentRows(fullNewAgreementUserAnswers),
      breakClause = createBreakClauseRows(fullNewAgreementUserAnswers),
      otherDetailsSummary = createOtherDetailsRow(fullNewAgreementUserAnswers),
      isRentReviewed = false
    ).body

    val htmlRender = view.render(
      address,
      leaseRenewalsSummary = createLeaseRenewalsSummaryRows(fullNewAgreementUserAnswers),
      landlordSummary = createLandlordSummaryRows(fullNewAgreementUserAnswers),
      agreementDetailsSummary = createAgreementDetailsRows(fullNewAgreementUserAnswers),
      rentSummary = createRentRows(fullNewAgreementUserAnswers),
      firstRentPeriod = createFirstRentPeriodRow(fullNewAgreementUserAnswers),
      rentPeriods = createRentPeriodsSummaryLists(fullNewAgreementUserAnswers),
      whatYourRentIncludesSummary = createWhatYourRentIncludesRows(fullNewAgreementUserAnswers),
      repairsAndInsurance = createRepairsAndInsurance(fullNewAgreementUserAnswers),
      rentReview = createRentReviewRows(fullNewAgreementUserAnswers),
      repairsAndFittingOutSummary = createRepairsAndFittingOut(fullNewAgreementUserAnswers),
      payments = createPaymentRows(fullNewAgreementUserAnswers),
      breakClause = createBreakClauseRows(fullNewAgreementUserAnswers),
      otherDetailsSummary = createOtherDetailsRow(fullNewAgreementUserAnswers),
      isRentReviewed = false, request, messages, mockConfig).body

    "apply must nit be the same as render" in {
      htmlApply mustBe htmlRender
    }

    "render is not empty" in {
      htmlRender must not be empty
    }

    "show the correct title" in {
      elementText(Selectors.navTitle) mustBe title
    }

    "show the correct heading" in {
      elementText(Selectors.heading) mustBe heading
    }

    //Landlord table
    "show the correct label for landlord table" in {
      elementText(Selectors.landlordLabel) mustBe landlordLabel
    }
    //full name
    "show the correct message for land lords full name in the landlord table" in {
      elementText(Selectors.landlordFullName) mustBe landlordFullName
    }
    "show the correct answer for land lords full name in the landlord table" in {
      elementText(Selectors.landlordFullNameAnswer) mustBe landlordName
    }
    "show the correct change link for land lords full name in the landlord table" in {
      elementText(Selectors.landlordFullNameChangeButton) mustBe "Change landlord-full-name"
    }
    //relationship
    "show the correct message for landlord relationship in the landlord table" in {
      elementText(Selectors.landlordRelationship) mustBe landlordRelationship
    }
    "show the correct answer for land relationship in the landlord table" in {
      elementText(Selectors.landlordRelationshipAnswer) mustBe yes
    }
    "show the correct change link for land lords relationship in the landlord table" in {
      elementText(Selectors.landlordRelationshipChangeButton) mustBe "Change landlord-relationship"
    }
    //landlordRelationshipReason
    "show the correct message for landlord relationship reason in the landlord table" in {
      elementText(Selectors.landlordRelationshipReason) mustBe landlordRelationshipReason
    }
    "show the correct answer for land relationship reason in the landlord table" in {
      elementText(Selectors.landlordRelationshipReasonAnswer) mustBe "Parent"
    }
    "show the correct change link for land lords relationship reason in the landlord table" in {
      elementText(Selectors.landlordRelationshipReasonChangeButton) mustBe "Change landlord-relationship-reason"
    }

    //Agreement details table
    "show the correct label for agreement details table" in {
      elementText(Selectors.agreementDetailsLabel) mustBe agreementDetailsLabel
    }
    //Agreement what type of agreement
    "show the correct message for what type of agreement in the agreement details table" in {
      elementText(Selectors.whatTypeOfAgreement) mustBe whatTypeOfAgreement
    }
    "show the correct answer for what type of agreement in the agreement details table" in {
      elementText(Selectors.whatTypeOfAgreementAnswer) mustBe "Lease or tenancy agreement"
    }
    "show the correct change link for what type of agreement in the agreement details table" in {
      elementText(Selectors.whatTypeOfAgreementChangeButton) mustBe "Change what-type-of-agreement"
    }
    //Agreement start date
    "show the correct message for agreement start date in the agreement details table" in {
      elementText(Selectors.agreementStartDate) mustBe agreementStartDate
    }
    "show the correct answer for agreement start date in the agreement details table" in {
      elementText(Selectors.agreementStartDateAnswer) mustBe "12 December 2020"
    }
    "show the correct change link for agreement start date in the agreement details table" in {
      elementText(Selectors.agreementStartDateChangeButton) mustBe "Change agreement-start-date"
    }
    //Agreement start date
    "show the correct message for agreement is open ended in the agreement details table" in {
      elementText(Selectors.agreementIsOpenEnded) mustBe agreementIsOpenEnded
    }
    "show the correct answer for agreement is open ended in the agreement details table" in {
      elementText(Selectors.agreementIsOpenEndedAnswer) mustBe "Yes, it is open-ended"
    }
    "show the correct change link for agreement is open ended in the agreement details table" in {
      elementText(Selectors.agreementIsOpenChangeButton) mustBe "Change is-open-ended"
    }
    //Agreement Break Clause
    "show the correct message for agreement break clause in the agreement details table" in {
      elementText(Selectors.agreementBreakClause) mustBe agreementBreakClause
    }
    "show the correct answer for agreement break clause in the agreement details table" in {
      elementText(Selectors.agreementBreakClauseAnswer) mustBe yes
    }
    "show the correct change link for agreement break clause in the agreement details table" in {
      elementText(Selectors.agreementBreakClauseChangeButton) mustBe "Change break-clause"
    }
    //Agreement Break clause details
    "show the correct message for agreement break clause details in the agreement details table" in {
      elementText(Selectors.agreementBreakClauseDetails) mustBe agreementBreakClauseDetails
    }
    "show the correct answer for agreement break clause details in the agreement details table" in {
      elementText(Selectors.agreementBreakClauseDetailsAnswer) mustBe "break clause reason"
    }
    "show the correct change link for agreement break clause details in the agreement details table" in {
      elementText(Selectors.agreementBreakClauseDetailsChangeButton) mustBe "Change break-clause-details"
    }

    //Rent details
    "show the correct label for rent details table" in {
      elementText(Selectors.rentLabel) mustBe rentLabel
    }
    //whatIsYourRentBasedOn
    "show the correct message for what is your rent based on in the rent details table" in {
      elementText(Selectors.whatIsYourRentBasedOn) mustBe whatIsYourRentBasedOn
    }
    "show the correct answer for agreement break clause details in the rent details table" in {
      elementText(Selectors.whatIsYourRentBasedOnAnswer) mustBe "Other"
    }
    "show the correct change link for agreement break clause details in the rent details table" in {
      elementText(Selectors.whatIsYourRentBasedOnChangeButton) mustBe "Change what-is-your-rent-based-on"
    }
    //otherReason
    "show the correct message for other reason on in the rent details table" in {
      elementText(Selectors.otherReason) mustBe otherReason
    }
    "show the correct answer for other reason  in the rent details table" in {
      elementText(Selectors.otherReasonAnswer) mustBe "Other reason"
    }
    "show the correct change link for other reason  in the rent details table" in {
      elementText(Selectors.otherReasonChangeButton) mustBe "Change agreed-rent-change"
    }
    //agreedRentChange
    "show the correct message for agreed rent change in the rent details table" in {
      elementText(Selectors.agreedRentChange) mustBe agreedRentChange
    }
    "show the correct answer for agreed rent change in the rent details table" in {
      elementText(Selectors.agreedRentChangeAnswer) mustBe no
    }
    "show the correct change link for agreed rent change in the rent details table" in {
      elementText(Selectors.agreedRentChangeChangeButton) mustBe "Change agreed-rent-change"
    }
    //whenDidYouAgree
    "show the correct message for when did you agree in the rent details table" in {
      elementText(Selectors.whenDidYouAgree) mustBe whenDidYouAgree
    }
    "show the correct answer for when did you agree in the rent details table" in {
      elementText(Selectors.whenDidYouAgreeAnswer) mustBe "12 December 2020"
    }
    "show the correct change link when did you agree in the rent details table" in {
      elementText(Selectors.whenDidYouAgreeChangeButton) mustBe "Change when-did-you-agree"
    }
    //totalAnnualRent
    "show the correct message for total annual rent in the rent details table" in {
      elementText(Selectors.totalAnnualRent) mustBe totalAnnualRent
    }
    "show the correct answer for total annual rent in the rent details table" in {
      elementText(Selectors.totalAnnualRentAnswer) mustBe "£12"
    }
    "show the correct change link for total annual rent in the rent details table" in {
      elementText(Selectors.totalAnnualRentChangeButton) mustBe "Change how-much-is-total-annual-rent"
    }
    //rentCheckRentPeriod
    "show the correct message for rent check rent period in the rent details table" in {
      elementText(Selectors.rentCheckRentPeriod) mustBe rentCheckRentPeriod
    }
    "show the correct answer for rent check rent period in the rent details table" in {
      elementText(Selectors.rentCheckRentPeriodAnswer) mustBe yes
    }
    "show the correct change link for rent check rent period in the rent details table" in {
      elementText(Selectors.rentCheckRentPeriodChangeButton) mustBe "Change check-rent-free-period"
    }
    //rentFreePeriod
    "show the correct message for rent free period in the rent details table" in {
      elementText(Selectors.rentFreePeriod) mustBe rentFreePeriod
    }
    "show the correct answer for rent free period in the rent details table" in {
      elementText(Selectors.rentFreePeriodAnswer) mustBe "1 months"
    }
    "show the correct change link for rent free period in the rent details table" in {
      elementText(Selectors.rentFreePeriodChangeButton) mustBe "Change rent-free-period-months"
    }
    //rentFreePeriodReason
    "show the correct message for rent free period reason in the rent details table" in {
      elementText(Selectors.rentFreePeriodReason) mustBe rentFreePeriodReason
    }
    "show the correct answer for rent free period reason in the rent details table" in {
      elementText(Selectors.rentFreePeriodReasonAnswer) mustBe "rent-free period reason"
    }
    "show the correct change link for rent free period reason in the rent details table" in {
      elementText(Selectors.rentFreePeriodReasonChangeButton) mustBe "Change rent-free-period-reason"
    }
    //startPayingDate
    "show the correct message for start paying date in the rent details table" in {
      elementText(Selectors.startPayingDate) mustBe startPayingDate
    }
    "show the correct answer for start paying date in the rent details table" in {
      elementText(Selectors.startPayingDateAnswer) mustBe "13 December 2020"
    }
    "show the correct change link for start paying date in the rent details table" in {
      elementText(Selectors.startPayingDateChangeButton) mustBe "Change start-paying-date"
    }

    //whatYourRentIncludes
    "show the correct label for what your rent includes table" in {
      elementText(Selectors.whatYourRentIncludesLabel) mustBe whatYourRentIncludesLabel
    }
    //livingAccommodation
    "show the correct message for living accommodation in the rent details table" in {
      elementText(Selectors.livingAccommodation) mustBe livingAccommodation
    }
    "show the correct answer for living accommodation in the rent details table" in {
      elementText(Selectors.livingAccommodationAnswer) mustBe yes
    }
    "show the correct change link for living accommodation in the rent details table" in {
      elementText(Selectors.livingAccommodationChangeButton) mustBe "Change living-accommodation"
    }
    //bedroomNumbers
    "show the correct message for bedroom numbers in the rent details table" in {
      elementText(Selectors.bedroomNumbers) mustBe bedroomNumbers
    }
    "show the correct answer for bedroom numbers in the rent details table" in {
      elementText(Selectors.bedroomNumbersAnswer) mustBe "1"
    }
    "show the correct change link for bedroom numbers in the rent details table" in {
      elementText(Selectors.bedroomNumbersChangeButton) mustBe "Change bedroom-numbers"
    }
    //rentPartAddress
    "show the correct message for rent part address in the rent details table" in {
      elementText(Selectors.rentPartAddress) mustBe rentPartAddress
    }
    "show the correct answer for rent part address in the rent details table" in {
      elementText(Selectors.rentPartAddressAnswer) mustBe no
    }
    "show the correct change link for rent part address in the rent details table" in {
      elementText(Selectors.rentPartAddressChangeButton) mustBe "Change rent-part-address"
    }
    //rentEmptyShell
    "show the correct message for rent empty shell in the rent details table" in {
      elementText(Selectors.rentEmptyShell) mustBe rentEmptyShell
    }
    "show the correct answer for rent empty shell in the rent details table" in {
      elementText(Selectors.rentEmptyShellAnswer) mustBe yes
    }
    "show the correct change link for rent empty shell in the rent details table" in {
      elementText(Selectors.rentEmptyShellChangeButton) mustBe "Change rent-empty-shell"
    }
    //rentIncBusinessRates
    "show the correct message for rent includes business rates in the rent details table" in {
      elementText(Selectors.rentIncBusinessRates) mustBe rentIncBusinessRates
    }
    "show the correct answer for rent includes business rates in the rent details table" in {
      elementText(Selectors.rentIncBusinessRatesAnswer) mustBe no
    }
    "show the correct change link for rent includes business rates in the rent details table" in {
      elementText(Selectors.rentIncBusinessRatesChangeButton) mustBe "Change rent-inc-business-rates"
    }
    //rentIncWaterCharges
    "show the correct message for rent inc water charges in the rent details table" in {
      elementText(Selectors.rentIncWaterCharges) mustBe rentIncWaterCharges
    }
    "show the correct answer for rent inc water charges in the rent details table" in {
      elementText(Selectors.rentIncWaterChargesAnswer) mustBe yes
    }
    "show the correct change link for rent inc water charges in the rent details table" in {
      elementText(Selectors.rentIncWaterChargesChangeButton) mustBe "Change rent-inc-water-charges"
    }
    //rentIncService
    "show the correct message for rent inc service in the rent details table" in {
      elementText(Selectors.rentIncService) mustBe rentIncService
    }
    "show the correct answer for rent inc service in the rent details table" in {
      elementText(Selectors.rentIncServiceAnswer) mustBe no
    }
    "show the correct change link for rent inc service in the rent details table" in {
      elementText(Selectors.rentIncServiceChangeButton) mustBe "Change rent-inc-service"
    }
    //doesYourRentIncludeParking
    "show the correct message for does your rent include parking in the rent details table" in {
      elementText(Selectors.doesYourRentIncludeParking) mustBe doesYourRentIncludeParking
    }
    "show the correct answer for does your rent include parking in the rent details table" in {
      elementText(Selectors.doesYourRentIncludeParkingAnswer) mustBe yes
    }
    "show the correct change link for does your rent include parking in the rent details table" in {
      elementText(Selectors.doesYourRentIncludeParkingChangeButton) mustBe "Change rent-inc-parking"
    }
    //howManyUncoveredSpacesIncludedInRent
    "show the correct message for how many uncovered spaces included in rent in the rent details table" in {
      elementText(Selectors.howManyUncoveredSpacesIncludedInRent) mustBe howManyUncoveredSpacesIncludedInRent
    }
    "show the correct answer for how many uncovered spaces included in rent in the rent details table" in {
      elementText(Selectors.howManyUncoveredSpacesIncludedInRentAnswer) mustBe "1"
    }
    "show the correct change link for how many uncovered spaces included in rent in the rent details table" in {
      elementText(Selectors.howManyUncoveredSpacesIncludedInRentChangeButton) mustBe "Change how-many-uncovered-spaces-included-in-rent"
    }
    //howManyCoveredSpacesIncludedInRent
    "show the correct message for how many covered spaces included in rent in rent in the rent details table" in {
      elementText(Selectors.howManyCoveredSpacesIncludedInRent) mustBe howManyCoveredSpacesIncludedInRent
    }
    "show the correct answer for how many covered spaces included in rent in rent in the rent details table" in {
      elementText(Selectors.howManyCoveredSpacesIncludedInRentAnswer) mustBe "2"
    }
    "show the correct change link for how many covered spaces included in rent in rent in the rent details table" in {
      elementText(Selectors.howManyCoveredSpacesIncludedInRentChangeButton) mustBe "Change how-many-covered-spaces-included-in-rent"
    }
    //howManyGaragesIncludedInRent
    "show the correct message for how many garages included in the rent details table" in {
      elementText(Selectors.howManyGaragesIncludedInRent) mustBe howManyGaragesIncludedInRent
    }
    "show the correct answer for how garages included in the rent details table" in {
      elementText(Selectors.howManyGaragesIncludedInRentAnswer) mustBe "3"
    }
    "show the correct change link for how many garages included in the rent details table" in {
      elementText(Selectors.howManyGaragesIncludedInRentChangeButton) mustBe "Change how-many-covered-spaces-included-in-rent"
    }
    //doYouPayExtraForParkingSpaces
    "show the correct message for do you pay extra for parking spaces in the rent details table" in {
      elementText(Selectors.doYouPayExtraForParkingSpaces) mustBe doYouPayExtraForParkingSpaces
    }
    "show the correct answer for do you pay extra for parking spaces rent in the rent details table" in {
      elementText(Selectors.doYouPayExtraForParkingSpacesAnswer) mustBe yes
    }
    "show the correct change link for do you pay extra for parking spaces in the rent details table" in {
      elementText(Selectors.doYouPayExtraForParkingSpacesButton) mustBe "Change do-you-pay-extra-for-parking-spaces"
    }
    //howManyUncoveredSpacesNotIncludedInRent
    "show the correct message for how many uncovered spaces not included in rent in the rent details table" in {
      elementText(Selectors.howManyUncoveredSpacesNotIncludedInRent) mustBe howManyUncoveredSpacesNotIncludedInRent
    }
    "show the correct answer for how many uncovered spaces not included in rent in the rent details table" in {
      elementText(Selectors.howManyUncoveredSpacesNotIncludedInRentAnswer) mustBe "1"
    }
    "show the correct change link for how many uncovered spaces not included in rent in the rent details table" in {
      elementText(Selectors.howManyUncoveredSpacesNotIncludedInRentChangeButton) mustBe "Change how-many-uncovered-spaces-not-included-in-rent"
    }
    //howManyCoveredSpacesNotIncludedInRent
    "show the correct message for how many covered spaces not included in rent, in the rent details table" in {
      elementText(Selectors.howManyCoveredSpacesNotIncludedInRent) mustBe howManyCoveredSpacesNotIncludedInRent
    }
    "show the correct answer for how many covered spaces not included in rent, in the rent details table" in {
      elementText(Selectors.howManyCoveredSpacesNotIncludedInRentAnswer) mustBe "2"
    }
    "show the correct change link for how many covered spaces not included in rent, in the rent details table" in {
      elementText(Selectors.howManyCoveredSpacesNotIncludedInRentChangeButton) mustBe "Change how-many-covered-spaces-not-included-in-rent"
    }
    //howManyGaragesNotIncludedInRent
    "show the correct message for how many garages not included in rent, in the rent details table" in {
      elementText(Selectors.howManyGaragesNotIncludedInRent) mustBe howManyGaragesNotIncludedInRent
    }
    "show the correct answer for how many garages not included in rent, in the rent details table" in {
      elementText(Selectors.howManyGaragesNotIncludedInRentAnswer) mustBe "3"
    }
    "show the correct change link for how many garages not included in rent, in the rent details table" in {
      elementText(Selectors.howManyGaragesNotIncludedInRentChangeButton) mustBe "Change how-many-garages-not-included-in-rent"
    }
    //totalCost
    "show the correct message for How much extra do you pay for parking and garages in the rent details table" in {
      elementText(Selectors.totalCost) mustBe totalCost
    }
    "show the correct answer for How much extra do you pay each year for parking and garages in the rent details table" in {
      elementText(Selectors.totalCostAnswer) mustBe "£12"
    }
    "show the correct change link for How much extra do you pay each year for parking and garages in the rent details table" in {
      elementText(Selectors.totalCostChangeButton) mustBe "Change parking-spaces-or-garages-not-included-in-your-rent-value"
    }
    //agreementDate
    "show the correct message for agreement date for parking and garages in the rent details table" in {
      elementText(Selectors.agreementDate) mustBe agreementDate
    }
    "show the correct answer for agreement date for parking and garages in the rent details table" in {
      elementText(Selectors.agreementDateAnswer) mustBe "12 December 2020"
    }
    "show the correct change link for agreement date for parking and garages in the rent details table" in {
      elementText(Selectors.agreementDateChangeButton) mustBe "Change parking-spaces-or-garages-not-included-in-your-rent-value"
    }

    //Repairs and insurance details table
    "show the correct label for repairs and insurance details table" in {
      elementText(Selectors.rentLabel) mustBe rentLabel
    }
    //internalRepairs
    "show the correct message for internal repairs in the repairs and insurance details table" in {
      elementText(Selectors.internalRepairs) mustBe internalRepairs
    }
    "show the correct answer for internal repairs in the repairs and insurance details table" in {
      elementText(Selectors.internalRepairsAnswer) mustBe "You"
    }
    "show the correct change link for internal repairs in the repairs and insurance details table" in {
      elementText(Selectors.internalRepairsChangeButton) mustBe "Change internal-repairs"
    }
    //externalRepairs
    "show the correct message for external repairs in the repairs and insurance details table" in {
      elementText(Selectors.externalRepairs) mustBe externalRepairs
    }
    "show the correct answer for external repairs in the repairs and insurance details table" in {
      elementText(Selectors.externalRepairsAnswer) mustBe "The landlord"
    }
    "show the correct change link for external repairs in the repairs and insurance details table" in {
      elementText(Selectors.externalRepairsChangeButton) mustBe "Change external-repairs"
    }
    //buildingInsurance
    "show the correct message for building insurance in the repairs and insurance details table" in {
      elementText(Selectors.buildingInsurance) mustBe buildingInsurance
    }
    "show the correct answer for building insurance in the repairs and insurance details table" in {
      elementText(Selectors.buildingInsuranceAnswer) mustBe "You and the landlord"
    }
    "show the correct change link for building insurance in the repairs and insurance details table" in {
      elementText(Selectors.buildingInsuranceChangeButton) mustBe "Change building-insurance"
    }

    //Repairs and fitting out details table
    "show the correct label for repairs and fitting out details table" in {
      elementText(Selectors.repairsAndFittingOutLabel) mustBe repairsAndFittingOutLabel
    }
    //repairsAndFittingOut
    "show the correct message for internal repairs in the repairs and fitting out details table" in {
      elementText(Selectors.repairsAndFittingOut) mustBe repairsAndFittingOut
    }
    "show the correct answer for internal repairs in the repairs and fitting out details table" in {
      elementText(Selectors.repairsAndFittingOutAnswer) mustBe yes
    }
    "show the correct change link for internal repairs in the repairs and fitting out details table" in {
      elementText(Selectors.repairsAndFittingOutChangeButton) mustBe "Change repairs-and-fitting-out"
    }
    //repairsAndFittingOutDate
    "show the correct message for external repairs in the repairs and fitting out details table" in {
      elementText(Selectors.repairsAndFittingOutDate) mustBe repairsAndFittingOutDate
    }
    "show the correct answer for external repairs in the repairs and fitting out details table" in {
      elementText(Selectors.repairsAndFittingOutDateAnswer) mustBe "January 2020"
    }
    "show the correct change link for external repairs in the repairs and fitting out details table" in {
      elementText(Selectors.repairsAndFittingOutDateChangeButton) mustBe "Change repairs-and-fitting-out-date"
    }
    //repairsAndFittingOutCost
    "show the correct message for building insurance in the repairs and fitting out details table" in {
      elementText(Selectors.repairsAndFittingOutCost) mustBe repairsAndFittingOutCost
    }
    "show the correct answer for building insurance in the repairs and fitting out details table" in {
      elementText(Selectors.repairsAndFittingOutCostAnswer) mustBe "£12"
    }
    "show the correct change link for building insurance in the repairs and fitting out details table" in {
      elementText(Selectors.repairsAndFittingOutCostChangeButton) mustBe "Change repairs-and-fitting-out-cost"
    }


    //payments table
    "show the correct label for payments table" in {
      elementText(Selectors.repairsAndFittingOutLabel) mustBe repairsAndFittingOutLabel
    }
    //didYouGetMoneyFromLandlord
    "show the correct message for did you get money from landlord in the payments table" in {
      elementText(Selectors.didYouGetMoneyFromLandlord) mustBe didYouGetMoneyFromLandlord
    }
    "show the correct answer for did you get money from landlord in the payments table" in {
      elementText(Selectors.didYouGetMoneyFromLandlordAnswer) mustBe no
    }
    "show the correct change link for did you get money from landlord in the payments table" in {
      elementText(Selectors.didYouGetMoneyFromLandlordChangeButton) mustBe "Change did-you-get-money-from-landlord"
    }
    //didYouPayAnyMoneyToLandlord
    "show the correct message for external repairs in the payments table" in {
      elementText(Selectors.didYouPayAnyMoneyToLandlord) mustBe didYouPayAnyMoneyToLandlord
    }
    "show the correct answer for external repairs in the payments table" in {
      elementText(Selectors.didYouPayAnyMoneyToLandlordAnswer) mustBe yes
    }
    "show the correct change link for external repairs in the payments table" in {
      elementText(Selectors.didYouPayAnyMoneyToLandlordChangeButton) mustBe "Change did-you-pay-money-to-landlord"
    }
    //repairsAndFittingOutCost
    "show the correct message for repairs and fitting out cost in the payments table" in {
      elementText(Selectors.moneyYouPaidInAdvanceToLandlordAmount) mustBe repairsAndFittingOutCost
    }
    "show the correct answer for repairs and fitting out cost in the payments table" in {
      elementText(Selectors.moneyYouPaidInAdvanceToLandlordAmountAnswer) mustBe "£12"
    }
    "show the correct change link for repairs and fitting out cost in the payments table" in {
      elementText(Selectors.moneyYouPaidInAdvanceToLandlordAmountChangeButton) mustBe "Change money-you-paid-in-advance-to-landlord-amount"
    }
    //moneyYouPaidInAdvanceToLandlordDate
    "show the correct message for money you paid in advance to landlord date in the payments table" in {
      elementText(Selectors.moneyYouPaidInAdvanceToLandlordDate) mustBe moneyYouPaidInAdvanceToLandlordDate
    }
    "show the correct answer for money you paid in advance to landlord date in the payments table" in {
      elementText(Selectors.moneyYouPaidInAdvanceToLandlordDateAnswer) mustBe "12 December 2020"
    }
    "show the correct change link for money you paid in advance to landlord date in the payments table" in {
      elementText(Selectors.moneyYouPaidInAdvanceToLandlordDateChangeButton) mustBe "Change money-you-paid-in-advance-to-landlord-amount"
    }

    //other details table
    "show the correct label for other details table" in {
      elementText(Selectors.otherDetailsLabel) mustBe otherDetailsLabel
    }
    //didYouGetMoneyFromLandlord
    "show the correct message for has anything else affected the rent in other details table" in {
      elementText(Selectors.hasAnyAffectedRent) mustBe hasAnyAffectedRent
    }
    "show the correct answer for has anything else affected the rent in the other details table" in {
      elementText(Selectors.hasAnyAffectedRentAnswer) mustBe yes
    }
    "show the correct change link for has anything else affected the rent in the other details table" in {
      elementText(Selectors.hasAnyAffectedRentChangeButton) mustBe "Change other-details"
    }
    //didYouPayAnyMoneyToLandlord
    "show the correct message for has anything else affected the rent reason in the other details table" in {
      elementText(Selectors.otherDetailsReason) mustBe otherDetailsReason
    }
    "show the correct answer for has anything else affected the rent reason in the other details table" in {
      elementText(Selectors.otherDetailsReasonAnswer) mustBe "something else"
    }
    "show the correct change link for has anything else affected the rent reason in the other details table" in {
      elementText(Selectors.otherDetailsReasonChangeButton) mustBe "Change other-details-reason"
    }

    "show the correct save button" in {
      elementText(Selectors.saveButton) mustBe saveButton
    }
  }

  val fullRenewedAgreementUserAnswers: UserAnswers = UserAnswers(
    testCredId,
    data = Json.obj(
      "tellUsAboutRenewedAgreement" -> "RenewedAgreement",
      "whatTypeOfLeaseRenewal" -> "RenewedAgreement",
      "landlord" -> Json.obj(
        "landlordName" -> "Jake",
        "hasRelationship" -> true,
        "landlordRelationship" -> "Parent"
      ),
      "whatTypeOfAgreement" -> "LeaseOrTenancy",
      "agreement" -> Json.obj(
        "agreementStart" -> "2020-12-12",
        "isOpenEnded" -> true,
        "haveBreakClause" -> true,
        "breakClauseInfo" -> "break clause reason"
      ),
      "whatIsYourRentBasedOn" -> Json.obj(
        "rentBased" -> "Other",
        "otherDesc" -> "Other reason"
      ),
      "agreedRentChange" -> true,
      "firstRentPeriod" -> Json.obj(
        "startDate" -> "2020-12-12",
        "endDate" -> "2021-01-01",
        "isRentPayablePeriod" -> true,
        "rentPeriodAmount" -> 12.0
      ),
      "rentPeriodsDetails" -> Json.arr(
        Json.obj("endDate" -> "2022-03-03", "rentPeriodAmount" -> 12.0),
        Json.obj("endDate" -> "2023-05-05", "rentPeriodAmount" -> 12.0)
      ),
      "rentPeriods" -> false,
      "didYouAgreeRentWithLandlordPage" -> true,
      "rentDatesAgree" -> "2020-12-12",
      "whatYourRentIncludes" -> Json.obj(
        "livingAccommodation" -> true,
        "rentPartAddress" -> false,
        "rentEmptyShell" -> true,
        "rentIncBusinessRates" -> false,
        "rentIncWaterCharges" -> true,
        "rentIncService" -> false,
        "bedroomNumbers" -> 1
      ),
      "doesYourRentIncludeParking" -> true,
      "howManyParkingSpacesOrGaragesIncludedInRent" -> Json.obj(
        "uncoveredSpaces" -> 1,
        "coveredSpaces" -> 2,
        "garages" -> 3
      ),
      "doYouPayExtraForParkingSpacesNotIncludedInRent" -> true,
      "parkingSpacesOrGaragesNotIncludedInYourRent" -> Json.obj(
        "uncoveredSpaces" -> 1,
        "coveredSpaces" -> 2,
        "garages" -> 3,
        "totalCost" -> 12.0,
        "agreementDate" -> "2020-12-12"
      ),
      "repairsAndInsurance" -> Json.obj(
        "internalRepairs" -> "You",
        "externalRepairs" -> "Landlord",
        "buildingInsurance" -> "YouAndLandlord"
      ),
      "rentReview" -> Json.obj(
        "hasIncludeRentReview" -> true,
        "rentReviewMonths" -> 2,
        "rentReviewYears" -> 1,
        "canRentGoDown" -> true
      ),
      "didYouGetMoneyFromLandlord" -> false,
      "didYouPayAnyMoneyToLandlord" -> true,
      "moneyYouPaidInAdvanceToLandlord" -> Json.obj(
        "amount" -> 12.0,
        "date" -> "2020-12-12"
      ),
      "hasAnythingElseAffectedTheRent" -> Json.obj(
        "radio" -> true,
        "reason" -> "something else"
      )
    )
  )


  "CheckAnswersView renewed agreement" must {

    val leaseRenewalDetailsLabel = "Lease renewal details"
    val whatLeaseRenewal = "What type of lease renewal is it?"
    val firstRentPeriodLabel = "First rent period"
    val firstPeriodStartDate = "Start date"
    val firstPeriodEndDate = "End date"
    val firstPeriodDoYouPay = "Do you pay rent in this period?"
    val firstPeriodPayAmount = "Rent for this period (excluding VAT)"
    val secondRentPeriodLabel = "Second rent period"
    val secondPeriodEndDate = "End date"
    val secondPeriodAmount = "Rent for this period (excluding VAT)"
    val thirdRentPeriodLabel = "Third rent period"
    val thirdPeriodEndDate = "End date"
    val thirdPeriodAmount = "Rent for this period (excluding VAT)"

    val checkAnswersView = view(
      selectedPropertyAddress = address,
      leaseRenewalsSummary = createLeaseRenewalsSummaryRows(fullRenewedAgreementUserAnswers),
      landlordSummary = createLandlordSummaryRows(fullRenewedAgreementUserAnswers),
      agreementDetailsSummary = createAgreementDetailsRows(fullRenewedAgreementUserAnswers),
      rentSummary = createRentRows(fullRenewedAgreementUserAnswers),
      firstRentPeriod = createFirstRentPeriodRow(fullRenewedAgreementUserAnswers),
      rentPeriods = createRentPeriodsSummaryLists(fullRenewedAgreementUserAnswers),
      whatYourRentIncludesSummary = createWhatYourRentIncludesRows(fullRenewedAgreementUserAnswers),
      repairsAndInsurance = createRepairsAndInsurance(fullRenewedAgreementUserAnswers),
      rentReview = createRentReviewRows(fullRenewedAgreementUserAnswers),
      repairsAndFittingOutSummary = createRepairsAndFittingOut(fullRenewedAgreementUserAnswers),
      payments = createPaymentRows(fullRenewedAgreementUserAnswers),
      breakClause = createBreakClauseRows(fullRenewedAgreementUserAnswers),
      otherDetailsSummary = createOtherDetailsRow(fullRenewedAgreementUserAnswers),
      isRentReviewed = false
    )

    lazy implicit val document: Document = Jsoup.parse(checkAnswersView.body)
    val htmlApply = view.apply(
      address,
      leaseRenewalsSummary = createLeaseRenewalsSummaryRows(fullRenewedAgreementUserAnswers),
      landlordSummary = createLandlordSummaryRows(fullRenewedAgreementUserAnswers),
      agreementDetailsSummary = createAgreementDetailsRows(fullRenewedAgreementUserAnswers),
      rentSummary = createRentRows(fullRenewedAgreementUserAnswers),
      firstRentPeriod = createFirstRentPeriodRow(fullRenewedAgreementUserAnswers),
      rentPeriods = createRentPeriodsSummaryLists(fullRenewedAgreementUserAnswers),
      whatYourRentIncludesSummary = createWhatYourRentIncludesRows(fullRenewedAgreementUserAnswers),
      repairsAndInsurance = createRepairsAndInsurance(fullRenewedAgreementUserAnswers),
      rentReview = createRentReviewRows(fullRenewedAgreementUserAnswers),
      repairsAndFittingOutSummary = createRepairsAndFittingOut(fullRenewedAgreementUserAnswers),
      payments = createPaymentRows(fullRenewedAgreementUserAnswers),
      breakClause = createBreakClauseRows(fullRenewedAgreementUserAnswers),
      otherDetailsSummary = createOtherDetailsRow(fullRenewedAgreementUserAnswers),
      isRentReviewed = false
    ).body

    val htmlRender = view.render(
      address,
      leaseRenewalsSummary = createLeaseRenewalsSummaryRows(fullRenewedAgreementUserAnswers),
      landlordSummary = createLandlordSummaryRows(fullRenewedAgreementUserAnswers),
      agreementDetailsSummary = createAgreementDetailsRows(fullRenewedAgreementUserAnswers),
      rentSummary = createRentRows(fullRenewedAgreementUserAnswers),
      firstRentPeriod = createFirstRentPeriodRow(fullRenewedAgreementUserAnswers),
      rentPeriods = createRentPeriodsSummaryLists(fullRenewedAgreementUserAnswers),
      whatYourRentIncludesSummary = createWhatYourRentIncludesRows(fullRenewedAgreementUserAnswers),
      repairsAndInsurance = createRepairsAndInsurance(fullRenewedAgreementUserAnswers),
      rentReview = createRentReviewRows(fullRenewedAgreementUserAnswers),
      repairsAndFittingOutSummary = createRepairsAndFittingOut(fullRenewedAgreementUserAnswers),
      payments = createPaymentRows(fullRenewedAgreementUserAnswers),
      breakClause = createBreakClauseRows(fullRenewedAgreementUserAnswers),
      otherDetailsSummary = createOtherDetailsRow(fullRenewedAgreementUserAnswers),
      isRentReviewed = false,
      request, messages, mockConfig).body

    "apply must nit be the same as render" in {
      htmlApply mustBe htmlRender
    }

    "render is not empty" in {
      htmlRender must not be empty
    }

    "show the correct title" in {
      elementText(Selectors.navTitle) mustBe title
    }

    "show the correct heading" in {
      elementText(Selectors.heading) mustBe heading
    }

    //Lease Renewal table
    "show the correct label for lease renewal table" in {
      elementText(Selectors.leaseRenewalDetailsLabel) mustBe leaseRenewalDetailsLabel
    }
    //what lease renewal
    "show the correct message for what lease renewal in the renewal table" in {
      elementText(Selectors.whatLeaseRenewal) mustBe whatLeaseRenewal
    }

    //first rent period table
    "show the correct label for first rent period table" in {
      elementText(Selectors.firstRentPeriodLabel) mustBe firstRentPeriodLabel
    }
    //first rent period start date
    "show the correct message for first period start date in the first rent period table" in {
      elementText(Selectors.firstPeriodStartDate) mustBe firstPeriodStartDate
    }
    //first rent period end date
    "show the correct message for first period end date in the first rent period table" in {
      elementText(Selectors.firstPeriodEndDate) mustBe firstPeriodEndDate
    }
    //first rent period do you pay
    "show the correct message for first period do you pay in the first rent period table" in {
      elementText(Selectors.firstPeriodDoYouPay) mustBe firstPeriodDoYouPay
    }
    //first rent period pay amount
    "show the correct message for first period pay amount in the first rent period table" in {
      elementText(Selectors.firstPeriodPayAmount) mustBe firstPeriodPayAmount
    }

    //second rent period table
    "show the correct label for second rent period table" in {
      elementText(Selectors.secondRentPeriodLabel) mustBe secondRentPeriodLabel
    }
    //second rent period start date
    "show the correct message for second period end date in the second rent period table" in {
      elementText(Selectors.secondPeriodEndDate) mustBe secondPeriodEndDate
    }
    //second rent period amount
    "show the correct message for second period amount in the second rent period table" in {
      elementText(Selectors.secondPeriodAmount) mustBe secondPeriodAmount
    }

    //third rent period table
    "show the correct label for third rent period table" in {
      elementText(Selectors.thirdRentPeriodLabel) mustBe thirdRentPeriodLabel
    }
    //third rent period start date
    "show the correct message for third period end date in the third rent period table" in {
      elementText(Selectors.thirdPeriodEndDate) mustBe thirdPeriodEndDate
    }
    //third rent period amount
    "show the correct message for third period amount in the third rent period table" in {
      elementText(Selectors.thirdPeriodAmount) mustBe thirdPeriodAmount
    }

  }
}
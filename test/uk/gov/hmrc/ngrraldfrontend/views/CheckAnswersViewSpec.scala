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
import play.api.data.Form
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.Radios
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.{NGRDate, NormalMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.AgreementVerbalForm
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.services.CheckAnswers.{createAgreementDetailsRows, createBreakClauseRows, createLandlordSummaryRows, createLeaseRenewalsSummaryRows, createOtherDetailsRow, createPaymentRows, createRentPeriodRow, createRentReviewRows, createRentRows, createRepairsAndFittingOut, createRepairsAndInsurance, createWhatYourRentIncludesRows}
import uk.gov.hmrc.ngrraldfrontend.views.html.{AgreementVerbalView, CheckAnswersView}
import uk.gov.hmrc.ngrraldfrontend.views.html.components.DateTextFields

class CheckAnswersViewSpec extends ViewBaseSpec {
  lazy val view: CheckAnswersView = inject[CheckAnswersView]

  val heading = "Check your answers"
  val title = s"$heading - GOV.UK"

  val landlordLabel = "Landlord details"
  val landlordFullName = "Landlords full name"
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
  val totalCost = "How much extra do you pay each year for parking and garages (excluding VAT)?"
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


  val saveButton = "Continue"

  object Selectors {
    val navTitle = "head > title"
    val heading = "#main-content > div > div.govuk-grid-column-two-thirds > h1"
    val landlordLabel = "#main-content > div > div.govuk-grid-column-two-thirds > h2:nth-child(2)"
    val landlordFullName = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(3) > div:nth-child(1) > dt"
    val landlordRelationship = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(3) > div:nth-child(2) > dt"
    val landlordRelationshipReason = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(3) > div:nth-child(3) > dt"

    val agreementDetailsLabel = "#main-content > div > div.govuk-grid-column-two-thirds > h2:nth-child(4)"
    val whatTypeOfAgreement = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(5) > div:nth-child(1) > dt"
    val agreementStartDate = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(5) > div:nth-child(2) > dt"
    val agreementIsOpenEnded = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(5) > div:nth-child(3) > dt"
    val agreementBreakClause = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(5) > div:nth-child(4) > dt"
    val agreementBreakClauseDetails = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(5) > div:nth-child(5) > dt"

    val rentLabel = "#main-content > div > div.govuk-grid-column-two-thirds > h2:nth-child(6)"
    val whatIsYourRentBasedOn = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(7) > div:nth-child(1) > dt"
    val otherReason = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(7) > div:nth-child(2) > dt"
    val didYouAgreeRentWithLandlord = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(7) > div:nth-child(3) > dt"
    val whenDidYouAgree = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(7) > div:nth-child(4) > dt"
    val totalAnnualRent = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(7) > div:nth-child(5) > dt"
    val rentCheckRentPeriod = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(7) > div:nth-child(6) > dt"
    val rentFreePeriod = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(7) > div:nth-child(7) > dt"
    val rentFreePeriodReason = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(7) > div:nth-child(8) > dt"
    val startPayingDate = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(7) > div:nth-child(9) > dt"

    val whatYourRentIncludesLabel = "#main-content > div > div.govuk-grid-column-two-thirds > h2:nth-child(8)"
    val livingAccommodation = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(9) > div:nth-child(1) > dt"
    val bedroomNumbers = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(9) > div:nth-child(2) > dt"
    val rentPartAddress = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(9) > div:nth-child(3) > dt"
    val rentEmptyShell = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(9) > div:nth-child(4) > dt"
    val checkRentPeriod = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(9) > div:nth-child(5) > dt"
    val rentIncBusinessRates = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(9) > div:nth-child(6) > dt"
    val rentIncWaterCharges = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(9) > div:nth-child(7) > dt"
    val rentIncService = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(9) > div:nth-child(8) > dt"
    val doesYourRentIncludeParking = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(9) > div:nth-child(9) > dt"
    val howManyUncoveredSpacesIncludedInRent = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(9) > div:nth-child(10) > dt"
    val howManyCoveredSpacesIncludedInRent = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(9) > div:nth-child(11) > dt"
    val howManyGaragesIncludedInRent = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(9) > div:nth-child(12) > dt"
    val howManyUncoveredSpacesNotIncludedInRent = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(9) > div:nth-child(13) > dt"
    val howManyCoveredSpacesNotIncludedInRent = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(9) > div:nth-child(14) > dt"
    val howManyGaragesNotIncludedInRent = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(9) > div:nth-child(15) > dt"
    val doYouPayExtraForParkingSpaces = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(9) > div:nth-child(16) > dt"
    val parkingSpacesOrGaragesNotIncludedInYourRent = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(9) > div:nth-child(17) > dt"
    val totalCost = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(9) > div:nth-child(18) > dt"
    val agreementDate = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(9) > div:nth-child(19) > dt"

    val repairsAndInsuranceLabel = ""
    val internalRepairs = ""
    val externalRepairs = ""
    val buildingInsurance = ""

    val rentReviewLabel = ""
    val hasIncludeRentReview = ""
    val howOftenReviewed = ""
    val canRentGoDown = ""

    val repairsAndFittingOutLabel = ""
    val repairsAndFittingOut = ""
    val repairsAndFittingOutDate = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(15) > div:nth-child(2) > dt"
    val repairsAndFittingOutCost = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(15) > div:nth-child(3) > dt"

    val paymentsLabel = "#main-content > div > div.govuk-grid-column-two-thirds > h2:nth-child(16)"
    val didYouGetMoneyFromLandlord = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(17) > div:nth-child(1) > dt"
    val didYouPayAnyMoneyToLandlord = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(17) > div:nth-child(2) > dt"
    val moneyYouPaidInAdvanceToLandlordAmount = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(17) > div:nth-child(3) > dt"
    val moneyYouPaidInAdvanceToLandlordDate = "#main-content > div > div.govuk-grid-column-two-thirds > dl:nth-child(17) > div:nth-child(4) > dt"

    val saveButton = "#continue"
  }

  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"
  val fullNewAgreementUserAnswers: UserAnswers = UserAnswers(testCredId, Json.obj(
    "tellUsAboutRent" -> "RentAgreement",
    "landlord" -> Json.obj(
      "landlordName" -> "Anna"
    )
  ))

  "CheckAnswersView" must {
    val checkAnswersView = view(
      selectedPropertyAddress = address,
      leaseRenewalsSummary = createLeaseRenewalsSummaryRows(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      landlordSummary = createLandlordSummaryRows(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      agreementDetailsSummary = createAgreementDetailsRows(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      rentSummary = createRentRows(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      firstRentPeriod = createRentPeriodRow(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      whatYourRentIncludesSummary = createWhatYourRentIncludesRows(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      repairsAndInsurance = createRepairsAndInsurance(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      rentReview = createRentReviewRows(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      repairsAndFittingOutSummary = createRepairsAndFittingOut(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      payments = createPaymentRows(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      breakClause = createBreakClauseRows(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      otherDetailsSummary = createOtherDetailsRow(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers))
    )

    lazy implicit val document: Document = Jsoup.parse(checkAnswersView.body)
    val htmlApply = view.apply(
      address,
      leaseRenewalsSummary = createLeaseRenewalsSummaryRows(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      landlordSummary = createLandlordSummaryRows(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      agreementDetailsSummary = createAgreementDetailsRows(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      rentSummary = createRentRows(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      firstRentPeriod = createRentPeriodRow(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      whatYourRentIncludesSummary = createWhatYourRentIncludesRows(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      repairsAndInsurance = createRepairsAndInsurance(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      rentReview = createRentReviewRows(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      repairsAndFittingOutSummary = createRepairsAndFittingOut(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      payments = createPaymentRows(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      breakClause = createBreakClauseRows(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      otherDetailsSummary = createOtherDetailsRow(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers))
    ).body

    val htmlRender = view.render(
      address,
      leaseRenewalsSummary = createLeaseRenewalsSummaryRows(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      landlordSummary = createLandlordSummaryRows(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      agreementDetailsSummary = createAgreementDetailsRows(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      rentSummary = createRentRows(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      firstRentPeriod = createRentPeriodRow(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      whatYourRentIncludesSummary = createWhatYourRentIncludesRows(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      repairsAndInsurance = createRepairsAndInsurance(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      rentReview = createRentReviewRows(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      repairsAndFittingOutSummary = createRepairsAndFittingOut(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      payments = createPaymentRows(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      breakClause = createBreakClauseRows(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
      otherDetailsSummary = createOtherDetailsRow(credId = testCredId.value, userAnswers = Some(fullNewAgreementUserAnswers)),
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

    "show the correct save button" in {
      elementText(Selectors.saveButton) mustBe saveButton
    }
  }
}
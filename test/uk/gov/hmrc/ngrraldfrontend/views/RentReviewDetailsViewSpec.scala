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
import uk.gov.hmrc.govukfrontend.views.html.components.GovukRadios
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.Radios
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentReviewDetailsForm
import uk.gov.hmrc.ngrraldfrontend.models.{NGRDate, NormalMode}
import uk.gov.hmrc.ngrraldfrontend.views.html.RentReviewDetailsView

class RentReviewDetailsViewSpec extends ViewBaseSpec {
  lazy val view: RentReviewDetailsView = inject[RentReviewDetailsView]
  lazy val govukRadios: GovukRadios = inject[GovukRadios]

  object Strings {
    val heading = "Rent review"
    val p1 = "These details should be in a a document that sets out the new terms that you agreed with your landlord or their agent. This is often called a rent review memorandum."
    val annualAmountLabel = "How much is your new total annual rent?"
    val annualAmountHint = "This is the amount you pay each year (excluding VAT) even if you pay monthly or quarterly"
    val whatHappensRadioLabel = "What did your agreement say could happen to the rent at rent review?"
    val whatHappensRadioButton1Label = "The rent can go up or down"
    val whatHappensRadioButton2Label = "The rent can only go up"
    val startDateLabel = "When will you start paying rent?"
    val startDateDayLabel = "Day"
    val startDateMonthLabel = "Month"
    val startDateYearLabel = "Year"
    val startDateHint = "For example, 27 6 2026"
    val hasAgreedRadioLabel = "Did you and the landlord (or their agent) agree the new rent?"
    val whoAgreedRadioLabel = "Who agreed the new rent?"
    val whoAgreedRadioButton1Label = "An arbitrator"
    val whoAgreedRadioButton2Label = "An independent expert"
    val yesLabel = "Yes"
    val noLabel = "No"
    val continue = "Continue"
  }

  object Selectors {
    val heading = "#main-content > div > div.govuk-grid-column-two-thirds > form > h1"
    val p1 = "#main-content > div > div.govuk-grid-column-two-thirds > form > p"
    val annualAmountLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(4) > label"
    val annualAmountHint = "#annualAmount-hint"
    val whatHappensRadioLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(5) > fieldset > legend"
    val whatHappensRadioButton1Label = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(5) > fieldset > div > div:nth-child(1) > label"
    val whatHappensRadioButton2Label = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(5) > fieldset > div > div:nth-child(2) > label"
    val startDateLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(6) > fieldset > legend"
    val startDateDayLabel = "#startDate > div:nth-child(1) > div > label"
    val startDateMonthLabel = "#startDate > div:nth-child(2) > div > label"
    val startDateYearLabel = "#startDate > div:nth-child(3) > div > label"
    val startDateHint = "#startDate-hint"
    val hasAgreedRadioLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(7) > fieldset > legend"
    val hasAgreedRadioButton1Label = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(7) > fieldset > div > div:nth-child(1) > label"
    val hasAgreedRadioButton2Label = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(7) > fieldset > div > div:nth-child(2) > label"
    val whoAgreedRadioLabel = "#conditional-has-agreed-new-rent-radio-2 > div > fieldset > legend"
    val whoAgreedRadioButton1Label = "#conditional-has-agreed-new-rent-radio-2 > div > fieldset > div > div:nth-child(1) > label"
    val whoAgreedRadioButton2Label = "#conditional-has-agreed-new-rent-radio-2 > div > fieldset > div > div:nth-child(2) > label"
    val continue = "#continue"
  }

  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"
  val form = RentReviewDetailsForm.form.fillAndValidate(RentReviewDetailsForm(BigDecimal("3000"), "GoUpOrDown", NGRDate("30", "10", "2020"), "false", Some("Arbitrator")))
  private val whatHappensRadio: Radios = buildRadios(form, RentReviewDetailsForm.createWhatHappensAtRentReviewRadio)
  private val canRentGoDownRadio: Radios = buildRadios(form, RentReviewDetailsForm.createHasAgreedNewRentRadio(form, govukRadios))

  "RentReviewDetailsView" must {
    val rentReviewDetailsView = view(form, whatHappensRadio, canRentGoDownRadio, address, NormalMode)
    lazy implicit val document: Document = Jsoup.parse(rentReviewDetailsView.body)
    val htmlApply = view.apply(form, whatHappensRadio, canRentGoDownRadio, address, NormalMode).body
    val htmlRender = view.render(form, whatHappensRadio, canRentGoDownRadio, address, NormalMode, request, messages, mockConfig).body
    lazy val htmlF = view.f(form, whatHappensRadio, canRentGoDownRadio, address, NormalMode)

    "htmlF is not empty" in {
      htmlF.toString() must not be empty
    }

    "apply must be the same as render" in {
      htmlApply mustBe htmlRender
    }

    "render is not empty" in {
      htmlRender must not be empty
    }

    "show correct heading" in {
      elementText(Selectors.heading) mustBe Strings.heading
    }

    "show correct annual rent amount label" in {
      elementText(Selectors.annualAmountLabel) mustBe Strings.annualAmountLabel
    }

    "show correct annual rent amount hint" in {
      elementText(Selectors.annualAmountHint) mustBe Strings.annualAmountHint
    }

    "show correct what happens at rent review radio title" in {
      elementText(Selectors.whatHappensRadioLabel) mustBe Strings.whatHappensRadioLabel
    }

    "show correct what happens at rent review radio button 1 label" in {
      elementText(Selectors.whatHappensRadioButton1Label) mustBe Strings.whatHappensRadioButton1Label
    }

    "show correct what happens at rent review radio button 2 label" in {
      elementText(Selectors.whatHappensRadioButton2Label) mustBe Strings.whatHappensRadioButton2Label
    }

    "show correct start date label" in {
      elementText(Selectors.startDateLabel) mustBe Strings.startDateLabel
    }

    "show correct start date hint" in {
      elementText(Selectors.startDateHint) mustBe Strings.startDateHint
    }

    "show correct start date day label" in {
      elementText(Selectors.startDateDayLabel) mustBe Strings.startDateDayLabel
    }

    "show correct start date month label" in {
      elementText(Selectors.startDateMonthLabel) mustBe Strings.startDateMonthLabel
    }

    "show correct start date year label" in {
      elementText(Selectors.startDateYearLabel) mustBe Strings.startDateYearLabel
    }

    "show correct has agreed new rent radio label" in {
      elementText(Selectors.hasAgreedRadioLabel) mustBe Strings.hasAgreedRadioLabel
    }

    "show correct has agreed new rent radio button 1 label" in {
      elementText(Selectors.hasAgreedRadioButton1Label) mustBe Strings.yesLabel
    }

    "show correct has agreed new rent radio button 2 label" in {
      elementText(Selectors.hasAgreedRadioButton2Label) mustBe Strings.noLabel
    }

    "show correct who agreed new rent radio label" in {
      elementText(Selectors.whoAgreedRadioLabel) mustBe Strings.whoAgreedRadioLabel
    }

    "show correct who agreed new rent radio button 1 label" in {
      elementText(Selectors.whoAgreedRadioButton1Label) mustBe Strings.whoAgreedRadioButton1Label
    }

    "show correct who agreed new rent radio button 2 label" in {
      elementText(Selectors.whoAgreedRadioButton2Label) mustBe Strings.whoAgreedRadioButton2Label
    }

    "show correct continue button" in {
      elementText(Selectors.continue) mustBe Strings.continue
    }
  }
}


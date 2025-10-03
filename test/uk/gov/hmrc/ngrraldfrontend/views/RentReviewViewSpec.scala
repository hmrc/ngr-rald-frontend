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
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.Radios
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentReviewForm
import uk.gov.hmrc.ngrraldfrontend.models.{NGRMonthYear, NormalMode}
import uk.gov.hmrc.ngrraldfrontend.views.html.RentReviewView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputDateForMonthYear

class RentReviewViewSpec extends ViewBaseSpec {
  lazy val view: RentReviewView = inject[RentReviewView]
  lazy val inputDateForMonthYear: InputDateForMonthYear = inject[InputDateForMonthYear]

  object Strings {
    val heading = "Rent review"
    val hasIncludedRentReviewRadioTitle = "Does your agreement include a rent review?"
    val hasIncludedRentReviewRadioHint = "If you have a rent review, you and your landlord can agree a new rent on the date set out in your agreement. This is different to a stepped rent, where changes to your rent are described in your agreement."
    val canRentGoDownTitle = "Can the rent go down when it is reviewed?"
    val yesLabel = "Yes"
    val noLabel = "No"
    val reviewYearsMonthsTitle = "How often is your rent reviewed?"
    val years = "Years"
    val months = "Months"
    val continue = "Continue"
  }

  object Selectors {
    val heading = "#main-content > div > div.govuk-grid-column-two-thirds > form > h1"
    val hasIncludedRentReviewRadioTitle = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(3) > fieldset > legend > h1"
    val hasIncludedRentReviewRadioHint = "#has-include-rent-review-radio-hint"
    val reviewYearsMonthsTitle = "#conditional-has-include-rent-review-radio > div > fieldset > legend"
    val years = "#date > div:nth-child(1) > div > label"
    val months = "#date > div:nth-child(2) > div > label"
    val canRentGoDownTitle = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(4) > fieldset > legend"
    val radio1YesLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(3) > fieldset > div.govuk-radios.govuk-radios > div:nth-child(1) > label"
    val radio1NoLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(3) > fieldset > div.govuk-radios.govuk-radios > div:nth-child(3) > label"
    val radio2YesLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(4) > fieldset > div > div:nth-child(1) > label"
    val radio2NoLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(4) > fieldset > div > div:nth-child(2) > label"
    val continue = "#continue"
  }

  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"
  val form = RentReviewForm.form.fillAndValidate(RentReviewForm("true", Some(NGRMonthYear("11", "1")), "false"))
  private val hasIncludedRentReviewRadio: NGRRadio = RentReviewForm.createHasIncludeRentReviewRadio(form, inputDateForMonthYear)
  private val canRentGoDownRadio: NGRRadio = RentReviewForm.createCanRentGoDownRadio
  val radio1: Radios = buildRadios(form, hasIncludedRentReviewRadio)
  val radio2: Radios = buildRadios(form, canRentGoDownRadio)

  "RentReviewView" must {
    val rentReviewView = view(form, radio1, radio2, address, NormalMode)
    lazy implicit val document: Document = Jsoup.parse(rentReviewView.body)
    val htmlApply = view.apply(form, radio1, radio2, address, NormalMode).body
    val htmlRender = view.render(form, radio1, radio2, address, NormalMode, request, messages, mockConfig).body
    lazy val htmlF = view.f(form, radio1, radio2, address, NormalMode)

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

    "show correct has included rent review radio title" in {
      elementText(Selectors.hasIncludedRentReviewRadioTitle) mustBe Strings.hasIncludedRentReviewRadioTitle
    }

    "show correct has included rent review radio hint" in {
      elementText(Selectors.hasIncludedRentReviewRadioHint) mustBe Strings.hasIncludedRentReviewRadioHint
    }

    "show correct has included rent review yes radio label" in {
      elementText(Selectors.radio1YesLabel) mustBe Strings.yesLabel
    }

    "show correct has included rent review no radio label" in {
      elementText(Selectors.radio1NoLabel) mustBe Strings.noLabel
    }

    "show correct how often rent review title" in {
      elementText(Selectors.reviewYearsMonthsTitle) mustBe Strings.reviewYearsMonthsTitle
    }

    "show correct how often rent review months label" in {
      elementText(Selectors.months) mustBe Strings.months
    }

    "show correct how often rent review years label" in {
      elementText(Selectors.years) mustBe Strings.years
    }

    "show correct can rent go down radio title" in {
      elementText(Selectors.canRentGoDownTitle) mustBe Strings.canRentGoDownTitle
    }

    "show correct can rent go down yes radio label" in {
      elementText(Selectors.radio2YesLabel) mustBe Strings.yesLabel
    }

    "show correct can rent go down no radio label" in {
      elementText(Selectors.radio2NoLabel) mustBe Strings.noLabel
    }

    "show correct continue button" in {
      elementText(Selectors.continue) mustBe Strings.continue
    }
  }
}


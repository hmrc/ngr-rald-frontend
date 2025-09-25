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
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.{NGRDate, NormalMode}
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentDatesAgreeStartForm
import uk.gov.hmrc.ngrraldfrontend.views.html.RentDatesAgreeStartView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.DateTextFields

class RentDatesAgreeStartViewSpec extends ViewBaseSpec {
  lazy val view: RentDatesAgreeStartView = inject[RentDatesAgreeStartView]
  val dateTextFields: DateTextFields = inject[DateTextFields]
  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"

  val heading = "Rent dates"
  val title = s"$heading - GOV.UK"
  val agreedDateTile = "When did you agree your rent?"
  val agreedDateHint = "For example, 27 6 2026"
  val startPayingDateTitle = "When will you start paying rent?"
  val startPayingDateHint = "If you are already paying rent, tell us when you started paying it, for example, 27 6 2026"
  val dayInputLabel = "Day"
  val monthInputLabel = "Month"
  val yearInputLabel = "Year"
  val saveButton = "Continue"

  private val form: Form[RentDatesAgreeStartForm] = RentDatesAgreeStartForm.form.fillAndValidate(RentDatesAgreeStartForm(NGRDate("30", "4", "2025"), NGRDate("1", "6", "2025")))

  object Selectors {
    val navTitle = "head > title"
    val heading = "#main-content > div > div.govuk-grid-column-two-thirds > form > h1"
    val addressCaption = "#main-content > div > div.govuk-grid-column-two-thirds > form > span"
    val agreedDateTile = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(3) > fieldset > legend"
    val agreedDateHint = "#agreedDate-hint"
    val agreedDateDayInputLabel = "#agreedDate > div:nth-child(1) > div > label"
    val agreedDateMonthInputLabel = "#agreedDate > div:nth-child(2) > div > label"
    val agreedDateYearInputLabel = "#agreedDate > div:nth-child(3) > div > label"
    val startPayingDateTitle = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(4) > fieldset > legend"
    val startPayingDateHint = "#startPayingDate-hint"
    val startPayingDateDayInputLabel = "#startPayingDate > div:nth-child(1) > div > label"
    val startPayingDateMonthInputLabel = "#startPayingDate > div:nth-child(2) > div > label"
    val startPayingDateYearInputLabel = "#startPayingDate > div:nth-child(3) > div > label"
    val saveButton = "#continue"
  }

  "RentDatesAgreeStartView" must {
    val rentDatesAgreeStartView = view(form, address, NormalMode)
    lazy implicit val document: Document = Jsoup.parse(rentDatesAgreeStartView.body)
    val htmlApply = view.apply(form, address, NormalMode).body
    val htmlRender = view.render(form, address, NormalMode, request, messages, mockConfig).body
    lazy val htmlF = view.f(form, address, NormalMode)

    "htmlF is not empty" in {
      htmlF.toString() must not be empty
    }

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

    "show the correct agree date title" in {
      elementText(Selectors.agreedDateTile) mustBe agreedDateTile
    }

    "show the correct agree start date hint" in {
      elementText(Selectors.agreedDateHint) mustBe agreedDateHint
    }

    "show the correct agree date day field label" in {
      elementText(Selectors.agreedDateDayInputLabel) mustBe dayInputLabel
    }

    "show the correct agree date month field label" in {
      elementText(Selectors.agreedDateMonthInputLabel) mustBe monthInputLabel
    }

    "show the correct agree date year field label" in {
      elementText(Selectors.agreedDateYearInputLabel) mustBe yearInputLabel
    }

    "show the correct start paying date title" in {
      elementText(Selectors.startPayingDateTitle) mustBe startPayingDateTitle
    }

    "show the correct start paying start date hint" in {
      elementText(Selectors.startPayingDateHint) mustBe startPayingDateHint
    }

    "show the correct start paying date day field label" in {
      elementText(Selectors.startPayingDateDayInputLabel) mustBe dayInputLabel
    }

    "show the correct start paying date month field label" in {
      elementText(Selectors.startPayingDateMonthInputLabel) mustBe monthInputLabel
    }

    "show the correct start paying date year field label" in {
      elementText(Selectors.startPayingDateYearInputLabel) mustBe yearInputLabel
    }

    "show the correct save button" in {
      elementText(Selectors.saveButton) mustBe saveButton
    }
  }
}


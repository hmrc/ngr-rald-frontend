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
import uk.gov.hmrc.govukfrontend.views.viewmodels.dateinput.DateInput
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.forms.ProvideDetailsOfSecondRentPeriodForm
import uk.gov.hmrc.ngrraldfrontend.models.{NGRDate, NormalMode}
import uk.gov.hmrc.ngrraldfrontend.views.html.ProvideDetailsOfSecondRentPeriodView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText

import java.time.LocalDate

class AdditionalRentPeriodViewSpec extends ViewBaseSpec:

  private val view: ProvideDetailsOfSecondRentPeriodView = inject[ProvideDetailsOfSecondRentPeriodView]
  private val inputText: InputText = inject[InputText]

  object Strings:
    val address = "2A, RODLEY LANE, RODLEY, LEEDS, BH1 1HU"
    val heading = "Third rent period"
    val startDateLabel = "Third rent period start date"
    val startDateHint = "This is the day after the second rent period ended"
    val endDateLabel = "When does the third rent period end?"
    val endDateHint = "For example, 27 6 2026"
    val endDateDayLabel = "Day"
    val endDateMonthLabel = "Month"
    val endDateYearLabel = "Year"
    val endDateDay = "31"
    val endDateMonth = "12"
    val endDateYear = "2025"
    val formatDate = "1 January 2000"
    val rentAmountLabel = "How much is the rent for this period (excluding VAT)?"
    val rentAmountHint = "Enter the amount you pay each year (excluding VAT) even if the period is for more or less than a year."
    val rentAmount = "1999000"
    val continue = "Continue"


  object Selectors {
    val address = "#main-content > div > div.govuk-grid-column-two-thirds > form > span"
    val heading = "#main-content > div > div.govuk-grid-column-two-thirds > form > h1"
    val startDateLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > h2"
    val startDateHint = "#main-content > div > div.govuk-grid-column-two-thirds > form > p.govuk-hint"
    val startDate = "#main-content > div > div.govuk-grid-column-two-thirds > form > p.govuk-body"
    val endDateLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(6) > fieldset > legend"
    val endDateHint = "#endDate-hint"
    val endDateDayLabel = "#endDate > div:nth-child(1) > div > label"
    val endDateMonthLabel = "#endDate > div:nth-child(2) > div > label"
    val endDateYearLabel = "#endDate > div:nth-child(3) > div > label"
    val endDateDay = "#endDate\\.day"
    val endDateMonth = "#endDate\\.month"
    val endDateYear = "#endDate\\.year"

    val rentAmountLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(7) > label"
    val rentAmountHint = "#rentPeriodAmount-hint"
    val rentAmount = "#rentPeriodAmount"
    val continue = "#continue"
  }

  private val thirdPeriodIndex: Int = 1
  private val form = ProvideDetailsOfSecondRentPeriodForm.form(LocalDate.parse("2025-10-01"), thirdPeriodIndex).fillAndValidate(
    ProvideDetailsOfSecondRentPeriodForm(
      NGRDate("31", "12", "2025"),
      BigDecimal(1999000)
    )
  )

  private val endDateInput: DateInput = ProvideDetailsOfSecondRentPeriodForm.endDateInput(thirdPeriodIndex)

  "ProvideDetailsOfSecondRentPeriodView" must {
    val secondRentPeriodView = view(Strings.address, form, Strings.formatDate, endDateInput, NormalMode, thirdPeriodIndex)
    implicit val document: Document = Jsoup.parse(secondRentPeriodView.body)
    val htmlApply = view.apply(Strings.address, form, Strings.formatDate, endDateInput, NormalMode, thirdPeriodIndex).body
    val htmlRender = view.render(Strings.address, form, Strings.formatDate, endDateInput, NormalMode, thirdPeriodIndex, request, messages, mockConfig).body
    val htmlF = view.f(Strings.address, form, Strings.formatDate, endDateInput, NormalMode, thirdPeriodIndex)

    "htmlF is not empty" in {
      htmlF.toString() must not be empty
    }

    "apply must be the same as render" in {
      htmlApply mustBe htmlRender
    }

    "render is not empty" in {
      htmlRender must not be empty
    }

    "show only one H1 heading" in {
      document.select("h1").asList.size mustBe 1
    }

    "show correct address" in {
      elementText(Selectors.address) mustBe Strings.address
    }

    "show correct heading" in {
      elementText(Selectors.heading) mustBe Strings.heading
    }

    "show correct start date" in {
      elementText(Selectors.startDate) mustBe Strings.formatDate
    }

    "show correct start date hint" in {
      elementText(Selectors.startDateHint) mustBe Strings.startDateHint
    }

    "show correct start date label" in {
      elementText(Selectors.startDateLabel) mustBe Strings.startDateLabel
    }

    "show correct end date label" in {
      elementText(Selectors.endDateLabel) mustBe Strings.endDateLabel
    }

    "show correct end date hint" in {
      elementText(Selectors.endDateHint) mustBe Strings.endDateHint
    }

    "show correct end date day label" in {
      elementText(Selectors.endDateDayLabel) mustBe Strings.endDateDayLabel
    }

    "show correct end date month label" in {
      elementText(Selectors.endDateMonthLabel) mustBe Strings.endDateMonthLabel
    }

    "show correct end date year label" in {
      elementText(Selectors.endDateYearLabel) mustBe Strings.endDateYearLabel
    }

    "show correct end date day" in {
      elementValue(Selectors.endDateDay) mustBe Strings.endDateDay
    }

    "show correct end date month" in {
      elementValue(Selectors.endDateMonth) mustBe Strings.endDateMonth
    }

    "show correct end date year" in {
      elementValue(Selectors.endDateYear) mustBe Strings.endDateYear
    }

    "show correct rent amount label" in {
      elementText(Selectors.rentAmountLabel) mustBe Strings.rentAmountLabel
    }

    "show correct rent amount hint" in {
      elementText(Selectors.rentAmountHint) mustBe Strings.rentAmountHint
    }

    "show correct rent amount" in {
      elementValue(Selectors.rentAmount) mustBe Strings.rentAmount
    }

    "show continue button" in {
      elementText(Selectors.continue) mustBe Strings.continue
    }
  }

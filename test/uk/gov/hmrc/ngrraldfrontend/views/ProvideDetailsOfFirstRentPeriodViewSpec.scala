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
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.Radios
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.ProvideDetailsOfFirstRentPeriod
import uk.gov.hmrc.ngrraldfrontend.models.forms.ProvideDetailsOfFirstRentPeriodForm
import uk.gov.hmrc.ngrraldfrontend.models.NormalMode
import uk.gov.hmrc.ngrraldfrontend.views.html.ProvideDetailsOfFirstRentPeriodView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText

import java.time.LocalDate

class ProvideDetailsOfFirstRentPeriodViewSpec extends ViewBaseSpec:

  private val view: ProvideDetailsOfFirstRentPeriodView = inject[ProvideDetailsOfFirstRentPeriodView]
  private val inputText: InputText = inject[InputText]

  object Strings:
    val address = "2A, RODLEY LANE, RODLEY, LEEDS, BH1 1HU"
    val heading = "First rent period"
    val startDateLabel = "When does the first rent period start?"
    val startDateHint = "For example, 27 6 2026"
    val startDateDayLabel = "Day"
    val startDateMonthLabel = "Month"
    val startDateYearLabel = "Year"
    val startDateDay = "1"
    val startDateMonth = "1"
    val startDateYear = "2025"
    val endDateLabel = "When does the first rent period end?"
    val endDateHint = "For example, 27 6 2026"
    val endDateDayLabel = "Day"
    val endDateMonthLabel = "Month"
    val endDateYearLabel = "Year"
    val endDateDay = "31"
    val endDateMonth = "12"
    val endDateYear = "2025"
    val isRentPayablePeriodRadioLabel = "Do you pay rent in this period?"
    val yesLabel = "Yes"
    val noLabel = "No, this is a rent-free period"
    val rentAmountLabel = "How much is the rent for this period (excluding VAT)?"
    val rentAmountHint = "Enter the amount you pay each year (excluding VAT) even if the period is for more or less than a year."
    val rentAmount = "1999000"
    val continue = "Continue"

  object Selectors:
    val address = "span.govuk-caption-m"
    val heading = "h1"
    val startDateLabel = "legend:has(+ #startDate-hint)"
    val startDateHint = "#startDate-hint"
    val startDateDayLabel = "label[for=startDate.day]"
    val startDateMonthLabel = "label[for=startDate.month]"
    val startDateYearLabel = "label[for=startDate.year]"
    val startDateDay = "#startDate\\.day"
    val startDateMonth = "#startDate\\.month"
    val startDateYear = "#startDate\\.year"
    val endDateLabel = "legend:has(+ #endDate-hint)"
    val endDateHint = "#endDate-hint"
    val endDateDayLabel = "label[for=endDate.day]"
    val endDateMonthLabel = "label[for=endDate.month]"
    val endDateYearLabel = "label[for=endDate.year]"
    val endDateDay = "#endDate\\.day"
    val endDateMonth = "#endDate\\.month"
    val endDateYear = "#endDate\\.year"
    val isRentPayablePeriodRadioLabel = "legend:has(+ .govuk-radios)"
    val yesLabel = "label[for=provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod]"
    val noLabel = "label[for=provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod-2]"
    val isRentPayablePeriodRadioButtonYes = "input[type=radio][name=provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod][value=true]"
    val rentAmountLabel = "label[for=rentPeriodAmount]"
    val rentAmountHint = "#rentPeriodAmount-hint"
    val rentAmount = "#rentPeriodAmount"
    val continue = "#continue"

  private val form = ProvideDetailsOfFirstRentPeriodForm.form.fillAndValidate(
    ProvideDetailsOfFirstRentPeriod(
      LocalDate.parse("2025-01-01"),
      LocalDate.parse("2025-12-31"),
      true,
      Some(BigDecimal(1999000))
    )
  )
  private val startDateInput: DateInput = ProvideDetailsOfFirstRentPeriodForm.firstDateStartInput
  private val endDateInput: DateInput = ProvideDetailsOfFirstRentPeriodForm.firstDateEndInput
  private val firstPeriodRadio: Radios = buildRadios(form, ProvideDetailsOfFirstRentPeriodForm.firstRentPeriodRadio(form, inputText))

  "ProvideDetailsOfFirstRentPeriodView" must {
    val firstRentPeriodView = view(Strings.address, form, startDateInput, endDateInput, firstPeriodRadio, NormalMode)
    implicit val document: Document = Jsoup.parse(firstRentPeriodView.body)
    val htmlApply = view.apply(Strings.address, form, startDateInput, endDateInput, firstPeriodRadio, NormalMode).body
    val htmlRender = view.render(Strings.address, form, startDateInput, endDateInput, firstPeriodRadio, NormalMode, request, messages, mockConfig).body
    val htmlF = view.f(Strings.address, form, startDateInput, endDateInput, firstPeriodRadio, NormalMode)

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

    "show correct start date day" in {
      elementValue(Selectors.startDateDay) mustBe Strings.startDateDay
    }

    "show correct start date month" in {
      elementValue(Selectors.startDateMonth) mustBe Strings.startDateMonth
    }

    "show correct start date year" in {
      elementValue(Selectors.startDateYear) mustBe Strings.startDateYear
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

    "show correct isRentPayablePeriod radio label" in {
      elementText(Selectors.isRentPayablePeriodRadioLabel) mustBe Strings.isRentPayablePeriodRadioLabel
    }

    "show correct isRentPayablePeriod radio button Yes label" in {
      elementText(Selectors.yesLabel) mustBe Strings.yesLabel
    }

    "show correct isRentPayablePeriod radio button No label" in {
      elementText(Selectors.noLabel) mustBe Strings.noLabel
    }

    "show isRentPayablePeriod radio button with Yes checked" in {
      element(Selectors.isRentPayablePeriodRadioButtonYes).hasAttr("checked") mustBe true
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

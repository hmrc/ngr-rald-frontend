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
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.{DateInput, Fieldset, Hint, Legend, PrefixOrSuffix, Text}
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.forms.MoneyYouPaidInAdvanceToLandlordForm
import uk.gov.hmrc.ngrraldfrontend.models.{NGRDate, NGRMonthYear, NormalMode}
import uk.gov.hmrc.ngrraldfrontend.views.html.MoneyYouPaidInAdvanceToLandlordView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText

class MoneyYouPaidInAdvanceToLandlordViewSpec extends ViewBaseSpec {
  lazy val view: MoneyYouPaidInAdvanceToLandlordView = inject[MoneyYouPaidInAdvanceToLandlordView]

  object Strings {
    val heading = "Money you paid in advance to the landlord"
    val title = s"$heading - GOV.UK"
    val label1 = "How much money did you pay in advance to the landlord (excluding VAT)?"
    val label2 = "When did you pay the money?"
    val hint2 = "For example, 27 6 2026"
    val dayInputLabel = "Day"
    val monthInputLabel = "Month"
    val yearInputLabel = "Year"
    val saveAndContinue = "Continue"
  }

  object Selectors {
    val navTitle = "head > title"
    val heading = "#main-content > div > div.govuk-grid-column-two-thirds > form > h1"
    val label1 = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(3) > h1"
    val label2 = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(4) > fieldset > legend"
    val hint2 = "#date-hint"
    val startPayingDateDayInputLabel = "#date > div:nth-child(1) > div > label"
    val startPayingDateMonthInputLabel = "#date > div:nth-child(2) > div > label"
    val startPayingDateYearInputLabel = "#date > div:nth-child(3) > div > label"
    val saveAndContinue = "#continue"
  }

  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"

  val form = MoneyYouPaidInAdvanceToLandlordForm.form.fillAndValidate(MoneyYouPaidInAdvanceToLandlordForm(amount = BigDecimal(1000), date = NGRDate(day = "1", month = "1", year = "2020")))
  val mockInputText: InputText = inject[InputText]
  def generateInputText(form: Form[MoneyYouPaidInAdvanceToLandlordForm], inputFieldName: String)(implicit messages: Messages): HtmlFormat.Appendable = {
    mockInputText(
      form = form,
      id = inputFieldName,
      name = inputFieldName,
      label = messages(s"moneyYouPaidInAdvanceToLandlord.label.1"),
      headingMessageArgs = Seq("govuk-fieldset__legend govuk-fieldset__legend--s"),
      isPageHeading = true,
      isVisible = true,
      classes = Some("govuk-input govuk-input--width-5"),
      prefix = Some(PrefixOrSuffix(content = Text("£")))
    )
  }

  def dateInput()(implicit messages: Messages): DateInput = DateInput(
    id = "date",
    namePrefix = Some("moneyYouPaidInAdvanceToLandlord"),
    fieldset = Some(Fieldset(
      legend = Some(Legend(
        content = Text(messages("moneyYouPaidInAdvanceToLandlord.date.label.2")),
        classes = "govuk-fieldset__legend--s",
        isPageHeading = false
      ))
    )),
    hint = Some(Hint(
      id = Some("moneyYouPaidInAdvanceToLandlord.date.hint.2"),
      content = Text(messages("moneyYouPaidInAdvanceToLandlord.date.hint.2"))
    ))
  )

  val howMuch: HtmlFormat.Appendable = generateInputText(form, "howMuch")
  val date: DateInput = dateInput()

  "MoneyYouPaidInAdvanceToLandlordView" must {
    val moneyYouPaidInAdvanceToLandlordView = view(form, address, howMuch, date, NormalMode)
    lazy implicit val document: Document = Jsoup.parse(moneyYouPaidInAdvanceToLandlordView.body)
    val htmlApply = view.apply(form, address, howMuch, date, NormalMode).body
    val htmlRender = view.render(form, address, howMuch, date, NormalMode, request, messages, mockConfig).body
    lazy val htmlF = view.f(form, address, howMuch, date,  NormalMode)

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

    "show the correct title" in {
      elementText(Selectors.navTitle) mustBe Strings.title
    }


    "show correct label for how much field" in {
      elementText(Selectors.label1) mustBe Strings.label1
    }

    "show correct label for day, month and year fields" in {
      elementText(Selectors.label2) mustBe Strings.label2
    }

    "show correct hint for the day, month and year fields" in {
      elementText(Selectors.hint2) mustBe Strings.hint2
    }

    "show the correct start paying date day field label" in {
      elementText(Selectors.startPayingDateDayInputLabel) mustBe Strings.dayInputLabel
    }

    "show the correct start paying date month field label" in {
      elementText(Selectors.startPayingDateMonthInputLabel) mustBe Strings.monthInputLabel
    }

    "show the correct start paying date year field label" in {
      elementText(Selectors.startPayingDateYearInputLabel) mustBe Strings.yearInputLabel
    }

    "show correct continue button" in {
      elementText(Selectors.saveAndContinue) mustBe Strings.saveAndContinue
    }
  }
}
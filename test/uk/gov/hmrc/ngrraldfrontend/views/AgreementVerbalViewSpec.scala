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
import uk.gov.hmrc.govukfrontend.views.Aliases.{Legend, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.dateinput.DateInput
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.Fieldset
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.Radios
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.{NGRDate, NormalMode}
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.AgreementVerbalForm
import uk.gov.hmrc.ngrraldfrontend.views.html.AgreementVerbalView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.DateTextFields

class AgreementVerbalViewSpec extends ViewBaseSpec {
  lazy val view: AgreementVerbalView = inject[AgreementVerbalView]
  val dateTextFields: DateTextFields = inject[DateTextFields]
  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"

  val heading = "Agreement"
  val title = s"$heading - GOV.UK"
  val startDateTile = "When did your agreement start?"
  val startDateHint = "For example, 27 6 2026"
  val radioTitle = "Is your agreement open-ended?"
  val radioHint = "An open-ended agreement does not have a date the agreement expires on"
  val yesRadio = "Yes, it is open-ended"
  val noRadio = "No, it runs for a defined term or has an agreed end date"
  val endDateTitle = "When does your agreement end?"
  val endDateHint = "For example, 27 6 2036"
  val dayInputLabel = "Day"
  val monthInputLabel = "Month"
  val yearInputLabel = "Year"
  val saveButton = "Continue"

  private val form: Form[AgreementVerbalForm] = AgreementVerbalForm.form.fillAndValidate(AgreementVerbalForm("No", NGRDate("30", "4", "2025"), None))
  private val ngrRadioButtons: Seq[NGRRadioButtons] =
    Seq(
      NGRRadioButtons("agreementVerbal.yes", Yes),
      NGRRadioButtons(
        radioContent = "agreementVerbal.no",
        radioValue = No,
        conditionalHtml = Some(dateTextFields(form, DateInput(id = "agreementEndDate",
          fieldset = Some(Fieldset(legend = Some(Legend(content = Text(messages("agreementVerbal.endDate.title")), classes = "govuk-fieldset__legend--s")))),
          hint = Some(Hint(content = Text(messages("agreementVerbal.endDate.hint")))))))
      )
    )
  private val ngrRadio: NGRRadio = NGRRadio(NGRRadioName("agreement-verbal-radio"), ngrRadioButtons,
    Some(Legend(content = Text(messages("agreementVerbal.radio.title")), classes = "govuk-fieldset__legend--m", isPageHeading = true)),
    Some("agreementVerbal.radio.hint"))
  private val radio: Radios = buildRadios(form, ngrRadio)

  object Selectors {
    val navTitle = "head > title"
    val heading = "#main-content > div > div.govuk-grid-column-two-thirds > form > h1"
    val addressCaption = "#main-content > div > div.govuk-grid-column-two-thirds > form > span"
    val startDateTile = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(3) > fieldset > legend"
    val startDateHint = "#agreementStartDate-hint"
    val startDateDayInputLabel = "#agreementStartDate > div:nth-child(1) > div > label"
    val startDateMonthInputLabel = "#agreementStartDate > div:nth-child(2) > div > label"
    val startDateYearInputLabel = "#agreementStartDate > div:nth-child(3) > div > label"
    val radioTitle = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(4) > fieldset > legend > h1"
    val radioHint = "#agreement-verbal-radio-hint"
    val yesRadio = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(4) > fieldset > div.govuk-radios.govuk-radios > div:nth-child(1) > label"
    val noRadio = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(4) > fieldset > div.govuk-radios.govuk-radios > div:nth-child(2) > label"
    val endDateTitle = "#conditional-agreement-verbal-radio-2 > div > fieldset > legend"
    val endDateHint = "#agreementEndDate-hint"
    val endDateDayInputLabel = "#agreementEndDate > div:nth-child(1) > div > label"
    val endDateMonthInputLabel = "#agreementEndDate > div:nth-child(2) > div > label"
    val endDateYearInputLabel = "#agreementEndDate > div:nth-child(3) > div > label"
    val saveButton = "#continue"
  }

  "AgreementVerbalView" must {
    val agreementVerbalView = view(form, radio, address, NormalMode)
    lazy implicit val document: Document = Jsoup.parse(agreementVerbalView.body)
    val htmlApply = view.apply(form, radio, address, NormalMode).body
    val htmlRender = view.render(form, radio, address, NormalMode, request, messages, mockConfig).body
    lazy val htmlF = view.f(form, radio, address, NormalMode)

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

    "show the correct agreement start date title" in {
      elementText(Selectors.startDateTile) mustBe startDateTile
    }

    "show the correct agreement start date hint" in {
      elementText(Selectors.startDateHint) mustBe startDateHint
    }

    "show the correct agreement start date day field label" in {
      elementText(Selectors.startDateDayInputLabel) mustBe dayInputLabel
    }

    "show the correct agreement start date month field label" in {
      elementText(Selectors.startDateMonthInputLabel) mustBe monthInputLabel
    }

    "show the correct agreement start date year field label" in {
      elementText(Selectors.startDateYearInputLabel) mustBe yearInputLabel
    }

    "show the correct radio buttons title" in {
      htmlF.toString()
      elementText(Selectors.radioTitle) mustBe radioTitle
    }

    "show the correct radio buttons hint" in {
      elementText(Selectors.radioHint) mustBe radioHint
    }

    "show the correct yes radio button label" in {
      elementText(Selectors.yesRadio) mustBe yesRadio
    }

    "show the correct no radio button label" in {
      elementText(Selectors.noRadio) mustBe noRadio
    }

    "show the correct agreement end date day field label" in {
      elementText(Selectors.endDateDayInputLabel) mustBe dayInputLabel
    }

    "show the correct agreement end date month field label" in {
      elementText(Selectors.endDateMonthInputLabel) mustBe monthInputLabel
    }

    "show the correct agreement end date year field label" in {
      elementText(Selectors.endDateYearInputLabel) mustBe yearInputLabel
    }

    "show the correct save button" in {
      elementText(Selectors.saveButton) mustBe saveButton
    }
  }
}


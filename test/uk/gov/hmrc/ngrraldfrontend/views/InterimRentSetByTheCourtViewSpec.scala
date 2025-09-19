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
import uk.gov.hmrc.govukfrontend.views.Aliases.{PrefixOrSuffix, Text}
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.{NGRMonthYear, NormalMode}
import uk.gov.hmrc.ngrraldfrontend.models.forms.InterimRentSetByTheCourtForm
import uk.gov.hmrc.ngrraldfrontend.views.html.InterimRentSetByTheCourtView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText

class InterimRentSetByTheCourtViewSpec extends ViewBaseSpec {
  lazy val view: InterimRentSetByTheCourtView = inject[InterimRentSetByTheCourtView]

  object Strings {
    val heading = "Interim rent set by the court"
    val label1 = "How much was the interim rent?"
    val label2 = "When did you start paying the interim rent?"
    val hint2 = "For example, 6 2026"
    val saveAndContinue = "Continue"
  }

  object Selectors {
    val heading = "#main-content > div > div.govuk-grid-column-two-thirds > form > h1"
    val label1 = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(3) > h1"
    val label2 = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(4) > fieldset > legend"
    val hint2 = "#date-hint"
    val saveAndContinue = "#continue"
  }

  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"

  val form = InterimRentSetByTheCourtForm.form.fillAndValidate(InterimRentSetByTheCourtForm(amount = BigDecimal(1000), date = NGRMonthYear(month = "1", year = "2020")))
  val mockInputText: InputText = inject[InputText]
  def generateInputText(form: Form[InterimRentSetByTheCourtForm], inputFieldName: String)(implicit messages: Messages): HtmlFormat.Appendable = {
    mockInputText(
      form = form,
      id = inputFieldName,
      name = inputFieldName,
      label = messages(s"interimRentSetByTheCourt.label.1"),
      headingMessageArgs = Seq("govuk-fieldset__legend govuk-fieldset__legend--s"),
      isPageHeading = true,
      isVisible = true,
      classes = Some("govuk-input govuk-input--width-5"),
      prefix = Some(PrefixOrSuffix(content = Text("£")))
    )
  }

  val howMuch: HtmlFormat.Appendable = generateInputText(form, "howMuch")

  "InterimRentSetByTheCourtView" must {
    val interimRentSetByTheCourtView = view(form, address, howMuch, NormalMode)
    lazy implicit val document: Document = Jsoup.parse(interimRentSetByTheCourtView.body)
    val htmlApply = view.apply(form, address, howMuch, NormalMode).body
    val htmlRender = view.render(form, address, howMuch, NormalMode, request, messages, mockConfig).body
    lazy val htmlF = view.f(form, address, howMuch, NormalMode)

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

    "show correct label for how much field" in {
      elementText(Selectors.label1) mustBe Strings.label1
    }

    "show correct label for month and year fields" in {
      elementText(Selectors.label2) mustBe Strings.label2
    }

    "show correct hint for the month and year fields" in {
      elementText(Selectors.hint2) mustBe Strings.hint2
    }

    "show correct continue button" in {
      elementText(Selectors.saveAndContinue) mustBe Strings.saveAndContinue
    }
  }
}


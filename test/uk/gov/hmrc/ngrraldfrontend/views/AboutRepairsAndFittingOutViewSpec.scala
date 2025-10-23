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
import uk.gov.hmrc.ngrraldfrontend.models.forms.AboutRepairsAndFittingOutForm as FormModel
import uk.gov.hmrc.ngrraldfrontend.views.html.AboutRepairsAndFittingOutView

class AboutRepairsAndFittingOutViewSpec extends ViewBaseSpec {

  lazy val view: AboutRepairsAndFittingOutView = inject[AboutRepairsAndFittingOutView]

  object Strings {
    val heading = "About repairs and fitting out"
    val label1 = "How much did the repairs or fitting out cost (excluding VAT)?"
    val label2 = "When did you do the repairs or fitting out?"
    val hint2 = "You only need to give us an approximate date, for example, 9 2025"
    val saveAndContinue = "Continue"
  }

  object Selectors {
    val heading = "h1.govuk-heading-l"
    val label1 = "label[for=cost]"
    val label2 = "legend.govuk-fieldset__legend"
    val hint2 = "#date-hint"
    val saveAndContinue = "#continue"
  }

  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"

  val form: Form[FormModel] = FormModel.form.fill(
    FormModel(
      cost = BigDecimal("1000.00"),
      date = NGRMonthYear("9", "2025")
    )
  )

  "AboutRepairsAndFittingOutView" must {
    val renderedView = view(form, address, NormalMode)
    implicit lazy val document: Document = Jsoup.parse(renderedView.body)

    val htmlApply = view.apply(form, address, NormalMode).body
    val htmlRender = view.render(form, address, NormalMode, request, messages, mockConfig).body
    val htmlF = view.f(form, address, NormalMode)

    "htmlF is not empty" in {
      htmlF.toString must not be empty
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

    "show correct label for cost field" in {
      elementText(Selectors.label1) mustBe Strings.label1
    }

    "show correct label for date field" in {
      elementText(Selectors.label2) mustBe Strings.label2
    }

    "show correct hint for the date field" in {
      elementText(Selectors.hint2) mustBe Strings.hint2
    }

    "show correct continue button" in {
      elementText(Selectors.saveAndContinue) mustBe Strings.saveAndContinue
    }
  }
}


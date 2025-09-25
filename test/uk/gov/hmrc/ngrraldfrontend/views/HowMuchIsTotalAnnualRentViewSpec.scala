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
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.NormalMode
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.forms.HowMuchIsTotalAnnualRentForm
import uk.gov.hmrc.ngrraldfrontend.views.html.HowMuchIsTotalAnnualRentView

class HowMuchIsTotalAnnualRentViewSpec extends ViewBaseSpec {
  lazy val view: HowMuchIsTotalAnnualRentView = inject[HowMuchIsTotalAnnualRentView]

  object Strings {
    val heading = "How much is your total annual rent?"
    val p1 = "This is the amount you pay each year (excluding VAT) even if:"
    val b1 = "you pay monthly or quarterly"
    val b2 = "you get a rent-free period"
    val p2 = "For example, you get a rent free period of 3 months and pay £300 a month. Your annual rent is £300 multiplied by 12 months. This is because you ignore the rent-free period."
    val saveAndContinue = "Continue"
  }

  object Selectors {
    val heading = "#main-content > div > div.govuk-grid-column-two-thirds > form > h1"
    val p1 = "#main-content > div > div.govuk-grid-column-two-thirds > form > p:nth-child(3)"
    val b1 = "#main-content > div > div.govuk-grid-column-two-thirds > form > ul > li:nth-child(1)"
    val b2 = "#main-content > div > div.govuk-grid-column-two-thirds > form > ul > li:nth-child(2)"
    val p2 = "#main-content > div > div.govuk-grid-column-two-thirds > form > p:nth-child(6)"
    val saveAndContinue = "#continue"
  }

  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"

  val form = HowMuchIsTotalAnnualRentForm.form.fillAndValidate(HowMuchIsTotalAnnualRentForm(10000))
  
  "TellUsAboutYourNewAgreementView" must {
    val howMuchIsTotalAnnualRentView = view(form, address, NormalMode)
    lazy implicit val document: Document = Jsoup.parse(howMuchIsTotalAnnualRentView.body)
    val htmlApply = view.apply(form, address, NormalMode).body
    val htmlRender = view.render(form, address, NormalMode, request, messages, mockConfig).body
    lazy val htmlF = view.f(form, address, NormalMode)

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

    "show correct paragraph 1" in {
      elementText(Selectors.p1) mustBe Strings.p1
    }

    "show correct bullet point 1" in {
      elementText(Selectors.b1) mustBe Strings.b1
    }

    "show correct bullet point 2" in {
      elementText(Selectors.b2) mustBe Strings.b2
    }

    "show correct paragraph 2" in {
      elementText(Selectors.p2) mustBe Strings.p2
    }
    
    "show correct continue button" in {
      elementText(Selectors.saveAndContinue) mustBe Strings.saveAndContinue
    }
  }
}

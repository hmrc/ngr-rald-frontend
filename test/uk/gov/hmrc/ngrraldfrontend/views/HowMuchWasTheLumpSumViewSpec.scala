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
import uk.gov.hmrc.ngrraldfrontend.models.forms.HowMuchWasTheLumpSumForm
import uk.gov.hmrc.ngrraldfrontend.views.html.HowMuchWasTheLumpSumView

class HowMuchWasTheLumpSumViewSpec extends ViewBaseSpec {
  lazy val view: HowMuchWasTheLumpSumView = inject[HowMuchWasTheLumpSumView]

  object Strings {
    val heading = "How much was the lump sum?"
    val saveAndContinue = "Continue"
  }

  object Selectors {
    val heading = "#main-content > div > div.govuk-grid-column-two-thirds > form > h1"
    val textInput = "#how–much–was–the–lump–sum-value"
    val saveAndContinue = "#continue"
  }

  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"

  val form = HowMuchWasTheLumpSumForm.form.fillAndValidate(HowMuchWasTheLumpSumForm(10000))
  
  "TellUsAboutYourNewAgreementView" must {
    val HowMuchWasTheLumpSumView = view(form, address, NormalMode)
    lazy implicit val document: Document = Jsoup.parse(HowMuchWasTheLumpSumView.body)
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

    "show correct textInput" in {
      elementText(Selectors.textInput) contains("£")
    }
    
    "show correct continue button" in {
      elementText(Selectors.saveAndContinue) mustBe Strings.saveAndContinue
    }
  }
}

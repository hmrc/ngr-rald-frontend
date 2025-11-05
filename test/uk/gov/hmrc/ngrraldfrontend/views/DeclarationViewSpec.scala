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
import uk.gov.hmrc.ngrraldfrontend.views.html.DeclarationView

class DeclarationViewSpec extends ViewBaseSpec {
  lazy val view: DeclarationView = inject[DeclarationView]

  object Strings {
    val headingNewAgreement = "Declaration"
    val p1 = "By submitting these details, you declare that to the best of your knowledge the information given is correct and complete."
    val continue = "Accept and send"
  }

  object Selectors {
    val heading = "#main-content > div > div.govuk-grid-column-two-thirds > form > h1"
    val p1 = "#main-content > div > div.govuk-grid-column-two-thirds > form > p"
    val continue = "#continue"
  }

  "DeclarationView" must {
    val declarationView = view()
    lazy implicit val document: Document = Jsoup.parse(declarationView.body)
    val htmlApply = view.apply().body
    val htmlRender = view.render(request, messages, mockConfig).body
    lazy val htmlF = view.f()

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
      elementText(Selectors.heading) mustBe Strings.headingNewAgreement
    }

    "show correct p1" in {
      elementText(Selectors.p1) mustBe Strings.p1
    }

    "show correct continue button" in {
      elementText(Selectors.continue) mustBe Strings.continue
    }
  }
}

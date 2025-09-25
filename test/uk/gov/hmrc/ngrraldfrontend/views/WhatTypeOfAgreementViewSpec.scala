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
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.Radios
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.NormalMode
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.WhatTypeOfAgreementForm
import uk.gov.hmrc.ngrraldfrontend.views.html.WhatTypeOfAgreementView

class WhatTypeOfAgreementViewSpec extends ViewBaseSpec {
  lazy val view: WhatTypeOfAgreementView = inject[WhatTypeOfAgreementView]

  object Strings {
    val heading = "What type of agreement do you have?"
    val radio1 = "Lease or tenancy agreement"
    val radio2 = "Licence or other type of written agreement"
    val radio3 = "Verbal agreement"
    val continue = "Continue"
  }

  object Selectors {
    val heading = "#main-content > div > div > form > h1"
    val radio1 = "#main-content > div > div > form > div > div > div:nth-child(1) > label"
    val radio2 = "#main-content > div > div > form > div > div > div:nth-child(2) > label"
    val radio3 = "#main-content > div > div > form > div > div > div:nth-child(3) > label"
    val continue = "#continue"
  }

  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"
  private val leaseOrTenancyButton: NGRRadioButtons = NGRRadioButtons("Lease or tenancy agreement", LeaseOrTenancy)
  private val writtenAgreementButton: NGRRadioButtons = NGRRadioButtons("Licence or other type of written agreement", Written)
  private val verbalAgreementButton: NGRRadioButtons = NGRRadioButtons("Verbal agreement", Verbal)
  private val ngrRadio: NGRRadio = NGRRadio(NGRRadioName("what-type-of-agreement-radio"), Seq(leaseOrTenancyButton, writtenAgreementButton, verbalAgreementButton))
  val form = WhatTypeOfAgreementForm.form.fillAndValidate(WhatTypeOfAgreementForm("Verbal agreement"))
  val radio: Radios = buildRadios(form, ngrRadio)

  "TellUsAboutYourNewAgreementView" must {
    val whatTypeOfAgreementView = view(address, form, radio, NormalMode)
    lazy implicit val document: Document = Jsoup.parse(whatTypeOfAgreementView.body)
    val htmlApply = view.apply(address, form, radio, NormalMode).body
    val htmlRender = view.render(address, form, radio, NormalMode, request, messages, mockConfig).body
    lazy val htmlF = view.f(address, form, radio, NormalMode)

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

    "show correct radio 1" in {
      elementText(Selectors.radio1) mustBe Strings.radio1
    }

    "show correct radio 2" in {
      elementText(Selectors.radio2) mustBe Strings.radio2
    }

    "show correct radio 3" in {
      elementText(Selectors.radio3) mustBe Strings.radio3
    }

    "show correct continue button" in {
      elementText(Selectors.continue) mustBe Strings.continue
    }
  }
}

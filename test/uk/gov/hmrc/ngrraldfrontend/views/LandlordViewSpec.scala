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
import uk.gov.hmrc.ngrraldfrontend.models.forms.LandlordForm
import uk.gov.hmrc.ngrraldfrontend.views.html.LandlordView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.NGRCharacterCountComponent

class LandlordViewSpec extends ViewBaseSpec {
  lazy val view: LandlordView = inject[LandlordView]
  lazy val ngrCharacterCountComponent: NGRCharacterCountComponent = inject[NGRCharacterCountComponent]

  object Strings {
    val heading = "Landlord"
    val textInputLabel = "What is the landlord's full name?"
    val radioLabel = "What is your relationship with the landlord?"
    val radio1 = "Yes"
    val radio2 = "No"
    val continue = "Continue"


    object Selectors {
      val heading = "#main-content > div > div > form > h1"
      val textInputLabel = "#main-content > div > div > form > div:nth-child(4) > h1 > label"
      val radioLabel = "#main-content > div > div > form > div:nth-child(5) > fieldset > legend > h1"
      val radio1 = "#main-content > div > div > form > div:nth-child(5) > fieldset > div > div:nth-child(1) > label"
      val radio2 = "#main-content > div > div > form > div:nth-child(5) > fieldset > div > div:nth-child(2) > label"
      val continue = "#continue"
    }

    val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"
    val form = LandlordForm.form.fillAndValidate(LandlordForm(landlordName = "Bob", hasRelationship = "false", None))
    private val ngrRadio: NGRRadio = LandlordForm.landlordRadio(form, ngrCharacterCountComponent)
    val radio: Radios = buildRadios(form, ngrRadio)

    "LandlordView" must {
      val landlordView = view(address, form, radio, NormalMode)
      lazy implicit val document: Document = Jsoup.parse(landlordView.body)
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

      "show correct text input label" in {
        elementText(Selectors.textInputLabel) mustBe Strings.textInputLabel
      }

      "show correct radio label" in {
        elementText(Selectors.radioLabel) mustBe Strings.radioLabel
      }

      "show correct radio 1" in {
        elementText(Selectors.radio1) mustBe Strings.radio1
      }

      "show correct radio 2" in {
        elementText(Selectors.radio2) mustBe Strings.radio2
      }

      "show correct continue button" in {
        elementText(Selectors.continue) mustBe Strings.continue
      }
    }
  }
}


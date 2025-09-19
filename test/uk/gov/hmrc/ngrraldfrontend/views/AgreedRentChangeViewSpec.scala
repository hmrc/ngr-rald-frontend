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
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.Radios
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.NormalMode
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.{buildRadios, noButton, yesButton}
import uk.gov.hmrc.ngrraldfrontend.models.components.{LeaseOrTenancy, NGRRadio, NGRRadioButtons, NGRRadioName, NavBarContents, NavBarCurrentPage, NavBarPageContents, NavigationBarContent, Verbal, Written, Yes}
import uk.gov.hmrc.ngrraldfrontend.models.forms.{AgreedRentChangeForm, WhatTypeOfAgreementForm}
import uk.gov.hmrc.ngrraldfrontend.views.html.AgreedRentChangeView

class AgreedRentChangeViewSpec extends ViewBaseSpec {
  lazy val view:  AgreedRentChangeView = inject[ AgreedRentChangeView]

  object Strings {
    val heading = "Have you agreed in advance with the landlord when and by how much rent goes up?"
    val p1 = "For example, the agreement says that the rent will increase from £5,000 to £7,000 a year after 12 months. This is often known as a stepped rent."
    val radio1 = "Yes"
    val radio2 = "No"
    val continue = "Continue"
  }

  object Selectors {
    val heading = "#main-content > div > div.govuk-grid-column-two-thirds > form > h1"
    val p1 = "#main-content > div > div.govuk-grid-column-two-thirds > form > p"
    val radio1 = "#main-content > div > div.govuk-grid-column-two-thirds > form > div > div > div:nth-child(1) > label"
    val radio2 = "#main-content > div > div.govuk-grid-column-two-thirds > form > div > div > div:nth-child(2) > label"
    val continue = "#continue"
  }

  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"
  private val ngrRadio: NGRRadio = NGRRadio(NGRRadioName(AgreedRentChangeForm.agreedRentChangeRadio), Seq(yesButton, noButton))
  val form = AgreedRentChangeForm.form.fillAndValidate(AgreedRentChangeForm("Yes"))
  val radio: Radios = buildRadios(form, ngrRadio)

  "TellUsAboutYourNewAgreementView" must {
    val agreedRentChangeView = view(form, radio, address, NormalMode)
    lazy implicit val document: Document = Jsoup.parse(agreedRentChangeView.body)
    val htmlApply = view.apply(form, radio, address, NormalMode).body
    val htmlRender = view.render(form, radio, address, NormalMode, request, messages, mockConfig).body
    lazy val htmlF = view.f(form, radio, address, NormalMode)

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

    "show correct continue button" in {
      elementText(Selectors.continue) mustBe Strings.continue
    }

    "serialize to JSON correctly" in {
      val form = AgreedRentChangeForm(radioValue = "yes")
      val json = Json.toJson(form)

      json mustBe Json.obj(
        "radioValue" -> "yes",
      )
    }

    "deserialize from JSON correctly" in {
      val json = Json.obj(
        "radioValue" -> "yes",
      )
      val result = json.validate[AgreedRentChangeForm]

      result.isSuccess mustBe true
      result.get mustBe AgreedRentChangeForm("yes")
    }

    "fail deserialization if value is missing" in {
      val json = Json.obj()
      val result = json.validate[AgreedRentChangeForm]

      result.isError mustBe true
    }
  }
}

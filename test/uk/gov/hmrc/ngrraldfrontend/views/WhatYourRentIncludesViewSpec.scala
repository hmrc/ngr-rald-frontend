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
import uk.gov.hmrc.govukfrontend.views.Aliases.{Legend, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.Radios
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.forms.WhatYourRentIncludesForm
import uk.gov.hmrc.ngrraldfrontend.views.html.WhatYourRentIncludesView

class WhatYourRentIncludesViewSpec  extends ViewBaseSpec {
  lazy val view: WhatYourRentIncludesView = inject[WhatYourRentIncludesView]

  object Strings {
    val heading = "What your rent includes"
    val radio1Label = "Does your rent include any living accommodation?"
    val radio1Hint = "For example, a flat over your shop or caretaker's accommodation"
    val radio2Label = "Is the rent you pay only for part of this address?"
    val radio2Indent = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"
    val radio2Hint = "For example, the address says 'units 1 and 2', and you pay rent for unit 1 only"
    val radio3Label = "Is your rent for an empty shell building?"
    val radio3Hint = "For example, a building without internal walls, toilets, heating or lighting"
    val radio4Label = "Does your rent include business rates?"
    val radio5Label = "Does your rent include water charges?"
    val radio6Label = "Does your rent include service charges?"
    val radio6Hint = "For example, heating, lighting and maintenance of shared areas"
    val continue = "Continue"
  }

  object Selectors {
    val heading = "#main-content > div > div > form > div > div > h1"
    val radio1Label = "#main-content > div > div > form > div > div > div:nth-child(3) > fieldset > legend > h1"
    val radio1Hint = "#livingAccommodationRadio-hint"
    val radio2Label = "#main-content > div > div > form > div > div > p1"
    val radio2Indent = "#main-content > div > div > form > div > div > div.govuk-inset-text"
    val radio2Hint = "#rentPartAddressRadio-hint"
    val radio3Label = "#main-content > div > div > form > div > div > div:nth-child(7) > fieldset > legend > h1"
    val radio3Hint = "#rentEmptyShellRadio-hint"
    val radio4Label = "#main-content > div > div > form > div > div > div:nth-child(8) > fieldset > legend > h1"
    val radio5Label = "#main-content > div > div > form > div > div > div:nth-child(9) > fieldset > legend > h1"
    val radio6Label = "#main-content > div > div > form > div > div > div:nth-child(10) > fieldset > legend > h1"
    val radio6Hint = "#rentIncServiceRadio-hint"
    val continue = "#continue"
  }

  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"
  private val ngrRadio1: NGRRadio = NGRRadio(
    NGRRadioName("livingAccommodationRadio"),
    Seq(
      NGRRadioButtons(radioContent = "service.yes", radioValue = livingAccommodationYes),
      NGRRadioButtons(radioContent = "service.no", radioValue = livingAccommodationNo)
    ),
    Some(Legend(content = Text(messages("whatYourRentIncludes.radio.1.title")), classes = "govuk-fieldset__legend--m", isPageHeading = true)),
    Some("whatYourRentIncludes.radio.1.hint")
  )
  private val ngrRadio2: NGRRadio =  NGRRadio(
    NGRRadioName("rentPartAddressRadio"),
    Seq(
      NGRRadioButtons(radioContent = "service.yes", radioValue = rentPartAddressYes),
      NGRRadioButtons(radioContent = "service.no", radioValue = rentPartAddressNo)
    ),
    hint = Some("whatYourRentIncludes.radio.2.hint")
  )
  private val ngrRadio3: NGRRadio = NGRRadio(
    NGRRadioName("rentEmptyShellRadio"),
    Seq(
      NGRRadioButtons(radioContent = "service.yes", radioValue = rentEmptyShellYes),
      NGRRadioButtons(radioContent = "service.no", radioValue = rentEmptyShellNo)
    ),
    Some(Legend(content = Text(messages("whatYourRentIncludes.radio.3.title")), classes = "govuk-fieldset__legend--m", isPageHeading = true)),
    Some("whatYourRentIncludes.radio.3.hint")
  )
  private val ngrRadio4: NGRRadio = NGRRadio(
    NGRRadioName("rentIncBusinessRatesRadio"),
    Seq(
      NGRRadioButtons(radioContent = "service.yes", radioValue = rentIncBusinessRatesYes),
      NGRRadioButtons(radioContent = "service.no", radioValue = rentIncBusinessRatesNo)
    ),
    Some(Legend(content = Text(messages("whatYourRentIncludes.radio.4.title")), classes = "govuk-fieldset__legend--m", isPageHeading = true))
  )
  private val ngrRadio5: NGRRadio = NGRRadio(
    NGRRadioName("rentIncWaterChargesRadio"),
    Seq(
      NGRRadioButtons(radioContent = "service.yes", radioValue = rentIncWaterChargesYes),
      NGRRadioButtons(radioContent = "service.no", radioValue = rentIncWaterChargesNo)
    ),
    Some(Legend(content = Text(messages("whatYourRentIncludes.radio.5.title")), classes = "govuk-fieldset__legend--m", isPageHeading = true))
  )
  private val ngrRadio6: NGRRadio =  NGRRadio(
    NGRRadioName("rentIncServiceRadio"),
    Seq(
      NGRRadioButtons(radioContent = "service.yes", radioValue = rentIncServiceYes),
      NGRRadioButtons(radioContent = "service.no", radioValue = rentIncServiceNo)
    ),
    Some(Legend(content = Text(messages("whatYourRentIncludes.radio.6.title")), classes = "govuk-fieldset__legend--m", isPageHeading = true)),
    Some("whatYourRentIncludes.radio.6.hint")
  )
  val form = WhatYourRentIncludesForm.form.fillAndValidate(WhatYourRentIncludesForm("Yes","Yes","Yes","Yes","Yes","Yes"))
  val radio1: Radios = buildRadios(form, ngrRadio1)
  val radio2: Radios = buildRadios(form, ngrRadio2)
  val radio3: Radios = buildRadios(form, ngrRadio3)
  val radio4: Radios = buildRadios(form, ngrRadio4)
  val radio5: Radios = buildRadios(form, ngrRadio5)
  val radio6: Radios = buildRadios(form, ngrRadio6)

  "WhatYourRentIncludesView" must {
    val whatYourRentIncludesView = view(form, radio1, radio2, radio3, radio4, radio5, radio6, address)
    lazy implicit val document: Document = Jsoup.parse(whatYourRentIncludesView.body)
    val htmlApply = view.apply(form, radio1, radio2, radio3, radio4, radio5, radio6, propertyAddress = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW").body
    val htmlRender = view.render(form, radio1, radio2, radio3, radio4, radio5, radio6, address, request, messages, mockConfig).body
    lazy val htmlF = view.f(form, radio1, radio2, radio3, radio4, radio5, radio6, address)

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
      elementText(Selectors.radio1Label) mustBe Strings.radio1Label
    }

    "show correct radio 1 hint" in {
      elementText(Selectors.radio1Hint) mustBe Strings.radio1Hint
    }

    "show correct radio 2" in {
      elementText(Selectors.radio2Label) mustBe Strings.radio2Label
    }

    "show correct radio 2 hint" in {
      elementText(Selectors.radio2Hint) mustBe Strings.radio2Hint
    }

    "show correct radio 2 indented text" in {
      elementText(Selectors.radio2Indent) mustBe Strings.radio2Indent
    }

    "show correct radio 3 label" in {
      elementText(Selectors.radio3Label) mustBe Strings.radio3Label
    }

    "show correct radio 3 hint" in {
      elementText(Selectors.radio3Hint) mustBe Strings.radio3Hint
    }

    "show correct radio 4 label" in {
      elementText(Selectors.radio4Label) mustBe Strings.radio4Label
    }

    "show correct radio 5 label" in {
      elementText(Selectors.radio5Label) mustBe Strings.radio5Label
    }

    "show correct radio 6 label" in {
      elementText(Selectors.radio6Label) mustBe Strings.radio6Label
    }

    "show correct radio 6 hint" in {
      elementText(Selectors.radio6Hint) mustBe Strings.radio6Hint
    }

    "show correct continue button" in {
      elementText(Selectors.continue) mustBe Strings.continue
    }
  }
}

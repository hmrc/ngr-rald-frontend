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
import uk.gov.hmrc.govukfrontend.views.Aliases.{Label, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.Radios
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.WhatIsYourRentBasedOnForm
import uk.gov.hmrc.ngrraldfrontend.views.html.WhatIsYourRentBasedOnView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.NGRCharacterCountComponent

class WhatIsYourRentBasedOnViewSpec extends ViewBaseSpec {
  lazy val view: WhatIsYourRentBasedOnView = inject[WhatIsYourRentBasedOnView]
  lazy val ngrCharacterCountComponent = inject[NGRCharacterCountComponent]
  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"

  val heading = "What is your rent based on?"
  val title = s"$heading - GOV.UK"
  val radio1 = "Open market value"
  val radio2 = "A percentage of open market value"
  val radio3 = "Turnover top-up"
  val radio4 = "A percentage of expected turnover"
  val radio5 = "Total Occupancy Cost leases (TOCs)"
  val radio6 = "Indexation"
  val radio7 = "Other"
  val radio1Hint = "This is the rent a landlord could rent the property for if, it was available to anyone"
  val radio2Hint = "This is a percentage of the rent a landlord could rent the property for if, it was available to anyone"
  val radio3Hint = "The rent is a fixed base rent with an additional payment based on a percentage of your turnover"
  val radio4Hint = "The rent paid is based on a percentage of turnover"
  val radio5Hint = "The rent is the total cost of leasing the property. It includes base rent, business rates, insurance and utilities. It also includes common area maintenance and tenant improvements"
  val radio6Hint = "The rent is reviewed according to an index (such as Retail Price Index)"
  val radio7Hint = "The rent was agreed another way"
  val otherDescLabel = "Can you tell us how your rent was agreed?"
  val saveButton = "Save and continue"

  private val form: Form[WhatIsYourRentBasedOnForm] = WhatIsYourRentBasedOnForm.form.fill(WhatIsYourRentBasedOnForm("Other", None))
  private val ngrRadioButtons: Seq[NGRRadioButtons] =
    Seq(
      NGRRadioButtons(radioContent = "whatIsYourRentBasedOn.openMarket", radioValue = OpenMarket, buttonHint = Some("whatIsYourRentBasedOn.openMarket.hint")),
      NGRRadioButtons(radioContent = "whatIsYourRentBasedOn.percentageOpenMarket", radioValue = PercentageOpenMarket, buttonHint = Some("whatIsYourRentBasedOn.percentageOpenMarket.hint")),
      NGRRadioButtons(radioContent = "whatIsYourRentBasedOn.turnover", radioValue = Turnover, buttonHint = Some("whatIsYourRentBasedOn.turnover.hint")),
      NGRRadioButtons(radioContent = "whatIsYourRentBasedOn.percentageTurnover", radioValue = PercentageTurnover, buttonHint = Some("whatIsYourRentBasedOn.percentageTurnover.hint")),
      NGRRadioButtons(radioContent = "whatIsYourRentBasedOn.totalOccupancyCost", radioValue = TotalOccupancyCost, buttonHint = Some("whatIsYourRentBasedOn.totalOccupancyCost.hint")),
      NGRRadioButtons(radioContent = "whatIsYourRentBasedOn.indexation", radioValue = Indexation, buttonHint = Some("whatIsYourRentBasedOn.indexation.hint"))
    )
  private val otherButton: NGRRadioButtons = NGRRadioButtons(
    radioContent = "whatIsYourRentBasedOn.other",
    radioValue = Other,
    buttonHint = Some("whatIsYourRentBasedOn.other.hint"),
    conditionalHtml = Some(ngrCharacterCountComponent(form,
      NGRCharacterCount(
        id = "rent-based-on-other-desc",
        name = "rent-based-on-other-desc",
        maxLength = Some(250),
        label = Label(
          classes = "govuk-label govuk-label--s",
          content = Text(Messages("whatIsYourRentBasedOn.other.desc.label"))
        )
      )))
  )
  private val ngrRadio: NGRRadio = NGRRadio(NGRRadioName("rent-based-on-radio"), ngrRadioButtons :+ otherButton)
  private val radio: Radios = buildRadios(form, ngrRadio)

  object Selectors {
    val navTitle = "head > title"
    val heading = "#main-content > div > div.govuk-grid-column-two-thirds > form > h1"
    val addressCaption = "#main-content > div > div.govuk-grid-column-two-thirds > form > span"
    val radio1 = "#main-content > div > div.govuk-grid-column-two-thirds > form > div > div > div:nth-child(1) > label"
    val radio2 = "#main-content > div > div.govuk-grid-column-two-thirds > form > div > div > div:nth-child(2) > label"
    val radio3 = "#main-content > div > div.govuk-grid-column-two-thirds > form > div > div > div:nth-child(3) > label"
    val radio4 = "#main-content > div > div.govuk-grid-column-two-thirds > form > div > div > div:nth-child(4) > label"
    val radio5 = "#main-content > div > div.govuk-grid-column-two-thirds > form > div > div > div:nth-child(5) > label"
    val radio6 = "#main-content > div > div.govuk-grid-column-two-thirds > form > div > div > div:nth-child(6) > label"
    val radio7 = "#main-content > div > div.govuk-grid-column-two-thirds > form > div > div > div:nth-child(7) > label"
    val radio1Hint = "#rent-based-on-radio-item-hint"
    val radio2Hint = "#rent-based-on-radio-2-item-hint"
    val radio3Hint = "#rent-based-on-radio-3-item-hint"
    val radio4Hint = "#rent-based-on-radio-4-item-hint"
    val radio5Hint = "#rent-based-on-radio-5-item-hint"
    val radio6Hint = "#rent-based-on-radio-6-item-hint"
    val radio7Hint = "#rent-based-on-radio-7-item-hint"
    val otherDescLabel = "#conditional-rent-based-on-radio-7 > div > label"
    val saveButton = "#continue"
  }

  "WhatIsYourRentBasedOnView" must {
    val rentBasedOnView = view(content, form, radio, address)
    lazy implicit val document: Document = Jsoup.parse(rentBasedOnView.body)
    val htmlApply = view.apply(content, form, radio, address).body
    val htmlRender = view.render(content, form, radio, address, request, messages, mockConfig).body
    lazy val htmlF = view.f(content, form, radio, address)

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

    "show the correct radio button for OpenMarket" in {
      elementText(Selectors.radio1) mustBe radio1
    }

    "show the correct radio button hint for OpenMarket" in {
      elementText(Selectors.radio1Hint) mustBe radio1Hint
    }

    "show the correct radio button for PercentageOpenMarket" in {
      elementText(Selectors.radio2) mustBe radio2
    }

    "show the correct radio button hint for PercentageOpenMarket" in {
      elementText(Selectors.radio2Hint) mustBe radio2Hint
    }

    "show the correct radio button for Turnover" in {
      elementText(Selectors.radio3) mustBe radio3
    }

    "show the correct radio button hint for Turnover" in {
      elementText(Selectors.radio3Hint) mustBe radio3Hint
    }

    "show the correct radio button for PercentageTurnover" in {
      elementText(Selectors.radio4) mustBe radio4
    }

    "show the correct radio button hint for PercentageTurnover" in {
      elementText(Selectors.radio4Hint) mustBe radio4Hint
    }

    "show the correct radio button for TotalOccupancyCost" in {
      elementText(Selectors.radio5) mustBe radio5
    }

    "show the correct radio button hint for TotalOccupancyCost" in {
      elementText(Selectors.radio5Hint) mustBe radio5Hint
    }

    "show the correct radio button for Indexation" in {
      elementText(Selectors.radio6) mustBe radio6
    }

    "show the correct radio button hint for Indexation" in {
      elementText(Selectors.radio6Hint) mustBe radio6Hint
    }

    "show the correct radio button for Other" in {
      elementText(Selectors.radio7) mustBe radio7
    }

    "show the correct radio button hint for Other" in {
      elementText(Selectors.radio7Hint) mustBe radio7Hint
    }

    "show the correct text area label for Other Description" in {
      elementText(Selectors.otherDescLabel) mustBe otherDescLabel
    }

    "show the correct save button" in {
      elementText(Selectors.saveButton) mustBe saveButton
    }
  }
}


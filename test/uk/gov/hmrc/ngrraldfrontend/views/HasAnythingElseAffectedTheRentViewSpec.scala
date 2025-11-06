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
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{Label, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.Radios
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.NormalMode
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.{buildRadios, simpleNgrRadio}
import uk.gov.hmrc.ngrraldfrontend.models.components.{Indexation, NGRCharacterCount, NGRRadio, NGRRadioButtons, NGRRadioName, OpenMarket, Other, PercentageOpenMarket, PercentageTurnover, TotalOccupancyCost, Turnover}
import uk.gov.hmrc.ngrraldfrontend.models.forms.{HasAnythingElseAffectedTheRentForm, WhatIsYourRentBasedOnForm}
import uk.gov.hmrc.ngrraldfrontend.views.html.{HasAnythingElseAffectedTheRentView, WhatIsYourRentBasedOnView}

class HasAnythingElseAffectedTheRentViewSpec extends ViewBaseSpec {
  lazy val view: HasAnythingElseAffectedTheRentView= inject[HasAnythingElseAffectedTheRentView]
  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"

  val heading = "Has anything else affected the rent?"
  val title = s"$heading - GOV.UK"
  val radio1 = "Yes"
  val reasonLabel = "Can you tell us what else has affected the rent?"
  val radio2 = "No"
  val saveButton = "Continue"

  private val form: Form[HasAnythingElseAffectedTheRentForm] = HasAnythingElseAffectedTheRentForm.form.fillAndValidate(HasAnythingElseAffectedTheRentForm("true", None))

  private def reasonConditionalHtml(form: Form[HasAnythingElseAffectedTheRentForm])(implicit messages: Messages): Option[Html] =
    Some(mockNGRCharacterCountComponent(form,
      NGRCharacterCount(
        id = HasAnythingElseAffectedTheRentForm.reasonInput,
        name = HasAnythingElseAffectedTheRentForm.reasonInput,
        maxLength = Some(250),
        label = Label(
          classes = "govuk-label govuk-label--s",
          content = Text(Messages("hasAnythingElseAffectedTheRent.reason.label"))
        )
      )))

  object Selectors {
    val navTitle = "head > title"
    val heading = "#main-content > div > div.govuk-grid-column-two-thirds > form > h1"
    val addressCaption = "#main-content > div > div.govuk-grid-column-two-thirds > form > span"
    val radio1 = "#main-content > div > div.govuk-grid-column-two-thirds > form > div > div > div:nth-child(1) > label"
    val reasonLabel = "#conditional-hasAnythingElseAffectedTheRent > div > label"
    val radio2 = "#main-content > div > div.govuk-grid-column-two-thirds > form > div > div > div:nth-child(3) > label"
    val saveButton = "#continue"
  }

  private val radio: Radios =  buildRadios(form, simpleNgrRadio(HasAnythingElseAffectedTheRentForm.hasAnythingElseAffectedTheRentRadio, yesConditionalHtml = reasonConditionalHtml(form)))

  "WhatIsYourRentBasedOnView" must {
    val rentBasedOnView = view(form, radio, address, NormalMode)
    lazy implicit val document: Document = Jsoup.parse(rentBasedOnView.body)
    val htmlApply = view.apply(form, radio, address, NormalMode).body
    val htmlRender = view.render(form, radio, address, NormalMode, request, messages, mockConfig).body
    lazy val htmlF = view.f(form, radio, address, NormalMode)

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

    "show the correct radio button for Yes" in {
      elementText(Selectors.radio1) mustBe radio1
    }

    "show the correct radio button for No" in {
      elementText(Selectors.radio2) mustBe radio2
    }


    "show the correct text area label for Reason" in {
      elementText(Selectors.reasonLabel) mustBe reasonLabel
    }

    "show the correct save button" in {
      elementText(Selectors.saveButton) mustBe saveButton
    }
  }
}

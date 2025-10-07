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
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.Legend
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.Radios
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.NormalMode
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.{buildRadios, noButton, yesButton}
import uk.gov.hmrc.ngrraldfrontend.models.components.{NGRRadio, NGRRadioName}
import uk.gov.hmrc.ngrraldfrontend.models.forms.DidYouGetMoneyFromLandlordForm
import uk.gov.hmrc.ngrraldfrontend.views.html.DidYouGetMoneyFromLandlordView

class DidYouGetMoneyFromLandlordViewSpec extends ViewBaseSpec {
  lazy val view: DidYouGetMoneyFromLandlordView = inject[DidYouGetMoneyFromLandlordView]

  object Strings {
    val heading = "Did you get any money from the landlord or previous tenant to take on the lease?"
    val radio1 = "Yes"
    val radio2 = "No"
    val continue = "Continue"
  }

  object Selectors {
    val heading = "#main-content > div > div.govuk-grid-column-two-thirds > form > div > fieldset > legend > h1"
    val hint = "#main-content > div > div.govuk-grid-column-two-thirds > form > p"
    val radio1 = "#main-content > div > div.govuk-grid-column-two-thirds > form > div > fieldset > div > div:nth-child(1) > label"
    val radio2 = "#main-content > div > div.govuk-grid-column-two-thirds > form > div > fieldset > div > div:nth-child(2) > label"
    val continue = "#continue"
  }

  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"
  private val ngrRadio: NGRRadio = NGRRadio(NGRRadioName("didYouGetMoneyFromLandlord-radio-value"),ngrTitle = Some(Legend(content = Text(messages("didYouGetMoneyFromLandlord.title")), classes = "govuk-fieldset__legend--l", isPageHeading = true)) ,NGRRadioButtons = Seq(yesButton, noButton))
  val form = DidYouGetMoneyFromLandlordForm.form.fillAndValidate(DidYouGetMoneyFromLandlordForm("Yes"))
  val radio: Radios = buildRadios(form, ngrRadio)

  "DidYouGetMoneyFromLandlordView" must {
    val didYouGetMoneyFromLandlordView = view(address, form, radio, NormalMode)
    lazy implicit val document: Document = Jsoup.parse(didYouGetMoneyFromLandlordView.body)
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

    "show correct continue button" in {
      elementText(Selectors.continue) mustBe Strings.continue
    }
  }
}


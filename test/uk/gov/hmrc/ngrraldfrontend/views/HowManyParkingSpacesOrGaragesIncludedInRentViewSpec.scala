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
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.NormalMode
import uk.gov.hmrc.ngrraldfrontend.models.forms.HowManyParkingSpacesOrGaragesIncludedInRentForm
import uk.gov.hmrc.ngrraldfrontend.views.html.HowManyParkingSpacesOrGaragesIncludedInRentView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText

class HowManyParkingSpacesOrGaragesIncludedInRentViewSpec extends ViewBaseSpec {
  lazy val view: HowManyParkingSpacesOrGaragesIncludedInRentView = inject[HowManyParkingSpacesOrGaragesIncludedInRentView]

  object Strings {
    val heading = "How many parking spaces or garages are included in your rent?"
    val hint = "If spaces are in communal car park and you do not have a set number of spaces, provide an approximate number of spaces"
    val label1 = "Uncovered spaces"
    val label2 = "Covered spaces"
    val label3 = "Garages"
    val saveAndContinue = "Continue"
  }

  object Selectors {
    val heading = "#main-content > div > div.govuk-grid-column-two-thirds > form > h1"
    val hint = "#main-content > div > div.govuk-grid-column-two-thirds > form > p"
    val label1 = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(5) > h1 > label"
    val label2 = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(6) > h1 > label"
    val label3 = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(7) > h1 > label"
    val saveAndContinue = "#continue"
  }

  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"

  val form = HowManyParkingSpacesOrGaragesIncludedInRentForm.form.fillAndValidate(HowManyParkingSpacesOrGaragesIncludedInRentForm(10000,0,0))
  val mockInputText: InputText = inject[InputText]
  def generateInputText(form: Form[HowManyParkingSpacesOrGaragesIncludedInRentForm], inputFieldName: String)(implicit messages: Messages): HtmlFormat.Appendable = {
    mockInputText(
      form = form,
      id = inputFieldName,
      name = inputFieldName,
      label = messages(s"howManyParkingSpacesOrGaragesIncludedInRent.$inputFieldName.label"),
      headingMessageArgs = Seq("govuk-fieldset__legend govuk-fieldset__legend--s"),
      isPageHeading = true,
      isVisible = true,
      classes = Some("govuk-input govuk-input--width-5"),
    )
  }
  
  val uncoveredSpaces: HtmlFormat.Appendable = generateInputText(form, "uncoveredSpaces")
  val coveredSpaces: HtmlFormat.Appendable = generateInputText(form, "coveredSpaces")
  val garages: HtmlFormat.Appendable = generateInputText(form, "garages")
  
  "HowManyParkingSpacesOrGaragesIncludedInRentView" must {
    val HowManyParkingSpacesOrGaragesIncludedInRentView = view(
      form = form,
      propertyAddress =  address,
      uncoveredSpaces = uncoveredSpaces,
      coveredSpaces = coveredSpaces,
      garages = garages,
      mode = NormalMode
    )
    lazy implicit val document: Document = Jsoup.parse(HowManyParkingSpacesOrGaragesIncludedInRentView.body)
    val htmlApply = view.apply(form, address, uncoveredSpaces, coveredSpaces, garages, NormalMode).body
    val htmlRender = view.render(form, address, uncoveredSpaces, coveredSpaces, garages, NormalMode, request, messages, mockConfig).body
    lazy val htmlF = view.f(form, address, uncoveredSpaces, coveredSpaces, garages, NormalMode)

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

    "show correct hint" in {
      elementText(Selectors.hint) mustBe Strings.hint
    }

    "show correct label 1" in {
      elementText(Selectors.label1) mustBe Strings.label1
    }

    "show correct label 2" in {
      elementText(Selectors.label2) mustBe Strings.label2
    }

    "show correct label 3" in {
      elementText(Selectors.label3) mustBe Strings.label3
    }

    "show correct continue button" in {
      elementText(Selectors.saveAndContinue) mustBe Strings.saveAndContinue
    }
  }
}


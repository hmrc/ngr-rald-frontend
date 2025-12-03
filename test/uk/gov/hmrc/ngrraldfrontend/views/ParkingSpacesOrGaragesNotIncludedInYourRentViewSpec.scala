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
import uk.gov.hmrc.govukfrontend.views.Aliases.{Fieldset, Hint, Legend, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.dateinput.DateInput
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.forms.ParkingSpacesOrGaragesNotIncludedInYourRentForm
import uk.gov.hmrc.ngrraldfrontend.models.{NGRDate, NormalMode}
import uk.gov.hmrc.ngrraldfrontend.views.html.ParkingSpacesOrGaragesNotIncludedInYourRentView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText

class ParkingSpacesOrGaragesNotIncludedInYourRentViewSpec extends ViewBaseSpec {
  lazy val view: ParkingSpacesOrGaragesNotIncludedInYourRentView = inject[ParkingSpacesOrGaragesNotIncludedInYourRentView]

  object Strings {
    val heading = "Parking spaces or garages not included in your rent"
    val subheading = "How many parking spaces or garages do you pay extra for?"
    val hint = "If spaces are in communal car park and you do not have a set number of spaces. Provide an approximate number of spaces"
    val uncoveredSpacesLabel = "Uncovered spaces"
    val coveredSpacesLabel = "Covered spaces"
    val garagesLabel = "Garages"
    val totalCostTitle = "How much extra do you pay each year for parking and garages (excluding VAT)?"
    val agreementDateLabel = "When was this payment agreed for parking and garages?"
    val agreementDateHint = "For example, 27 6 2026"
    val continue = "Continue"
  }

  object Selectors {
    val heading = "#main-content > div > div.govuk-grid-column-two-thirds > form > h1"
    val subheading = "#main-content > div > div.govuk-grid-column-two-thirds > form > h2"
    val hint = "#main-content > div > div.govuk-grid-column-two-thirds > form > span:nth-child(4)"
    val uncoveredSpacesLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(6) > label"
    val coveredSpacesLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(7) > label"
    val garagesLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(8)  > label"
    val totalCostTitle = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(9) > label"
    val agreementDateLabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(10) > fieldset > legend"
    val agreementDateHint = "#agreementDate-hint"
    val continue = "#continue"
  }

  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"
  val mockInputText: InputText = inject[InputText]

  def generateInputText(form: Form[ParkingSpacesOrGaragesNotIncludedInYourRentForm], inputFieldName: String)(implicit messages: Messages): HtmlFormat.Appendable = {
    mockInputText(
      form = form,
      id = inputFieldName,
      name = inputFieldName,
      label = messages(s"howManyParkingSpacesOrGaragesIncludedInRent.$inputFieldName.label"),
      headingMessageArgs = Seq("govuk-fieldset__legend govuk-fieldset__legend--s"),
      isVisible = true,
      classes = Some("govuk-input govuk-input--width-5"),
    )
  }

  def agreementDateInput()(implicit messages: Messages): DateInput = DateInput(
    id = "agreementDate",
    namePrefix = Some("parkingSpacesOrGaragesNotIncludedInYourRent"),
    fieldset = Some(Fieldset(
      legend = Some(Legend(
        content = Text(messages("parkingSpacesOrGaragesNotIncludedInYourRent.agreementDate.label")),
        classes = "govuk-fieldset__legend--s"
      ))
    )),
    hint = Some(Hint(
      id = Some("parkingSpacesOrGaragesNotIncludedInYourRent.agreementDate.hint"),
      content = Text(messages("parkingSpacesOrGaragesNotIncludedInYourRent.agreementDate.hint"))
    ))
  )

  val form: Form[ParkingSpacesOrGaragesNotIncludedInYourRentForm] = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.fillAndValidate(ParkingSpacesOrGaragesNotIncludedInYourRentForm(
    uncoveredSpaces = 1,
    coveredSpaces = 1,
    garages = 1,
    totalCost = BigDecimal(1000.00),
    agreementDate = NGRDate(day = "01", month = "03", year = "2025")))

  private val uncoveredSpaces: HtmlFormat.Appendable = generateInputText(form, "uncoveredSpaces")
  private val coveredSpaces: HtmlFormat.Appendable = generateInputText(form, "coveredSpaces")
  private val garages: HtmlFormat.Appendable = generateInputText(form, "garages")


  "DidYouAgreeRentWithLandlordView" must {
    val parkingSpacesOrGaragesNotIncludedInYourRentView = view(form, address, uncoveredSpaces, coveredSpaces, garages, agreementDateInput(), NormalMode)
    lazy implicit val document: Document = Jsoup.parse(parkingSpacesOrGaragesNotIncludedInYourRentView.body)
    val htmlApply = view.apply(form, address, uncoveredSpaces, coveredSpaces, garages, agreementDateInput(), NormalMode).body
    val htmlRender = view.render(form, address, uncoveredSpaces, coveredSpaces, garages, agreementDateInput(), NormalMode, request, messages, mockConfig).body
    lazy val htmlF = view.f(form, address, uncoveredSpaces, coveredSpaces, garages, agreementDateInput(), NormalMode)

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

    "show correct subheading" in {
      elementText(Selectors.subheading) mustBe Strings.subheading
    }

    "show correct hint" in {
      elementText(Selectors.hint) mustBe Strings.hint
    }

    "show correct uncovered spaces label" in {
      elementText(Selectors.uncoveredSpacesLabel) mustBe Strings.uncoveredSpacesLabel
    }

    "show correct covered spaces label" in {
      elementText(Selectors.coveredSpacesLabel) mustBe Strings.coveredSpacesLabel
    }

    "show correct garages label" in {
      elementText(Selectors.garagesLabel) mustBe Strings.garagesLabel
    }

    "show correct total cost title" in {
      elementText(Selectors.totalCostTitle) mustBe Strings.totalCostTitle
    }

    "show correct agreement date label" in {
      elementText(Selectors.agreementDateLabel) mustBe Strings.agreementDateLabel
    }

    "show correct agreement date hint" in {
      elementText(Selectors.agreementDateHint) mustBe Strings.agreementDateHint
    }

    "show correct continue button" in {
      elementText(Selectors.continue) mustBe Strings.continue
    }
  }
}
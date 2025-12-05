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
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.NormalMode
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentFreePeriodForm
import uk.gov.hmrc.ngrraldfrontend.views.html.RentFreePeriodView

class RentFreePeriodViewSpec extends ViewBaseSpec {
  lazy val view: RentFreePeriodView = inject[RentFreePeriodView]
  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"

  val heading = "Rent-free period"
  val title = s"$heading - GOV.UK"
  val howManyMonthsTitle = "How many months is your rent-free period?"
  val reasonsTitle = "Can you tell us why you have a rent-free period?"
  val reasonsHint = "For example, as an incentive to sign the lease, fit out the building or do repairs"
  val charactersCount = "You can enter up to 250 characters"
  val saveButton = "Continue"

  private val form: Form[RentFreePeriodForm] = RentFreePeriodForm.form.fillAndValidate(RentFreePeriodForm(5, ""))

  object Selectors {
    val navTitle = "head > title"
    val heading = "#main-content > div > div.govuk-grid-column-two-thirds > form > h1"
    val addressCaption = "#main-content > div > div.govuk-grid-column-two-thirds > form > span"
    val howManyMonthsTitle = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(4) > label"
    val reasonsTitle = "#main-content > div > div.govuk-grid-column-two-thirds > form > div.govuk-form-group.govuk-character-count > label"
    val reasonsHint = "#reasons-hint"
    val charactersCount = "#reasons-info"
    val saveButton = "#continue"
  }

  "RentDatesAgreeStartView" must {
    val rentFreePeriodView = view(form, address, NormalMode)
    lazy implicit val document: Document = Jsoup.parse(rentFreePeriodView.body)
    val htmlApply = view.apply(form, address, NormalMode).body
    val htmlRender = view.render(form, address, NormalMode, request, messages, mockConfig).body
    lazy val htmlF = view.f(form, address, NormalMode)

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

    "show the correct how many months title" in {
      elementText(Selectors.howManyMonthsTitle) mustBe howManyMonthsTitle
    }

    "show the correct reasons title" in {
      elementText(Selectors.reasonsTitle) mustBe reasonsTitle
    }

    "show the correct reasons hint" in {
      elementText(Selectors.reasonsHint) mustBe reasonsHint
    }

    "show the correct characters count" in {
      htmlF.toString() must not be empty
      elementText(Selectors.charactersCount) mustBe charactersCount
    }

    "show the correct save button" in {
      elementText(Selectors.saveButton) mustBe saveButton
    }
  }
}


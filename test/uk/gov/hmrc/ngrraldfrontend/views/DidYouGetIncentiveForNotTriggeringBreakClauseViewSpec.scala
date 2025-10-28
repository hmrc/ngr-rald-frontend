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
import uk.gov.hmrc.ngrraldfrontend.models.forms.DidYouGetIncentiveForNotTriggeringBreakClauseForm
import uk.gov.hmrc.ngrraldfrontend.models.{DidYouGetIncentiveForNotTriggeringBreakClause, NormalMode}
import uk.gov.hmrc.ngrraldfrontend.views.html.DidYouGetIncentiveForNotTriggeringBreakClauseView

class DidYouGetIncentiveForNotTriggeringBreakClauseViewSpec extends ViewBaseSpec {
  lazy val view: DidYouGetIncentiveForNotTriggeringBreakClauseView = inject[DidYouGetIncentiveForNotTriggeringBreakClauseView]
  val form: DidYouGetIncentiveForNotTriggeringBreakClauseForm = inject[DidYouGetIncentiveForNotTriggeringBreakClauseForm]
  object Strings {
    val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"
    val title = "Did you get incentive for not triggering the break clause?"
    val lumpSumCheckBox = "Yes, I got a lump sum"
    val rentFreePeriodCheckBox = "Yes, I got a rent-free period"
    val noIncentiveCheckBox = "No, I did not get an incentive"
    val formError = "error.prefix: Select which incentives you got for not triggering the break clause, or select 'No, I did not get an incentive'"
    val continue = "Continue"
  }

  object Selectors {
    val address = "#main-content > div > div.govuk-grid-column-two-thirds > form > span"
    val title = "#main-content > div > div.govuk-grid-column-two-thirds > form > div > fieldset > legend > h1"
    val lumpSumCheckBox = "#main-content > div > div.govuk-grid-column-two-thirds > form > div > fieldset > div.govuk-checkboxes > div:nth-child(1) > label"
    val rentFreePeriodCheckBox = "#main-content > div > div.govuk-grid-column-two-thirds > form > div > fieldset > div.govuk-checkboxes > div:nth-child(2) > label"
    val noIncentiveCheckBox = "#main-content > div > div.govuk-grid-column-two-thirds > form > div > fieldset > div.govuk-checkboxes > div:nth-child(4) > label"
    val formError = "#Incentive-error"
    val continue = "#continue"
  }

  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"

  "DidYouGetIncentiveForNotTriggeringBreakClauseView" must {
    def formWithError(error: String): Form[DidYouGetIncentiveForNotTriggeringBreakClause] = form.apply().withError("incentive", error)
    val changeToUseSpaceView = view(address, formWithError("didYouGetIncentiveForNotTriggeringBreakClause.required.error"), NormalMode)
    lazy implicit val document: Document = Jsoup.parse(changeToUseSpaceView.body)

    "correctly show the address" in {
      elementText(Selectors.address) mustBe Strings.address
    }

    "show correct title" in {
      elementText(Selectors.title) mustBe Strings.title
    }

    "show correct error message" in {
      elementText(Selectors.formError) mustBe Strings.formError
    }

    "show correct lump sum check box" in {
      elementText(Selectors.lumpSumCheckBox) mustBe Strings.lumpSumCheckBox
    }

    "show correct rent free period check box" in {
      elementText(Selectors.rentFreePeriodCheckBox) mustBe Strings.rentFreePeriodCheckBox
    }

    "show correct no incentive check box" in {
      elementText(Selectors.noIncentiveCheckBox) mustBe Strings.noIncentiveCheckBox
    }

    "show correct continue button" in {
      elementText(Selectors.continue) mustBe Strings.continue
    }
  }
}


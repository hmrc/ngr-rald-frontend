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
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.Legend
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.Radios
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.{AgreementType, NormalMode}
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.components.{BuildingInsuranceLandlord, BuildingInsuranceYou, ExternalRepairsLandlord, ExternalRepairsYou, ExternalRepairsYouAndLandlord, InternalRepairsLandlord, InternalRepairsYou, InternalRepairsYouAndLandlord, LandlordYouAndLandlord, NGRRadio, NGRRadioButtons, NGRRadioName}
import uk.gov.hmrc.ngrraldfrontend.models.forms.RepairsAndInsuranceForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.RepairsAndInsuranceForm.{form, landlordButton, youAndLandlordButton, youButton}
import uk.gov.hmrc.ngrraldfrontend.views.html.RepairsAndInsuranceView

class RepairsAndInsuranceViewSpec extends ViewBaseSpec {
  lazy val view: RepairsAndInsuranceView = inject[RepairsAndInsuranceView]

  object Strings {
    val heading = "Repairs and insurance"
    val internalRepairslabel = "Who pays for internal repairs?"
    val externalRepairslabel = "Who pays for external repairs?"
    val buildingInsurancelabel = "Who pays for building insurance repairs?"
    val continue = "Continue"
  }

  object Selectors {
    val heading = "#main-content > div > div.govuk-grid-column-two-thirds > form > h1"
    val internalRepairslabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(3) > fieldset > legend > h1"
    val externalRepairslabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(4) > fieldset > legend > h1"
    val buildingInsurancelabel = "#main-content > div > div.govuk-grid-column-two-thirds > form > div:nth-child(5) > fieldset > legend > h1"
    val continue = "#continue"
  }

  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"
  val form = RepairsAndInsuranceForm.form.fillAndValidate(
    RepairsAndInsuranceForm(
      "InternalRepairsYou",
      "ExternalRepairsYou",
      "BuildingInsuranceYou"
    )
  )

  def ngrRadio(form: Form[RepairsAndInsuranceForm], radioType:String)(implicit messages: Messages): NGRRadio = {
    val buttons: Seq[NGRRadioButtons] = radioType match {
      case value if value.contains("internalRepairs") => Seq(youButton(InternalRepairsYou), landlordButton(InternalRepairsLandlord), youAndLandlordButton(InternalRepairsYouAndLandlord))
      case value if value.contains("externalRepairs") => Seq(youButton(ExternalRepairsYou), landlordButton(ExternalRepairsLandlord), youAndLandlordButton(ExternalRepairsYouAndLandlord))
      case _ => Seq(youButton(BuildingInsuranceYou), landlordButton(BuildingInsuranceLandlord), youAndLandlordButton(LandlordYouAndLandlord))
    }
    NGRRadio(
      NGRRadioName(s"repairsAndInsurance-$radioType-radio-value"),
      ngrTitle = Some(Legend(content = Text(messages(s"repairsAndInsurance.$radioType.radio.label")),
        classes = "govuk-fieldset__legend--l", isPageHeading = true)),
      NGRRadioButtons = buttons
    )
  }

  val internalRepairs: Radios = buildRadios(form, RepairsAndInsuranceForm.ngrRadio(form, "internalRepairs"))
  val externalRepairs: Radios = buildRadios(form, RepairsAndInsuranceForm.ngrRadio(form, "externalRepairs"))
  val buildingInsurance: Radios = buildRadios(form, RepairsAndInsuranceForm.ngrRadio(form, "buildingInsurance"))

  "RepairsAndInsuranceView" must {
    val repairsAndInsuranceView = view(form,internalRepairs, externalRepairs, buildingInsurance, address, NormalMode)
    lazy implicit val document: Document = Jsoup.parse(repairsAndInsuranceView.body)
    val htmlApply = view.apply(form, internalRepairs, externalRepairs, buildingInsurance, address, NormalMode).body
    val htmlRender = view.render(form,internalRepairs, externalRepairs, buildingInsurance, address, NormalMode, request, messages, mockConfig).body
    lazy val htmlF = view.f(form,internalRepairs, externalRepairs, buildingInsurance, address, NormalMode)

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

    "show correct internal Repairs label" in {
      elementText(Selectors.internalRepairslabel) mustBe Strings.internalRepairslabel
    }

    "show correct external Repairs label" in {
      elementText(Selectors.externalRepairslabel) mustBe Strings.externalRepairslabel
    }

    "show correct building Insurance label" in {
      elementText(Selectors.buildingInsurancelabel) mustBe Strings.buildingInsurancelabel
    }

    "show correct continue button" in {
      elementText(Selectors.continue) mustBe Strings.continue
    }
  }
}


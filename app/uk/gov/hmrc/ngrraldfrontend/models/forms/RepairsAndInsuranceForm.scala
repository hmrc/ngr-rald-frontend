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

package uk.gov.hmrc.ngrraldfrontend.models.forms

import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.i18n.*
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.ngrRadio
import uk.gov.hmrc.ngrraldfrontend.models.forms.DoesYourRentIncludeParkingForm.radioText
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings

final case class RepairsAndInsuranceForm(
                                          internalRepairs: String,
                                          externalRepairs: String,
                                          buildingInsurance: String
                                        )

object RepairsAndInsuranceForm extends CommonFormValidators with Mappings {
  implicit val format: OFormat[RepairsAndInsuranceForm] = Json.format[RepairsAndInsuranceForm]

  private lazy val internalRepairsRequiredError = "repairsAndInsurance.internalRepairs.radio.required.error"
  private lazy val externalRepairsRequiredError = "repairsAndInsurance.externalRepairs.radio.required.error"
  private lazy val buildingInsuranceRequiredError = "repairsAndInsurance.buildingInsurance.radio.required.error"
  val internalRepairsRadio = "repairsAndInsurance-internalRepairs-radio-value"
  val externalRepairsRadio = "repairsAndInsurance-externalRepairs-radio-value"
  val buildingInsuranceRadio = "repairsAndInsurance-buildingInsurance-radio-value"

  val messagesApi: MessagesApi = new DefaultMessagesApi()
  val lang: Lang = Lang.defaultLang
  val messages: Messages = MessagesImpl(lang, messagesApi)

  val buttons: Seq[NGRRadioButtons] = Seq(
    NGRRadioButtons(radioContent = "repairsAndInsurance.radio.you", radioValue = You),
    NGRRadioButtons(radioContent = "repairsAndInsurance.radio.landlord", radioValue = Landlord),
    NGRRadioButtons(radioContent = "repairsAndInsurance.radio.youAndLandlord", radioValue = YouAndLandlord)
  )
  
  def unapply(repairsAndInsuranceForm: RepairsAndInsuranceForm): Option[(String, String, String)] =
    Some(repairsAndInsuranceForm.internalRepairs, repairsAndInsuranceForm.externalRepairs, repairsAndInsuranceForm.buildingInsurance)

  def createRadio(radioType: String)(implicit messages: Messages): NGRRadio =
    ngrRadio(
      radioName = s"repairsAndInsurance-$radioType-radio-value",
      radioButtons = buttons,
      ngrTitle = s"repairsAndInsurance.$radioType.radio.label",
      isPageHeading = false
    )

  def form: Form[RepairsAndInsuranceForm] = {
    Form(
      mapping(
        internalRepairsRadio -> radioText(internalRepairsRequiredError),
        externalRepairsRadio -> radioText(externalRepairsRequiredError),
        buildingInsuranceRadio -> radioText(buildingInsuranceRequiredError),
      )(RepairsAndInsuranceForm.apply)(RepairsAndInsuranceForm.unapply)
    )
  }
}

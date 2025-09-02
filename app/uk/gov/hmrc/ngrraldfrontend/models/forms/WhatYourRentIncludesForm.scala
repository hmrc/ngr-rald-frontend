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

import play.api.data.Forms.mapping
import play.api.data.{Form, Forms}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Legend, Text}
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.{noButton, yesButton}
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.forms.LandlordForm.text
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings

final case class WhatYourRentIncludesForm(
                                           livingAccommodationRadio: String,
                                           rentPartAddressRadio: String,
                                           rentEmptyShellRadio: String,
                                           rentIncBusinessRatesRadio: String,
                                           rentIncWaterChargesRadio: String,
                                           rentIncServiceRadio: String,
                                         ) extends RadioEntry

object WhatYourRentIncludesForm extends CommonFormValidators with Mappings{
  implicit val format: OFormat[WhatYourRentIncludesForm] = Json.format[WhatYourRentIncludesForm]

  private lazy val livingAccommodationRadio = "livingAccommodationRadio"
  private lazy val rentPartAddressRadio = "rentPartAddressRadio"
  private lazy val rentEmptyShellRadio = "rentEmptyShellRadio"
  private lazy val rentIncBusinessRatesRadio = "rentIncBusinessRatesRadio"
  private lazy val rentIncWaterChargesRadio = "rentIncWaterChargesRadio"
  private lazy val rentIncServiceRadio = "rentIncServiceRadio"

  private lazy val livingAccommodationRadioError = "whatYourRentIncludes.radio.1.required"
  private lazy val rentPartAddressRadioError = "whatYourRentIncludes.radio.2.required"
  private lazy val rentEmptyShellRadioError = "whatYourRentIncludes.radio.3.required"
  private lazy val rentIncBusinessRatesRadioError = "whatYourRentIncludes.radio.4.required"
  private lazy val rentIncWaterChargesRadioError = "whatYourRentIncludes.radio.5.required"
  private lazy val rentIncServiceRadioError = "whatYourRentIncludes.radio.6.required"

  def ngrRadio1(implicit messages: Messages): NGRRadio =
    NGRRadio(
      NGRRadioName("livingAccommodationRadio"),
      Seq(
        NGRRadioButtons(radioContent = "service.yes", radioValue = livingAccommodationYes),
        NGRRadioButtons(radioContent = "service.no", radioValue = livingAccommodationNo)
      ),
      Some(Legend(content = Text(messages("whatYourRentIncludes.radio.1.title")), classes = "govuk-fieldset__legend--m", isPageHeading = true)),
      Some("whatYourRentIncludes.radio.1.hint")
    )

  def ngrRadio2(implicit messages: Messages): NGRRadio =
    NGRRadio(
      NGRRadioName("rentPartAddressRadio"),
      Seq(
        NGRRadioButtons(radioContent = "service.yes", radioValue = rentPartAddressYes),
        NGRRadioButtons(radioContent = "service.no", radioValue = rentPartAddressNo)
      ),
      hint = Some("whatYourRentIncludes.radio.2.hint")
    )

  def ngrRadio3(implicit messages: Messages): NGRRadio =
    NGRRadio(
      NGRRadioName("rentEmptyShellRadio"),
      Seq(
        NGRRadioButtons(radioContent = "service.yes", radioValue = rentEmptyShellYes),
        NGRRadioButtons(radioContent = "service.no", radioValue = rentEmptyShellNo)
      ),
      Some(Legend(content = Text(messages("whatYourRentIncludes.radio.3.title")), classes = "govuk-fieldset__legend--m", isPageHeading = true)),
      Some("whatYourRentIncludes.radio.3.hint")
    )

  def ngrRadio4(implicit messages: Messages): NGRRadio =
    NGRRadio(
      NGRRadioName("rentIncBusinessRatesRadio"),
      Seq(
        NGRRadioButtons(radioContent = "service.yes", radioValue = rentIncBusinessRatesYes),
        NGRRadioButtons(radioContent = "service.no", radioValue = rentIncBusinessRatesNo)
      ),
      Some(Legend(content = Text(messages("whatYourRentIncludes.radio.4.title")), classes = "govuk-fieldset__legend--m", isPageHeading = true))
    )

  def ngrRadio5(implicit messages: Messages): NGRRadio =
    NGRRadio(
      NGRRadioName("rentIncWaterChargesRadio"),
      Seq(
        NGRRadioButtons(radioContent = "service.yes", radioValue = rentIncWaterChargesYes),
        NGRRadioButtons(radioContent = "service.no", radioValue = rentIncWaterChargesNo)
      ),
      Some(Legend(content = Text(messages("whatYourRentIncludes.radio.5.title")), classes = "govuk-fieldset__legend--m", isPageHeading = true))
    )

  def ngrRadio6(implicit messages: Messages): NGRRadio =
    NGRRadio(
      NGRRadioName("rentIncServiceRadio"),
      Seq(
        NGRRadioButtons(radioContent = "service.yes", radioValue = rentIncServiceYes),
        NGRRadioButtons(radioContent = "service.no", radioValue = rentIncServiceNo)
      ),
      Some(Legend(content = Text(messages("whatYourRentIncludes.radio.6.title")), classes = "govuk-fieldset__legend--m", isPageHeading = true)),
      Some("whatYourRentIncludes.radio.6.hint")
    )

  def unapply(whatYourRentIncludesForm: WhatYourRentIncludesForm): Option[(String, String, String, String, String, String)] =
    Some((
      whatYourRentIncludesForm.livingAccommodationRadio,
      whatYourRentIncludesForm.rentPartAddressRadio,
      whatYourRentIncludesForm.rentEmptyShellRadio,
      whatYourRentIncludesForm.rentIncBusinessRatesRadio,
      whatYourRentIncludesForm.rentIncWaterChargesRadio,
      whatYourRentIncludesForm.rentIncServiceRadio
    ))

  def form: Form[WhatYourRentIncludesForm] = {
    Form(
      mapping(
      livingAccommodationRadio -> text(livingAccommodationRadioError),
      rentPartAddressRadio -> text(rentPartAddressRadioError),
      rentEmptyShellRadio -> text(rentEmptyShellRadioError),
      rentIncBusinessRatesRadio -> text(rentIncBusinessRatesRadioError),
      rentIncWaterChargesRadio -> text(rentIncWaterChargesRadioError),
      rentIncServiceRadio -> text(rentIncServiceRadioError)
      )(WhatYourRentIncludesForm.apply)(WhatYourRentIncludesForm.unapply)
    )
  }
}

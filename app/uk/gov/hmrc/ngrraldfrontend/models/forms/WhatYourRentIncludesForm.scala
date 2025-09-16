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

import play.api.data.Forms.{mapping, optional, text}
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.data.{Form, Forms}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Hint, Legend, PrefixOrSuffix, Text}
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText

import scala.util.Try

final case class WhatYourRentIncludesForm(
                                           livingAccommodationRadio: String,
                                           rentPartAddressRadio: String,
                                           rentEmptyShellRadio: String,
                                           rentIncBusinessRatesRadio: String,
                                           rentIncWaterChargesRadio: String,
                                           rentIncServiceRadio: String,
                                           bedroomNumbers: Option[String]
                                         ) extends RadioEntry

object WhatYourRentIncludesForm extends CommonFormValidators with Mappings{
  implicit val format: OFormat[WhatYourRentIncludesForm] = Json.format[WhatYourRentIncludesForm]

  private lazy val livingAccommodationRadio = "livingAccommodationRadio"
  private lazy val rentPartAddressRadio = "rentPartAddressRadio"
  private lazy val rentEmptyShellRadio = "rentEmptyShellRadio"
  private lazy val rentIncBusinessRatesRadio = "rentIncBusinessRatesRadio"
  private lazy val rentIncWaterChargesRadio = "rentIncWaterChargesRadio"
  private lazy val rentIncServiceRadio = "rentIncServiceRadio"
  private lazy val bedroomNumbers = "bedroomNumbers"

  private lazy val livingAccommodationRadioError = "whatYourRentIncludes.radio.1.required"
  private lazy val rentPartAddressRadioError = "whatYourRentIncludes.radio.2.required"
  private lazy val rentEmptyShellRadioError = "whatYourRentIncludes.radio.3.required"
  private lazy val rentIncBusinessRatesRadioError = "whatYourRentIncludes.radio.4.required"
  private lazy val rentIncWaterChargesRadioError = "whatYourRentIncludes.radio.5.required"
  private lazy val rentIncServiceRadioError = "whatYourRentIncludes.radio.6.required"

  def ngrRadio1(form: Form[WhatYourRentIncludesForm], inputText: InputText)(implicit messages: Messages): NGRRadio =
    NGRRadio(
      NGRRadioName(livingAccommodationRadio),
      Seq(
        NGRRadioButtons(
          radioContent = "service.yes",
          radioValue = livingAccommodationYes,
          conditionalHtml = Some(inputText(
            form = form,
            id = bedroomNumbers,
            name = bedroomNumbers,
            label = messages("whatYourRentIncludes.radio.1.text.title"),
            isVisible = true,
            classes = Some("govuk-input--width-4"),
            labelClasses = Some("govuk-label--s")
          ))
        ),
        NGRRadioButtons(radioContent = "service.no", radioValue = livingAccommodationNo)
      ),
      Some(Legend(content = Text(messages("whatYourRentIncludes.radio.1.title")), classes = "govuk-fieldset__legend--m", isPageHeading = true)),
      Some("whatYourRentIncludes.radio.1.hint")
    )

  def ngrRadio2(implicit messages: Messages): NGRRadio =
    NGRRadio(
      NGRRadioName(rentPartAddressRadio),
      Seq(
        NGRRadioButtons(radioContent = "service.yes", radioValue = rentPartAddressYes),
        NGRRadioButtons(radioContent = "service.no", radioValue = rentPartAddressNo)
      ),
      hint = Some("whatYourRentIncludes.radio.2.hint")
    )

  def ngrRadio3(implicit messages: Messages): NGRRadio =
    NGRRadio(
      NGRRadioName(rentEmptyShellRadio),
      Seq(
        NGRRadioButtons(radioContent = "service.yes", radioValue = rentEmptyShellYes),
        NGRRadioButtons(radioContent = "service.no", radioValue = rentEmptyShellNo)
      ),
      Some(Legend(content = Text(messages("whatYourRentIncludes.radio.3.title")), classes = "govuk-fieldset__legend--m", isPageHeading = true)),
      Some("whatYourRentIncludes.radio.3.hint")
    )

  def ngrRadio4(implicit messages: Messages): NGRRadio =
    NGRRadio(
      NGRRadioName(rentIncBusinessRatesRadio),
      Seq(
        NGRRadioButtons(radioContent = "service.yes", radioValue = rentIncBusinessRatesYes),
        NGRRadioButtons(radioContent = "service.no", radioValue = rentIncBusinessRatesNo)
      ),
      Some(Legend(content = Text(messages("whatYourRentIncludes.radio.4.title")), classes = "govuk-fieldset__legend--m", isPageHeading = true))
    )

  def ngrRadio5(implicit messages: Messages): NGRRadio =
    NGRRadio(
      NGRRadioName(rentIncWaterChargesRadio),
      Seq(
        NGRRadioButtons(radioContent = "service.yes", radioValue = rentIncWaterChargesYes),
        NGRRadioButtons(radioContent = "service.no", radioValue = rentIncWaterChargesNo)
      ),
      Some(Legend(content = Text(messages("whatYourRentIncludes.radio.5.title")), classes = "govuk-fieldset__legend--m", isPageHeading = true))
    )

  def ngrRadio6(implicit messages: Messages): NGRRadio =
    NGRRadio(
      NGRRadioName(rentIncServiceRadio),
      Seq(
        NGRRadioButtons(radioContent = "service.yes", radioValue = rentIncServiceYes),
        NGRRadioButtons(radioContent = "service.no", radioValue = rentIncServiceNo)
      ),
      Some(Legend(content = Text(messages("whatYourRentIncludes.radio.6.title")), classes = "govuk-fieldset__legend--m", isPageHeading = true)),
      Some("whatYourRentIncludes.radio.6.hint")
    )

  def unapply(whatYourRentIncludesForm: WhatYourRentIncludesForm): Option[(String, String, String, String, String, String, Option[String])] =
    Some((
      whatYourRentIncludesForm.livingAccommodationRadio,
      whatYourRentIncludesForm.rentPartAddressRadio,
      whatYourRentIncludesForm.rentEmptyShellRadio,
      whatYourRentIncludesForm.rentIncBusinessRatesRadio,
      whatYourRentIncludesForm.rentIncWaterChargesRadio,
      whatYourRentIncludesForm.rentIncServiceRadio,
      whatYourRentIncludesForm.bedroomNumbers
    ))

  private def isBedroomNumberValidation[A]: Constraint[A] =
    Constraint((input: A) =>
      val whatYourRentIncludesForm = input.asInstanceOf[WhatYourRentIncludesForm]
      val bedroomNumber: Option[String] = whatYourRentIncludesForm.bedroomNumbers
      if (whatYourRentIncludesForm.livingAccommodationRadio.equals("livingAccommodationYes")) {
        if (bedroomNumber.isEmpty)
          Invalid("whatYourRentIncludes.bedroom.number.required.error")
        else if (Try(Integer.parseInt(bedroomNumber.get)).isFailure)
          Invalid("whatYourRentIncludes.bedroom.number.invalid.error")
        else if (Integer.parseInt(bedroomNumber.get) < 1)
          Invalid("whatYourRentIncludes.bedroom.number.minimum.error")
        else if (Integer.parseInt(bedroomNumber.get) > 99)
          Invalid("whatYourRentIncludes.bedroom.number.maximum.error")
        else
          Valid
      }
      else
        Valid
    )

  def form: Form[WhatYourRentIncludesForm] = {
    Form(
      mapping(
        livingAccommodationRadio  -> radioText(livingAccommodationRadioError),
        rentPartAddressRadio      -> radioText(rentPartAddressRadioError),
        rentEmptyShellRadio       -> radioText(rentEmptyShellRadioError),
        rentIncBusinessRatesRadio -> radioText(rentIncBusinessRatesRadioError),
        rentIncWaterChargesRadio  -> radioText(rentIncWaterChargesRadioError),
        rentIncServiceRadio       -> radioText(rentIncServiceRadioError),
        bedroomNumbers             -> optional(text().transform[String](_.strip(), identity))
      )(WhatYourRentIncludesForm.apply)(WhatYourRentIncludesForm.unapply)
        .verifying(isBedroomNumberValidation)
    )
  }
}

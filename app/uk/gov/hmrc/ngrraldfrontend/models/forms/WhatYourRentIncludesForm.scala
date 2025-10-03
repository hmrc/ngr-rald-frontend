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
import uk.gov.hmrc.ngrraldfrontend.models.WhatYourRentIncludes
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.{ngrRadio, noButtonWithFalseValue, simpleNgrRadio, yesButtonWithTrueValue}
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText

final case class WhatYourRentIncludesForm(
                                           livingAccommodationRadio: String,
                                           rentPartAddressRadio: String,
                                           rentEmptyShellRadio: String,
                                           rentIncBusinessRatesRadio: String,
                                           rentIncWaterChargesRadio: String,
                                           rentIncServiceRadio: String,
                                           bedroomNumbers: Option[String]
                                         ) extends RadioEntry

object WhatYourRentIncludesForm extends CommonFormValidators with Mappings {
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
    ngrRadio(
      radioName = livingAccommodationRadio,
      radioButtons = Seq(
        yesButtonWithTrueValue(
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
        noButtonWithFalseValue()
      ),
      ngrTitle = "whatYourRentIncludes.radio.1.title",
      hint = Some("whatYourRentIncludes.radio.1.hint")
    )

  def ngrRadio2(implicit messages: Messages): NGRRadio =
    simpleNgrRadio(rentPartAddressRadio, Some("whatYourRentIncludes.radio.2.hint"))

  def ngrRadio3(implicit messages: Messages): NGRRadio =
    ngrRadio(
      radioName = rentEmptyShellRadio,
      radioButtons = Seq(yesButtonWithTrueValue(), noButtonWithFalseValue()),
      ngrTitle = "whatYourRentIncludes.radio.3.title",
      hint = Some("whatYourRentIncludes.radio.3.hint")
    )

  def ngrRadio4(implicit messages: Messages): NGRRadio =
    ngrRadio(
      radioName = rentIncBusinessRatesRadio,
      radioButtons = Seq(yesButtonWithTrueValue(), noButtonWithFalseValue()),
      ngrTitle = "whatYourRentIncludes.radio.4.title"
    )

  def ngrRadio5(implicit messages: Messages): NGRRadio =
    ngrRadio(
      radioName = rentIncWaterChargesRadio,
      radioButtons = Seq(yesButtonWithTrueValue(), noButtonWithFalseValue()),
      ngrTitle = "whatYourRentIncludes.radio.5.title"
    )

  def ngrRadio6(implicit messages: Messages): NGRRadio =
    ngrRadio(
      radioName = rentIncServiceRadio,
      radioButtons = Seq(yesButtonWithTrueValue(), noButtonWithFalseValue()),
      ngrTitle = "whatYourRentIncludes.radio.6.title",
      hint = Some("whatYourRentIncludes.radio.6.hint")
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

  def answerToForm(whatYourRentIncludes: WhatYourRentIncludes): Form[WhatYourRentIncludesForm] =
    form.fill(WhatYourRentIncludesForm(
      livingAccommodationRadio = whatYourRentIncludes.livingAccommodation.toString,
      rentPartAddressRadio = whatYourRentIncludes.rentPartAddress.toString,
      rentEmptyShellRadio = whatYourRentIncludes.rentEmptyShell.toString,
      rentIncBusinessRatesRadio = whatYourRentIncludes.rentIncBusinessRates.toString,
      rentIncWaterChargesRadio = whatYourRentIncludes.rentIncWaterCharges.toString,
      rentIncServiceRadio = whatYourRentIncludes.rentIncService.toString,
      bedroomNumbers = whatYourRentIncludes.bedroomNumbers.map(_.toString)
    ))

  def formToAnswers(whatYourRentIncludesForm: WhatYourRentIncludesForm): WhatYourRentIncludes =
    WhatYourRentIncludes(
      livingAccommodation = whatYourRentIncludesForm.livingAccommodationRadio.toBoolean,
      rentPartAddress = whatYourRentIncludesForm.rentPartAddressRadio.toBoolean,
      rentEmptyShell = whatYourRentIncludesForm.rentEmptyShellRadio.toBoolean,
      rentIncBusinessRates = whatYourRentIncludesForm.rentIncBusinessRatesRadio.toBoolean,
      rentIncWaterCharges = whatYourRentIncludesForm.rentIncWaterChargesRadio.toBoolean,
      rentIncService = whatYourRentIncludesForm.rentIncServiceRadio.toBoolean,
      bedroomNumbers = whatYourRentIncludesForm.bedroomNumbers match {
        case Some(value) if whatYourRentIncludesForm.livingAccommodationRadio == "true" => Some(value.toInt)
        case _ => None
      }
    )

  private def isBedroomNumberValid[A]: Constraint[A] =
    Constraint((input: A) =>
      val whatYourRentIncludesForm = input.asInstanceOf[WhatYourRentIncludesForm]
      val bedroomNumber: Option[String] = whatYourRentIncludesForm.bedroomNumbers
      if (whatYourRentIncludesForm.livingAccommodationRadio.equals("true")) {
        if (bedroomNumber.isEmpty)
          Invalid("whatYourRentIncludes.bedroom.number.required.error")
        else if (!bedroomNumber.get.matches(wholePositiveNumberRegexp.pattern()))
          Invalid("whatYourRentIncludes.bedroom.number.invalid.error")
        else if (bedroomNumber.get.toDoubleOption.getOrElse(0d) > 99)
          Invalid("whatYourRentIncludes.bedroom.number.maximum.error")
        else if (bedroomNumber.get.toLong < 1)
          Invalid("whatYourRentIncludes.bedroom.number.minimum.error")
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
        bedroomNumbers            -> optional(text().transform[String](_.strip(), identity))
      )(WhatYourRentIncludesForm.apply)(WhatYourRentIncludesForm.unapply)
        .verifying(isBedroomNumberValid)
    )
  }
}

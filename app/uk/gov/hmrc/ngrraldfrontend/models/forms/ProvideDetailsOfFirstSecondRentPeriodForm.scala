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

import play.api.data.Forms.{mapping, of, optional, text}
import play.api.data.format.Formatter
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.data.{Form, FormError}
import play.api.i18n.*
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings

import scala.math.BigDecimal.RoundingMode
import scala.math.BigDecimal.RoundingMode.RoundingMode
import scala.util.Try

final case class ProvideDetailsOfFirstSecondRentPeriodForm(
                                firstDateStartInput: NGRDate,
                                firstDateEndInput: NGRDate,
                                firstRentPeriodRadio: String,
                                firstRentPeriodAmount: Option[String],
                                secondDateStartInput: NGRDate,
                                secondDateEndInput: NGRDate,
                                secondHowMuchIsRent: BigDecimal,
                              )

object ProvideDetailsOfFirstSecondRentPeriodForm extends CommonFormValidators with Mappings with DateMappings {
  implicit val format: OFormat[ProvideDetailsOfFirstSecondRentPeriodForm] = Json.format[ProvideDetailsOfFirstSecondRentPeriodForm]

  private lazy val radioFirstPeriodRequiredError = "provideDetailsOfFirstSecondRentPeriod.firstPeriod.radio.error.required"
  private val firstDateStartInput = "first.startDate"
  private val firstDateEndInput = "first.endDate"
  private val firstRentPeriodRadio = "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio"
  private val RentPeriodAmount = "RentPeriodAmount"
  private val SecondRentPeriodAmount = "SecondRentPeriodAmount"
  private val secondDateStartInput = "second.startDate"
  private val secondDateEndInput = "second.endDate"
  private lazy val annualRentEmptyError = "provideDetailsOfFirstSecondRentPeriod.secondPeriod.amount.required.error"
  private lazy val firstPeriodAmountEmptyError = "provideDetailsOfFirstSecondRentPeriod.firstPeriod.amount.required.error"

  private val maxAmount: BigDecimal = BigDecimal("9999999.99")
  val messagesApi: MessagesApi = new DefaultMessagesApi()
  val lang: Lang = Lang.defaultLang
  val messages: Messages = MessagesImpl(lang, messagesApi)

  def unapply(provideDetailsOfFirstSecondRentPeriodForm: ProvideDetailsOfFirstSecondRentPeriodForm): Option[(
    NGRDate, NGRDate, String, Option[String], NGRDate, NGRDate, BigDecimal)] =
    Some(
      provideDetailsOfFirstSecondRentPeriodForm.firstDateStartInput,
      provideDetailsOfFirstSecondRentPeriodForm.firstDateEndInput,
      provideDetailsOfFirstSecondRentPeriodForm.firstRentPeriodRadio,
      provideDetailsOfFirstSecondRentPeriodForm.firstRentPeriodAmount,
      provideDetailsOfFirstSecondRentPeriodForm.secondDateStartInput,
      provideDetailsOfFirstSecondRentPeriodForm.secondDateEndInput,
      provideDetailsOfFirstSecondRentPeriodForm.secondHowMuchIsRent
    )
  
  private def firstRentPeriodAmountValidation[A]: Constraint[A] =
    Constraint((input: A) =>
      val provideDetailsOfFirstSecondRentPeriodForm = input.asInstanceOf[ProvideDetailsOfFirstSecondRentPeriodForm]
      val rentAmount: Option[String] = provideDetailsOfFirstSecondRentPeriodForm.firstRentPeriodAmount
      if (provideDetailsOfFirstSecondRentPeriodForm.firstRentPeriodRadio.equals("yesPayedRent")) {
        if (rentAmount.isEmpty)
          Invalid(firstPeriodAmountEmptyError)
        else if (!rentAmount.get.matches(amountRegex.pattern()))
          Invalid("provideDetailsOfFirstSecondRentPeriod.firstPeriod.amount.invalid.error")
        else if (BigDecimal(rentAmount.get) > maxAmount)
          Invalid("provideDetailsOfFirstSecondRentPeriod.firstPeriod.amount.max.error")
        else
          Valid
      }
      else
        Valid
    )

  def form: Form[ProvideDetailsOfFirstSecondRentPeriodForm] = {
    Form(
      mapping(
        firstDateStartInput -> dateMapping
          .verifying(
            firstError(
              isDateEmpty(errorKeys("provideDetailsOfFirstSecondRentPeriod", "first.startDate")),
              isDateValid("provideDetailsOfFirstSecondRentPeriod.startDate.invalid.error"),
              isDateAfter1900("provideDetailsOfFirstSecondRentPeriod.first.startDate.before.1900.error")
            )
          ),
        firstDateEndInput -> dateMapping
          .verifying(
            firstError(
              isDateEmpty(errorKeys("provideDetailsOfFirstSecondRentPeriod", "first.endDate")),
              isDateValid("provideDetailsOfFirstSecondRentPeriod.first.endDate.invalid.error"),
              isDateAfter1900("provideDetailsOfFirstSecondRentPeriod.first.endDate.before.1900.error")
            )
          ),
        firstRentPeriodRadio -> radioText(radioFirstPeriodRequiredError),
        RentPeriodAmount -> optional(
          radioText()
            .transform[String](_.strip(), identity)
        ),
        secondDateStartInput -> dateMapping
          .verifying(
            firstError(
              isDateEmpty(errorKeys("provideDetailsOfFirstSecondRentPeriod", "second.startDate")),
              isDateValid("provideDetailsOfFirstSecondRentPeriod.startDate.invalid.error"),
              isDateAfter1900("provideDetailsOfFirstSecondRentPeriod.second.startDate.before.1900.error")
            )
          ),
        secondDateEndInput -> dateMapping
          .verifying(
            firstError(
              isDateEmpty(errorKeys("provideDetailsOfFirstSecondRentPeriod", "second.endDate")),
              isDateValid("provideDetailsOfFirstSecondRentPeriod.second.endDate.invalid.error"),
              isDateAfter1900("provideDetailsOfFirstSecondRentPeriod.second.endDate.before.1900.error")
            )
          ),
        SecondRentPeriodAmount -> text()
          .transform[String](_.strip(), identity)
          .verifying(
            firstError(
              isNotEmpty(SecondRentPeriodAmount, annualRentEmptyError),
              regexp(amountRegex.pattern(), "provideDetailsOfFirstSecondRentPeriod.secondPeriod.amount.invalid.error")
            )
          )
          .transform[BigDecimal](BigDecimal(_).setScale(2, RoundingMode.UP), _.toString)
          .verifying(
            maximumValue[BigDecimal](maxAmount, "provideDetailsOfFirstSecondRentPeriod.firstPeriod.amount.max.error")
          )
      )(ProvideDetailsOfFirstSecondRentPeriodForm.apply)(ProvideDetailsOfFirstSecondRentPeriodForm.unapply)
        .verifying(firstRentPeriodAmountValidation)
    )
  }
}

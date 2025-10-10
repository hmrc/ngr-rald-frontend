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

import play.api.data.{Form, FormError}
import play.api.data.Forms.{mapping, optional, text, of}
import play.api.data.format.Formatter
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.i18n.*
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.dateinput.DateInput
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.{Fieldset, Legend}
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.input.PrefixOrSuffix
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.{ngrRadio, noButton, yesButton}
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText

import scala.math.BigDecimal.RoundingMode

final case class ProvideDetailsOfFirstSecondRentPeriodForm(
                                                            firstDateStartInput: NGRDate,
                                                            firstDateEndInput: NGRDate,
                                                            firstRentPeriodRadio: String,
                                                            firstRentPeriodAmount: Option[BigDecimal],
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

  private def dateInput(dateInputName: String, dateInputLabel: String)(implicit messages: Messages): DateInput = DateInput(
    id = dateInputName,
    namePrefix = Some(dateInputName),
    fieldset = Some(Fieldset(
      legend = Some(Legend(
        content = Text(messages(dateInputLabel)),
        classes = "govuk-fieldset__legend--s",
        isPageHeading = true
      ))
    )),
    hint = Some(Hint(
      id = Some("provideDetailsOfFirstSecondRentPeriod.date.hint"),
      content = Text(messages("provideDetailsOfFirstSecondRentPeriod.date.hint"))
    ))
  )

  def firstDateStartInput(implicit messages: Messages): DateInput =
    dateInput(firstDateStartInput, "provideDetailsOfFirstSecondRentPeriod.firstPeriod.start.date.label")

  def firstDateEndInput(implicit messages: Messages): DateInput =
    dateInput(firstDateEndInput, "provideDetailsOfFirstSecondRentPeriod.firstPeriod.end.date.label")

  def secondDateStartInput(implicit messages: Messages): DateInput =
    dateInput(secondDateStartInput, "provideDetailsOfFirstSecondRentPeriod.secondPeriod.start.date.label")

  def secondDateEndInput(implicit messages: Messages): DateInput =
    dateInput(secondDateEndInput, "provideDetailsOfFirstSecondRentPeriod.secondPeriod.end.date.label")

  def firstRentPeriodRadio(form: Form[ProvideDetailsOfFirstSecondRentPeriodForm], inputText: InputText)(implicit messages: Messages): NGRRadio =
    ngrRadio(
      radioName = firstRentPeriodRadio,
      radioButtons = Seq(
        yesButton(
          conditionalHtml = Some(inputText(
            form = form,
            id = RentPeriodAmount,
            name = RentPeriodAmount,
            label = messages("provideDetailsOfFirstSecondRentPeriod.firstPeriod.radio.conditional.hint.bold"),
            isVisible = true,
            hint = Some(Hint(
              content = Text(messages("provideDetailsOfFirstSecondRentPeriod.firstPeriod.radio.conditional.hint"))
            )),
            classes = Some("govuk-input--width-10"),
            prefix = Some(PrefixOrSuffix(content = Text("£")))
          ))
        ),
        noButton(radioContent = "provideDetailsOfFirstSecondRentPeriod.firstPeriod.radio.no")
      ),
      ngrTitle = "provideDetailsOfFirstSecondRentPeriod.firstPeriod.radio.label",
    )

  def unapply(provideDetailsOfFirstSecondRentPeriodForm: ProvideDetailsOfFirstSecondRentPeriodForm): Option[(
    NGRDate, NGRDate, String, Option[BigDecimal], NGRDate, NGRDate, BigDecimal)] =
    Some(
      provideDetailsOfFirstSecondRentPeriodForm.firstDateStartInput,
      provideDetailsOfFirstSecondRentPeriodForm.firstDateEndInput,
      provideDetailsOfFirstSecondRentPeriodForm.firstRentPeriodRadio,
      provideDetailsOfFirstSecondRentPeriodForm.firstRentPeriodAmount,
      provideDetailsOfFirstSecondRentPeriodForm.secondDateStartInput,
      provideDetailsOfFirstSecondRentPeriodForm.secondDateEndInput,
      provideDetailsOfFirstSecondRentPeriodForm.secondHowMuchIsRent
    )

  def answerToForm(value: ProvideDetailsOfFirstSecondRentPeriod): Form[ProvideDetailsOfFirstSecondRentPeriodForm] =
    form.fill(
      ProvideDetailsOfFirstSecondRentPeriodForm(
        NGRDate.fromString(value.firstDateStart),
        NGRDate.fromString(value.firstDateEnd),
        value.firstRentPeriodRadio.toString,
        value.firstRentPeriodAmount,
        NGRDate.fromString(value.secondDateStart),
        NGRDate.fromString(value.secondDateEnd),
        value.secondHowMuchIsRent
      )
    )

  def formToAnswers(provideDetailsOfFirstSecondRentPeriodForm: ProvideDetailsOfFirstSecondRentPeriodForm): ProvideDetailsOfFirstSecondRentPeriod =
    ProvideDetailsOfFirstSecondRentPeriod(
      provideDetailsOfFirstSecondRentPeriodForm.firstDateStartInput.makeString,
      provideDetailsOfFirstSecondRentPeriodForm.firstDateEndInput.makeString,
      provideDetailsOfFirstSecondRentPeriodForm.firstRentPeriodRadio.toBoolean,
      if (provideDetailsOfFirstSecondRentPeriodForm.firstRentPeriodRadio.toBoolean)
        provideDetailsOfFirstSecondRentPeriodForm.firstRentPeriodAmount
      else
        None,
      provideDetailsOfFirstSecondRentPeriodForm.secondDateStartInput.makeString,
      provideDetailsOfFirstSecondRentPeriodForm.secondDateEndInput.makeString,
      provideDetailsOfFirstSecondRentPeriodForm.secondHowMuchIsRent
    )

  private def rentPeriodAmountFormatter(args: Seq[String] = Seq.empty): Formatter[Option[BigDecimal]] = new Formatter[Option[BigDecimal]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[BigDecimal]] =
      val isPayingRent = data.get(firstRentPeriodRadio).exists(_ == "true")
      data.get(key) match {
        case None if isPayingRent => Left(Seq(FormError(key, firstPeriodAmountEmptyError, args)))
        case Some(s) if isPayingRent => isRentPeriodAmountValid(s.trim.replaceAll("[£|,|\\s]", ""), key, args)
        case Some(s) => Right(Some(BigDecimal(s)))
        case None => Right(None)
      }

    override def unbind(key: String, value: Option[BigDecimal]): Map[String, String] =
      Map(key -> value.map(_.toString()).getOrElse(""))
  }

  private def isRentPeriodAmountValid(rentAmount: String, key: String, args: Seq[String]): Either[Seq[FormError], Option[BigDecimal]] =
    if (rentAmount.isEmpty)
      Left(Seq(FormError(key, firstPeriodAmountEmptyError, args)))
    else if (!rentAmount.matches(amountRegex.pattern()))
      Left(Seq(FormError(key, "provideDetailsOfFirstSecondRentPeriod.firstPeriod.amount.invalid.error", args)))
    else if (BigDecimal(rentAmount) > maxAmount)
      Left(Seq(FormError(key, "provideDetailsOfFirstSecondRentPeriod.firstPeriod.amount.max.error", args)))
    else
      Right(Some(BigDecimal(rentAmount)))

  def form: Form[ProvideDetailsOfFirstSecondRentPeriodForm] = {
    Form(
      mapping(
        firstDateStartInput -> dateMapping
          .verifying(
            firstError(
              isDateEmpty(errorKeys("provideDetailsOfFirstSecondRentPeriod", "first.startDate")),
              isDateValid("provideDetailsOfFirstSecondRentPeriod.first.startDate.invalid.error"),
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
        RentPeriodAmount -> of(rentPeriodAmountFormatter()),
        secondDateStartInput -> dateMapping
          .verifying(
            firstError(
              isDateEmpty(errorKeys("provideDetailsOfFirstSecondRentPeriod", "second.startDate")),
              isDateValid("provideDetailsOfFirstSecondRentPeriod.second.startDate.invalid.error"),
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
          .transform[String](_.strip().replaceAll("[£|,|\\s]", ""), identity)
          .verifying(
            firstError(
              isNotEmpty(SecondRentPeriodAmount, annualRentEmptyError),
              regexp(amountRegex.pattern(), "provideDetailsOfFirstSecondRentPeriod.secondPeriod.amount.invalid.error")
            )
          )
          .transform[BigDecimal](BigDecimal(_).setScale(2, RoundingMode.HALF_UP), _.toString)
          .verifying(
            maximumValue[BigDecimal](maxAmount, "provideDetailsOfFirstSecondRentPeriod.firstPeriod.amount.max.error")
          )
      )(ProvideDetailsOfFirstSecondRentPeriodForm.apply)(ProvideDetailsOfFirstSecondRentPeriodForm.unapply)
    )
  }
}

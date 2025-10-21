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

import play.api.data.Forms.{mapping, of}
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}
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

final case class ProvideDetailsOfFirstRentPeriodForm(
                                                      firstDateStartInput: NGRDate,
                                                      firstDateEndInput: NGRDate,
                                                      firstRentPeriodRadio: Boolean,
                                                      firstRentPeriodAmount: Option[BigDecimal]
                                                    )

object ProvideDetailsOfFirstRentPeriodForm extends CommonFormValidators with Mappings with DateMappings:
  implicit val format: OFormat[ProvideDetailsOfFirstRentPeriodForm] = Json.format[ProvideDetailsOfFirstRentPeriodForm]

  private lazy val radioFirstPeriodRequiredError = "provideDetailsOfFirstRentPeriod.firstPeriod.radio.error.required"
  private val firstDateStartInput = "first.startDate"
  private val firstDateEndInput = "first.endDate"
  private val firstRentPeriodRadio = "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio"
  private val RentPeriodAmount = "RentPeriodAmount"
  private lazy val firstPeriodAmountEmptyError = "provideDetailsOfFirstRentPeriod.firstPeriod.amount.required.error"

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
    dateInput(firstDateStartInput, "provideDetailsOfFirstRentPeriod.first.startDate.label")

  def firstDateEndInput(implicit messages: Messages): DateInput =
    dateInput(firstDateEndInput, "provideDetailsOfFirstRentPeriod.first.endDate.label")

  def firstRentPeriodRadio(form: Form[ProvideDetailsOfFirstRentPeriodForm], inputText: InputText)(implicit messages: Messages): NGRRadio =
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

  def unapply(provideDetailsOfFirstRentPeriodForm: ProvideDetailsOfFirstRentPeriodForm): Option[(NGRDate, NGRDate, Boolean, Option[BigDecimal])] =
    Some(
      provideDetailsOfFirstRentPeriodForm.firstDateStartInput,
      provideDetailsOfFirstRentPeriodForm.firstDateEndInput,
      provideDetailsOfFirstRentPeriodForm.firstRentPeriodRadio,
      provideDetailsOfFirstRentPeriodForm.firstRentPeriodAmount
    )

  def answerToForm(value: ProvideDetailsOfFirstRentPeriod): Form[ProvideDetailsOfFirstRentPeriodForm] =
    form.fill(
      ProvideDetailsOfFirstRentPeriodForm(
        NGRDate.fromLocalDate(value.startDate),
        NGRDate.fromLocalDate(value.endDate),
        value.isRentPayablePeriod,
        value.rentPeriodAmount
      )
    )

  def formToAnswers(provideDetailsOfFirstRentPeriodForm: ProvideDetailsOfFirstRentPeriodForm): ProvideDetailsOfFirstRentPeriod =
    ProvideDetailsOfFirstRentPeriod(
      provideDetailsOfFirstRentPeriodForm.firstDateStartInput.toLocalDate,
      provideDetailsOfFirstRentPeriodForm.firstDateEndInput.toLocalDate,
      provideDetailsOfFirstRentPeriodForm.firstRentPeriodRadio,
      if provideDetailsOfFirstRentPeriodForm.firstRentPeriodRadio then provideDetailsOfFirstRentPeriodForm.firstRentPeriodAmount
      else None
    )

  private def rentPeriodAmountFormatter(args: Seq[String] = Seq.empty): Formatter[Option[BigDecimal]] =
    new Formatter[Option[BigDecimal]] {
      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[BigDecimal]] =
        val isPayingRent = data.get(firstRentPeriodRadio).contains("true")
        data.get(key) match {
          case None if isPayingRent => Left(Seq(FormError(key, firstPeriodAmountEmptyError, args)))
          case Some(s) if isPayingRent => isRentPeriodAmountValid(s.trim.replaceAll("[£|,\\s]", ""), key, args)
          case _ => Right(None)
        }

      override def unbind(key: String, value: Option[BigDecimal]): Map[String, String] =
        Map(key -> value.map(_.toString).getOrElse(""))
    }

  private def isRentPeriodAmountValid(rentAmount: String, key: String, args: Seq[String]): Either[Seq[FormError], Option[BigDecimal]] =
    if rentAmount.isEmpty then
      Left(Seq(FormError(key, firstPeriodAmountEmptyError, args)))
    else if !rentAmount.matches(amountRegex.pattern()) then
      Left(Seq(FormError(key, "provideDetailsOfFirstRentPeriod.firstPeriod.amount.invalid.error", args)))
    else if BigDecimal(rentAmount) > maxAmount then
      Left(Seq(FormError(key, "provideDetailsOfFirstRentPeriod.firstPeriod.amount.max.error", args)))
    else
      Right(Some(BigDecimal(rentAmount)))

  def form: Form[ProvideDetailsOfFirstRentPeriodForm] =
    Form(
      mapping(
        firstDateStartInput -> dateMapping
          .verifying(
            firstError(
              isDateEmpty(errorKeys("provideDetailsOfFirstRentPeriod", "first.startDate")),
              isDateValid("provideDetailsOfFirstRentPeriod.first.startDate.invalid.error"),
              isDateAfter1900("provideDetailsOfFirstRentPeriod.first.startDate.before.1900.error")
            )
          ),
        firstDateEndInput -> dateMapping
          .verifying(
            firstError(
              isDateEmpty(errorKeys("provideDetailsOfFirstRentPeriod", "first.endDate")),
              isDateValid("provideDetailsOfFirstRentPeriod.first.endDate.invalid.error"),
              isDateAfter1900("provideDetailsOfFirstRentPeriod.first.endDate.before.1900.error")
            )
          ),
        firstRentPeriodRadio -> radioBoolean(radioFirstPeriodRequiredError),
        RentPeriodAmount -> of(using rentPeriodAmountFormatter())
      )(ProvideDetailsOfFirstRentPeriodForm.apply)(ProvideDetailsOfFirstRentPeriodForm.unapply)
        .verifying(
          "provideDetailsOfFirstRentPeriod.first.endDate.before.startDate.error",
          firstRent => firstRent.firstDateStartInput.toLocalDate.isBefore(firstRent.firstDateEndInput.toLocalDate)
        )
    )

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
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.dateinput.DateInput
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.{Fieldset, Legend}
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.input.PrefixOrSuffix
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.{ngrRadio, noButton, yesButton}
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.{CompareWithAnotherDateValidation, DateValidation, Mappings}
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText

import java.time.LocalDate

object ProvideDetailsOfFirstRentPeriodForm extends CommonFormValidators with Mappings:

  private lazy val radioFirstPeriodRequiredError = "provideDetailsOfFirstRentPeriod.firstPeriod.radio.error.required"
  private val firstDateStartInput = "startDate"
  private val firstDateEndInput = "endDate"
  private val firstRentPeriodRadio = "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod"
  private val rentPeriodAmount = "rentPeriodAmount"
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
        classes = "govuk-fieldset__legend--m",
        isPageHeading = false
      ))
    )),
    hint = Some(Hint(
      id = Some("provideDetailsOfFirstSecondRentPeriod.date.hint"),
      content = Text(messages("provideDetailsOfFirstSecondRentPeriod.date.hint"))
    ))
  )

  def firstDateStartInput(implicit messages: Messages): DateInput =
    dateInput(firstDateStartInput, "provideDetailsOfFirstRentPeriod.startDate.label")

  def firstDateEndInput(implicit messages: Messages): DateInput =
    dateInput(firstDateEndInput, "provideDetailsOfFirstRentPeriod.endDate.label")

  def firstRentPeriodRadio(form: Form[ProvideDetailsOfFirstRentPeriod], inputText: InputText)(implicit messages: Messages): NGRRadio =
    ngrRadio(
      radioName = firstRentPeriodRadio,
      radioButtons = Seq(
        yesButton(
          conditionalHtml = Some(inputText(
            form = form,
            id = rentPeriodAmount,
            name = rentPeriodAmount,
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
      isPageHeading = false
    )

  private def rentPeriodAmountFormatter(args: Seq[String] = Seq.empty): Formatter[Option[BigDecimal]] =
    new Formatter[Option[BigDecimal]] {
      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[BigDecimal]] =
        val isPayingRent = data.get(firstRentPeriodRadio).contains("true")
        data.get(key) match {
          case None if isPayingRent => Left(Seq(FormError(key, firstPeriodAmountEmptyError, args)))
          case Some(s) if isPayingRent => isRentPeriodAmountValid(s.trim.replaceAll("[£,\\s]", ""), key, args)
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

  def form: Form[ProvideDetailsOfFirstRentPeriod] =
    Form(
      mapping(
        firstDateStartInput -> dateMapping("provideDetailsOfFirstRentPeriod.startDate"),
        firstDateEndInput -> dateMapping(
          "provideDetailsOfFirstRentPeriod.endDate",
          CompareWithAnotherDateValidation("before.startDate.error", "startDate", (end, start) => start.isBefore(end))
        ),
        firstRentPeriodRadio -> radioBoolean(radioFirstPeriodRequiredError),
        rentPeriodAmount -> of(using rentPeriodAmountFormatter())
      )(ProvideDetailsOfFirstRentPeriod.apply)(o =>  Some(
        o.startDate,
        o.endDate,
        o.isRentPayablePeriod,
        o.rentPeriodAmount
      ))
    )

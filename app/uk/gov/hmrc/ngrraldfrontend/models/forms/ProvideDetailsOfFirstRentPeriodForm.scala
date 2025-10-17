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
                                                      firstRentPeriodRadio: String,
                                                      firstRentPeriodAmount: Option[BigDecimal]
                                                    )

object ProvideDetailsOfFirstRentPeriodForm extends CommonFormValidators with Mappings with DateMappings:
  implicit val format: OFormat[ProvideDetailsOfFirstRentPeriodForm] = Json.format[ProvideDetailsOfFirstRentPeriodForm]

  private lazy val radioFirstPeriodRequiredError = "provideDetailsOfFirstSecondRentPeriod.firstPeriod.radio.error.required"
  private val firstDateStartInput = "first.startDate"
  private val firstDateEndInput = "first.endDate"
  private val firstRentPeriodRadio = "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio"
  private val RentPeriodAmount = "RentPeriodAmount"
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

  def unapply(provideDetailsOfFirstRentPeriodForm: ProvideDetailsOfFirstRentPeriodForm): Option[(
    NGRDate, NGRDate, String, Option[BigDecimal])] =
    Some(
      provideDetailsOfFirstRentPeriodForm.firstDateStartInput,
      provideDetailsOfFirstRentPeriodForm.firstDateEndInput,
      provideDetailsOfFirstRentPeriodForm.firstRentPeriodRadio,
      provideDetailsOfFirstRentPeriodForm.firstRentPeriodAmount
    )

  def answerToForm(value: ProvideDetailsOfFirstRentPeriod): Form[ProvideDetailsOfFirstRentPeriodForm] =
    form.fill(
      ProvideDetailsOfFirstRentPeriodForm(
        NGRDate.fromString(value.firstDateStart),
        NGRDate.fromString(value.firstDateEnd),
        value.firstRentPeriodRadio.toString,
        value.firstRentPeriodAmount
      )
    )

  def formToAnswers(provideDetailsOfFirstRentPeriodForm: ProvideDetailsOfFirstRentPeriodForm): ProvideDetailsOfFirstRentPeriod =
    ProvideDetailsOfFirstRentPeriod(
      provideDetailsOfFirstRentPeriodForm.firstDateStartInput.makeString,
      provideDetailsOfFirstRentPeriodForm.firstDateEndInput.makeString,
      provideDetailsOfFirstRentPeriodForm.firstRentPeriodRadio.toBoolean,
      if (provideDetailsOfFirstRentPeriodForm.firstRentPeriodRadio.toBoolean)        provideDetailsOfFirstRentPeriodForm.firstRentPeriodAmount
      else        None
    )

  private def rentPeriodAmountFormatter(args: Seq[String] = Seq.empty): Formatter[Option[BigDecimal]] = new Formatter[Option[BigDecimal]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[BigDecimal]] =
      val isPayingRent = data.get(firstRentPeriodRadio).contains("true")
      data.get(key) match {
        case None if isPayingRent => Left(Seq(FormError(key, firstPeriodAmountEmptyError, args)))
        case Some(s) if isPayingRent => isRentPeriodAmountValid(s.trim.replaceAll("[£|,\\s]", ""), key, args)
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

  def form: Form[ProvideDetailsOfFirstRentPeriodForm] =
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
        RentPeriodAmount -> of(using rentPeriodAmountFormatter())
      )(ProvideDetailsOfFirstRentPeriodForm.apply)(ProvideDetailsOfFirstRentPeriodForm.unapply)
    )

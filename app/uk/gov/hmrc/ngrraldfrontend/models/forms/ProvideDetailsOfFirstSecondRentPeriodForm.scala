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
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.data.{Form, FormError}
import play.api.i18n.*
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.forms.AgreementVerbalForm.{isDateEmpty, isDateValid}
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings

import scala.util.Try

final case class ProvideDetailsOfFirstSecondRentPeriodForm(
                                firstDateStartInput: NGRDate,
                                firstDateEndInput: NGRDate,
                                firstRentPeriodRadio: String,
                                firstRentPeriodAmount: Option[BigDecimal],
                                secondDateStartInput: NGRDate,
                                secondDateEndInput: NGRDate,
                                secondHowMuchIsRent: BigDecimal,
                              )

object ProvideDetailsOfFirstSecondRentPeriodForm extends CommonFormValidators with Mappings with DateMappings{
  implicit val format: OFormat[ProvideDetailsOfFirstSecondRentPeriodForm] = Json.format[ProvideDetailsOfFirstSecondRentPeriodForm]

  private def errorKeys(whichDate: String): Map[DateErrorKeys, String] = Map(
    Required -> s"provideDetailsOfFirstSecondRentPeriod.$whichDate.required.error",
    DayAndMonth -> s"provideDetailsOfFirstSecondRentPeriod.$whichDate.day.month.required.error",
    DayAndYear -> s"provideDetailsOfFirstSecondRentPeriod.$whichDate.day.year.required.error",
    MonthAndYear -> s"provideDetailsOfFirstSecondRentPeriod.$whichDate.month.year.required.error",
    Day -> s"provideDetailsOfFirstSecondRentPeriod.$whichDate.day.required.error",
    Month -> s"provideDetailsOfFirstSecondRentPeriod.$whichDate.month.required.error",
    Year -> s"provideDetailsOfFirstSecondRentPeriod.$whichDate.year.required.error"
  )

  private lazy val radioFirstPeriodRequiredError = "provideDetailsOfFirstSecondRentPeriod.firstPeriod.radio.error.required"
  private val firstDateStartInput = "provideDetailsOfFirstSecondRentPeriod.firstPeriod.start.date"
  private val firstDateEndInput = "provideDetailsOfFirstSecondRentPeriod.firstPeriod.end.date"
  private val firstRentPeriodRadio = "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio"
  private val RentPeriodAmount = "RentPeriodAmount"
  private val SecondRentPeriodAmount = "SecondRentPeriodAmount"
  private val secondDateStartInput = "provideDetailsOfFirstSecondRentPeriod.secondPeriod.start.date"
  private val secondDateEndInput = "provideDetailsOfFirstSecondRentPeriod.secondPeriod.end.date"
  private lazy val annualRentEmptyError = "provideDetailsOfFirstSecondRentPeriod.secondPeriod.amount.error.required"
  private lazy val provideDetailsOfFirstSecondRentPeriodSecondPeriodAmountFormatError = "provideDetailsOfFirstSecondRentPeriod.firstPeriod.amount.error.required"


  val messagesApi: MessagesApi = new DefaultMessagesApi()
  val lang: Lang = Lang.defaultLang
  val messages: Messages = MessagesImpl(lang, messagesApi)

  def unapply(provideDetailsOfFirstSecondRentPeriodForm: ProvideDetailsOfFirstSecondRentPeriodForm): Option[(
    NGRDate, NGRDate, String, Option[BigDecimal], NGRDate, NGRDate, BigDecimal)] =
    Some(
      (provideDetailsOfFirstSecondRentPeriodForm.firstDateStartInput,
        provideDetailsOfFirstSecondRentPeriodForm.firstDateEndInput,
        provideDetailsOfFirstSecondRentPeriodForm.firstRentPeriodRadio,
        provideDetailsOfFirstSecondRentPeriodForm.firstRentPeriodAmount,
        provideDetailsOfFirstSecondRentPeriodForm.secondDateStartInput,
        provideDetailsOfFirstSecondRentPeriodForm.secondDateEndInput,
        provideDetailsOfFirstSecondRentPeriodForm.secondHowMuchIsRent,
      )
    )


  def bigDecimalWithFormatError: Formatter[BigDecimal] = new Formatter[BigDecimal] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], BigDecimal] = {
      data.get(key).filter(_.nonEmpty) match {
        case Some(value) =>
          Try(BigDecimal(value)).toEither.left.map(_ =>
            Seq(FormError(key, annualRentEmptyError))
          )
        case None =>
          Left(Seq(FormError(key, annualRentEmptyError)))
      }
    }
    override def unbind(key: String, value: BigDecimal): Map[String, String] =
      Map(key -> value.toString())
  }

  private def optionalBigDecimalWithFormatError: Formatter[Option[BigDecimal]] = new Formatter[Option[BigDecimal]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[BigDecimal]] = {
      data.get(key).map(_.trim).filter(_.nonEmpty) match {
        case Some(value) =>
          Try(BigDecimal(value)).toEither match {
            case Right(parsed) => Right(Some(parsed))
            case Left(_) => Left(Seq(FormError(key, "provideDetailsOfFirstSecondRentPeriod.firstPeriod.amount.error.required")))
          }
        case None => Right(None) // Field is optional and not provided
      }
    }

    override def unbind(key: String, value: Option[BigDecimal]): Map[String, String] =
      value.map(v => Map(key -> v.toString)).getOrElse(Map.empty)
  }


  private def isOptionalBigDecimalEmptyOrInvalid[A]: Constraint[A] =
    Constraint((input: A) =>
      val provideDetailsOfFirstSecondRentPeriodForm = input.asInstanceOf[ProvideDetailsOfFirstSecondRentPeriodForm]
      if (provideDetailsOfFirstSecondRentPeriodForm.firstRentPeriodRadio.equals("yesPayedRent") && provideDetailsOfFirstSecondRentPeriodForm.firstRentPeriodAmount.getOrElse(None) == None)
        Invalid(provideDetailsOfFirstSecondRentPeriodSecondPeriodAmountFormatError)
      else
        Valid
    )


  def form: Form[ProvideDetailsOfFirstSecondRentPeriodForm] = {
    Form(
      mapping(
        firstDateStartInput -> dateMapping
          .verifying(
            firstError(
              isDateEmpty(errorKeys("first.startDate")),
              isDateValid("provideDetailsOfFirstSecondRentPeriod.startDate.format.error")
            )
          ),
        firstDateEndInput -> dateMapping
          .verifying(
            firstError(
              isDateEmpty(errorKeys("first.endDate")),
              isDateValid("provideDetailsOfFirstSecondRentPeriod.endDate.format.error")
            )
          ),
        firstRentPeriodRadio -> text(radioFirstPeriodRequiredError),
        RentPeriodAmount -> of(optionalBigDecimalWithFormatError),
        secondDateStartInput -> dateMapping
          .verifying(
            firstError(
              isDateEmpty(errorKeys("second.startDate")),
              isDateValid("provideDetailsOfFirstSecondRentPeriod.startDate.format.error")
            )
          ),
        secondDateEndInput -> dateMapping
          .verifying(
            firstError(
              isDateEmpty(errorKeys("second.endDate")),
              isDateValid("provideDetailsOfFirstSecondRentPeriod.endDate.format.error")
            )
          ),
        SecondRentPeriodAmount -> of(bigDecimalWithFormatError)
      )(ProvideDetailsOfFirstSecondRentPeriodForm.apply)(ProvideDetailsOfFirstSecondRentPeriodForm.unapply)
        .verifying(
          firstError(
            isOptionalBigDecimalEmptyOrInvalid
          )
        )
    )
  }
}

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
import play.api.data.Forms.{mapping, optional, of}
import play.api.data.format.Formatter
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationResult}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.{ngrRadio, noButton, yesButton}
import uk.gov.hmrc.ngrraldfrontend.models.components.{NGRRadio, NGRRadioButtons}
import uk.gov.hmrc.ngrraldfrontend.models.forms.LandlordForm.wholePositiveNumberRegexp
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings
import uk.gov.hmrc.ngrraldfrontend.models.{MonthYearMappings, NGRDate, NGRMonthYear, RentReview}
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputDateForMonthYear

case class RentReviewForm(hasIncludeRentReview: String, monthsYears: Option[NGRMonthYear], canRentGoDown: String)

object RentReviewForm extends Mappings with MonthYearMappings {
  
  implicit val format: OFormat[RentReviewForm] = Json.format[RentReviewForm]

  val hasIncludeRentReviewRadio = "has-include-rent-review-radio"
  val canRentGoDownRadio = "can-rent-go-down-radio"
  
  def unapply(rentReviewForm: RentReviewForm): Option[(String, Option[NGRMonthYear], String)] =
    Some(rentReviewForm.hasIncludeRentReview, rentReviewForm.monthsYears, rentReviewForm.canRentGoDown)

  def answerToForm(rentReview: RentReview): Form[RentReviewForm] =
    form.fill(
      RentReviewForm(
        hasIncludeRentReview = rentReview.hasIncludeRentReview.toString,
        monthsYears = if (rentReview.rentReviewYears.isDefined || rentReview.rentReviewMonths.isDefined)
          Some(NGRMonthYear(rentReview.rentReviewMonths.map(_.toString).getOrElse(""), rentReview.rentReviewYears.map(_.toString).getOrElse("")))
        else
          None,
        canRentGoDown = rentReview.canRentGoDown.toString
      )
    )

  def formToAnswers(rentReviewForm: RentReviewForm): RentReview =
    val hasIncluded = rentReviewForm.hasIncludeRentReview.toBoolean
    RentReview(
      hasIncludeRentReview = rentReviewForm.hasIncludeRentReview.toBoolean,
      rentReviewMonths = if (hasIncluded) rentReviewForm.monthsYears.flatMap(_.month.toIntOption) else None,
      rentReviewYears = if (hasIncluded) rentReviewForm.monthsYears.flatMap(_.year.toIntOption) else None,
      canRentGoDown = rentReviewForm.canRentGoDown.toBoolean
    )

  private def monthYearFormatter(args: Seq[String] = Seq.empty): Formatter[Option[NGRMonthYear]] = new Formatter[Option[NGRMonthYear]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[NGRMonthYear]] =
      val hasRentReview = data.get(hasIncludeRentReviewRadio).exists(_ == "true")
      (data.get(s"$key.month"), data.get(s"$key.year")) match {
        case (None, None) if hasRentReview => Left(Seq(FormError(key, "rentReview.date.required.error", args)))
        case (Some(month), Some(year)) if hasRentReview => isMonthYearValid(month.trim, year.trim, key, args)
        case (Some(month), Some(year)) => Right(Some(NGRMonthYear(month, year)))
        case (None, None) => Right(None)
      }

    override def unbind(key: String, value: Option[NGRMonthYear]): Map[String, String] =
      Map(
        s"$key.month" -> value.map(_.month).getOrElse(""),
        s"$key.year"  -> value.map(_.year).getOrElse("")
      )
  }

  private def isMonthYearValid(month: String, year: String, key: String, args: Seq[String]): Either[Seq[FormError], Option[NGRMonthYear]] =
    (month, year) match
      case ("" | "0", "" | "0") => Left(Seq(FormError(key, "rentReview.date.required.error", args)))
      case ("" | "0", year) => isYearsValid(month, year, key, args)
      case (month, "" | "0") => isMonthsValidWhenNoYears(month, year, key, args)
      case (month, year) =>
        val monthEither = isMonthsValidWithYears(month, year, key, args)
        val yearEither = isYearsValid(month, year, key, args)
        (monthEither.isLeft, yearEither.isLeft) match
          case (true, true) => monthEither.left.map(formErrorSeq => formErrorSeq ++ yearEither.left.getOrElse(Seq.empty))
          case (false, true) => yearEither
          case (_, _) => monthEither

  private def isMonthsValidWhenNoYears(month: String, year: String, key: String, args: Seq[String]): Either[Seq[FormError], Option[NGRMonthYear]] =
    if (!month.matches(wholePositiveNumberRegexp.pattern()))
      Left(Seq(FormError(s"$key.month", "rentReview.rentReviewMonthsYears.months.invalid.error", args)))
    else if (month.toDouble > 12d)
      Left(Seq(FormError(s"$key.month", "rentReview.rentReviewMonthsYears.months.maximum.12.error", args)))
    else
      Right(Some(NGRMonthYear(month, year)))

  private def isMonthsValidWithYears(month: String, year: String, key: String, args: Seq[String]): Either[Seq[FormError], Some[NGRMonthYear]] =
    if (!month.matches(wholePositiveNumberRegexp.pattern()))
      Left(Seq(FormError(s"$key.month", "rentReview.rentReviewMonthsYears.months.invalid.error", args)))
    else if (month.toDouble > 11d)
      Left(Seq(FormError(s"$key.month", "rentReview.rentReviewMonthsYears.months.maximum.11.error", args)))
    else
      Right(Some(NGRMonthYear(month, year)))

  private def isYearsValid(month: String, year: String, key: String, args: Seq[String]): Either[Seq[FormError], Option[NGRMonthYear]] =
    if (!year.matches(wholePositiveNumberRegexp.pattern()))
      Left(Seq(FormError(s"$key.year", "rentReview.rentReviewMonthsYears.years.invalid.error", args)))
    else if (year.toDouble > 1000)
      Left(Seq(FormError(s"$key.year", "rentReview.rentReviewMonthsYears.years.maximum.1000.error", args)))
    else
      Right(Some(NGRMonthYear(month, year)))

  def form: Form[RentReviewForm] = {
    Form(
      mapping(
        hasIncludeRentReviewRadio -> radioText("rentReview.hasIncludeRentReview.radio.empty.error"),
        "date" -> of(monthYearFormatter()),
        canRentGoDownRadio -> radioText("rentReview.canRentGoDown.radio.empty.error")
      )(RentReviewForm.apply)(RentReviewForm.unapply)
    )
  }

  def createHasIncludeRentReviewRadio(form: Form[RentReviewForm], inputDateForMonthYear: InputDateForMonthYear)(implicit messages: Messages): NGRRadio =
    ngrRadio(radioName = hasIncludeRentReviewRadio,
      radioButtons = Seq(
        yesButton(
          conditionalHtml = Some(inputDateForMonthYear(form,
            legendContent = messages("rentReview.howOftenReviewed.label"),
            text1Label = "rentReview.years",
            text2Label = "rentReview.months",
            text1Name = "year",
            text2Name = "month",
            text2Class = "govuk-input--width-2",
            legendAsPageHeading = false))
        ),
        noButton()
      ),
      ngrTitle = "rentReview.hasIncludeRentReview.radio.label",
      hint = Some("rentReview.hasIncludeRentReview.radio.hint")
    )

  def createCanRentGoDownRadio(implicit messages: Messages): NGRRadio =
    ngrRadio(radioName = canRentGoDownRadio,
      radioButtons = Seq(yesButton(), noButton()),
      ngrTitle = "rentReview.canRentGoDown.radio.label"
    )
}

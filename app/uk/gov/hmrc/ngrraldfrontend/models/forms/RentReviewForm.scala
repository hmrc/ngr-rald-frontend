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

import play.api.data.Form
import play.api.data.Forms.{mapping, optional}
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationResult}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.{ngrRadio, noButton, yesButton}
import uk.gov.hmrc.ngrraldfrontend.models.{MonthYearMappings, NGRMonthYear, RentReview}
import uk.gov.hmrc.ngrraldfrontend.models.components.{NGRRadio, NGRRadioButtons}
import uk.gov.hmrc.ngrraldfrontend.models.forms.LandlordForm.wholePositiveNumberRegexp
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings
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
  
  private def isMonthsValid[A]: Constraint[A] =
    Constraint((input: A) =>
      val rentReviewForm = input.asInstanceOf[RentReviewForm]
      if (rentReviewForm.hasIncludeRentReview.toBoolean)
        if (rentReviewForm.monthsYears.isEmpty)
          Invalid("rentReview.date.required.error")
        else
          val ngrMonthYear: NGRMonthYear = rentReviewForm.monthsYears.get
          (ngrMonthYear.month, ngrMonthYear.year) match
            case ("" | "0", years)  => Valid
            case (months, "" | "0") => isMonthsValidWhenNoYearsIsEmpty(months)
            case (months, years)    => isMonthsValidWithYears(months)
      else
        Valid
    )

  private def isMonthsValidWhenNoYearsIsEmpty(months: String): ValidationResult =
    if (!months.matches(wholePositiveNumberRegexp.pattern()))
      Invalid("rentReview.rentReviewMonthsYears.months.invalid.error")
    else if (months.toDouble > 12d)
      Invalid("rentReview.rentReviewMonthsYears.months.maximum.12.error")
    else
      Valid

  private def isMonthsValidWithYears(months: String): ValidationResult =
    if (!months.matches(wholePositiveNumberRegexp.pattern()))
      Invalid("rentReview.rentReviewMonthsYears.months.invalid.error")
    else if (months.toDouble > 11d)
      Invalid("rentReview.rentReviewMonthsYears.months.maximum.11.error")
    else
      Valid

  private def isYearsValid[A]: Constraint[A] =
    Constraint((input: A) =>
      val rentReviewForm = input.asInstanceOf[RentReviewForm]
      if (rentReviewForm.hasIncludeRentReview.toBoolean && rentReviewForm.monthsYears.nonEmpty && rentReviewForm.monthsYears.get.year.nonEmpty)
        val years = rentReviewForm.monthsYears.get.year
        if (!years.matches(wholePositiveNumberRegexp.pattern()))
          Invalid("rentReview.rentReviewMonthsYears.years.invalid.error")
        else if (years.toDouble > 1000)
          Invalid("rentReview.rentReviewMonthsYears.years.maximum.1000.error")
        else
          Valid
      else
        Valid
    )

  def form: Form[RentReviewForm] = {
    Form(
      mapping(
        hasIncludeRentReviewRadio -> radioText("rentReview.hasIncludeRentReview.radio.empty.error"),
        "date" -> optional(monthYearMapping),
        canRentGoDownRadio -> radioText("rentReview.canRentGoDown.radio.empty.error")
      )(RentReviewForm.apply)(RentReviewForm.unapply)
        .verifying(isMonthsValid, isYearsValid)
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

/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.data.validation.{Constraint, Invalid, Valid, ValidationResult}
import uk.gov.hmrc.ngrraldfrontend.models.*

import java.util.regex.Pattern
import scala.util.Try

trait CommonFormValidators {
  val amountRegex: Pattern = Pattern.compile("([0-9]+\\.[0-9]+|[0-9]+)")

  protected def firstError[A](constraints: Constraint[A]*): Constraint[A] =
    Constraint { input =>
      constraints
        .map(_.apply(input))
        .find(_ != Valid)
        .getOrElse(Valid)
    }

  protected def regexp(regex: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.matches(regex) =>
        Valid
      case _ =>
        Invalid(errorKey, regex)
    }

  protected def maxLength(maximum: Int, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.length <= maximum =>
        Valid
      case _ =>
        Invalid(errorKey, maximum)
    }

  protected def isNotEmpty(value: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.trim.nonEmpty =>
        Valid
      case _ =>
        Invalid(errorKey, value)
    }

  protected def maximumValue[A](maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint { input =>
      import ev._

      if (input <= maximum) {
        Valid
      } else {
        Invalid(errorKey, maximum)
      }
    }

  protected def isMonthYearEmpty[A](errorKeys: Map[DateErrorKeys, String]): Constraint[A] =
    Constraint((input: A) =>
      monthYearEmptyValidation(input.asInstanceOf[NGRMonthYear], errorKeys)
    )

  protected def isMonthYearValid[A](errorKey: String): Constraint[A] =
    Constraint((input: A) =>
      val date = input.asInstanceOf[NGRMonthYear]
      monthYearValidation(date, errorKey)
    )

  protected def isMonthYearAfter1900[A](errorKey: String): Constraint[A] =
    Constraint((input: A) =>
      val date = input.asInstanceOf[NGRMonthYear]
      monthYearAfter1900Validation(date, errorKey)
    )

  protected def isDateEmpty[A](errorKeys: Map[DateErrorKeys, String]): Constraint[A] =
    Constraint((input: A) =>
      dateEmptyValidation(input.asInstanceOf[NGRDate], errorKeys)
    )

  protected def isDateValid[A](errorKey: String): Constraint[A] =
    Constraint((input: A) =>
      val date = input.asInstanceOf[NGRDate]
      dateValidation(date, errorKey)
    )

  protected def isDateAfter1900[A](errorKey: String): Constraint[A] =
    Constraint((input: A) =>
      val date = input.asInstanceOf[NGRDate]
      dateAfter1900Validation(date, errorKey)
    )

  protected def dateEmptyValidation(date: NGRDate, errorKeys: Map[DateErrorKeys, String]): ValidationResult =
    (date.day.isEmpty, date.month.isEmpty, date.year.isEmpty) match
      case (true, true, true) => Invalid(errorKeys.get(Required).getOrElse(""))
      case (true, true, false) => Invalid(errorKeys.get(DayAndMonth).getOrElse(""))
      case (true, false, true) => Invalid(errorKeys.get(DayAndYear).getOrElse(""))
      case (false, true, true) => Invalid(errorKeys.get(MonthAndYear).getOrElse(""))
      case (true, false, false) => Invalid(errorKeys.get(Day).getOrElse(""))
      case (false, true, false) => Invalid(errorKeys.get(Month).getOrElse(""))
      case (false, false, true) => Invalid(errorKeys.get(Year).getOrElse(""))
      case (_, _, _) => Valid

  protected def monthYearEmptyValidation(date: NGRMonthYear, errorKeys: Map[DateErrorKeys, String]): ValidationResult =
    (date.month.isEmpty, date.year.isEmpty) match
      case (true, true) => Invalid(errorKeys.get(Required).getOrElse(""))
      case (true, false) => Invalid(errorKeys.get(Month).getOrElse(""))
      case (false, true) => Invalid(errorKeys.get(Year).getOrElse(""))
      case (_, _) => Valid

  protected def dateValidation(date: NGRDate, errorKey: String) =
    if (Try(date.ngrDate).isFailure || (Try(date.ngrDate).isSuccess && date.year.length > 4))
      Invalid(errorKey)
    else
      Valid


  private def monthYearAfter1900Validation(date: NGRMonthYear, errorKey: String) =
    val maybeYear = date.year.toIntOption
    maybeYear match {
      case Some(year) =>
        if (date.year.toInt < 1900)
          Invalid (errorKey)
        else
          Valid
      case None => Invalid(errorKey)
    }

  private def dateAfter1900Validation(date: NGRDate, errorKey: String) =
    if (Try(date.ngrDate).isSuccess && date.year.toInt < 1900)
      Invalid(errorKey)
    else
      Valid

  protected def monthYearValidation(date: NGRMonthYear, errorKey: String) = {
    val maybeMonth = date.month.toIntOption
    val maybeYear = date.year.toIntOption
    (maybeMonth, maybeYear) match {
      case (Some(month), Some(year)) if month >= 0 && month <= 12 && (month + year != 0) =>
        Valid
      case _ =>
        Invalid(errorKey)
    }
  }
}

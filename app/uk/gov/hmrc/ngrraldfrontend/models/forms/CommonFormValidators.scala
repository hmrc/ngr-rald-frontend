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
import uk.gov.hmrc.ngrraldfrontend.models.{DateErrorKeys, Day, DayAndMonth, DayAndYear, Month, MonthAndYear, NGRDate, Required, Year}

import scala.util.Try

trait CommonFormValidators {
  protected def firstError[A](constraints: Constraint[A]*): Constraint[A] =
    Constraint { input =>
      constraints
        .map(_.apply(input))
        .find(_ != Valid)
        .getOrElse(Valid)
    }

  protected def maxLength(maximum: Int, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.length <= maximum =>
        Valid
      case _ =>
        Invalid(errorKey, maximum)
    }

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
      
  protected def dateValidation(date: NGRDate, errorKey: String) =
    if (Try(date.ngrDate).isFailure)
      Invalid(errorKey)
    else
      Valid

  private def dateAfter1900Validation(date: NGRDate, errorKey: String) =
    if (Try(date.ngrDate).isSuccess && date.year.toInt < 1900)
      Invalid(errorKey)
    else
      Valid
}

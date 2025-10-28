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

package uk.gov.hmrc.ngrraldfrontend.models.forms.mappings

import play.api.data.FormError
import play.api.data.Forms.*
import play.api.data.format.Formatter

import java.time.LocalDate
import scala.util.{Right, Try}

/**
 * Handles binding 3 date fields to [[java.time.LocalDate LocalDate]] and unbinding from LocalDate to 3 date fields.
 *
 * @author Yuriy Tumakha
 */
class LocalDateFormatter(
                          errorKeyPrefix: String,
                          extraDateValidations: DateValidation*
                        ) extends Formatter[LocalDate]:

  private val allDateFields = Seq("day", "month", "year")
  private val nineteenHundred = LocalDate.of(1900, 1, 1)

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] =
    single(
      key -> tuple(
        "day" -> optional(text),
        "month" -> optional(text),
        "year" -> optional(text)
      )
    ).bind(data).flatMap {
      case (None, None, None) => oneError(key, "required.error")
      case (Some(d), Some(m), Some(y)) =>
        validateDate(d, m, y, data).left.map { errorKeys =>
          errorKeys.map(errorKey => FormError(key, errorKey))
        }
      case (d, m, y) =>
        val missedFields = (Seq(d, m, y) zip allDateFields).filter(_._1.isEmpty).map(_._2)
        val missedFieldsCapitalized = missedFields.map(v => if v == missedFields.head then v else v.capitalize)
        val focusKey = s"$key.${missedFields.head}"
        val errorKey = missedFieldsCapitalized.mkString("", "And", ".required.error")
        oneError(focusKey, errorKey)
    }

  override def unbind(key: String, value: LocalDate): Map[String, String] =
    Map(
      s"$key.day" -> value.getDayOfMonth.toString,
      s"$key.month" -> value.getMonthValue.toString,
      s"$key.year" -> value.getYear.toString
    )

  private def oneError(key: String, errorTypeKey: String): Left[Seq[FormError], LocalDate] =
    Left(Seq(FormError(key, s"$errorKeyPrefix.$errorTypeKey")))

  private def validateDate(day: String, month: String, year: String, data: Map[String, String]): Either[Seq[String], LocalDate] =
    Try(LocalDate.of(year.toInt, month.toInt, day.toInt)).toEither.left
      .map(_ => Seq(s"$errorKeyPrefix.invalid.error"))
      .flatMap { date =>
        val extraValidationErrorKeys = extraDateValidations.flatMap { dv =>
          if dv.validateDateAndData(date, data) then None else Some(s"$errorKeyPrefix.${dv.errorTypeKey}")
        }

        if date.isBefore(nineteenHundred) then Left(Seq(s"$errorKeyPrefix.before.1900.error"))
        else if extraValidationErrorKeys.nonEmpty then Left(extraValidationErrorKeys)
        else Right(date)
      }

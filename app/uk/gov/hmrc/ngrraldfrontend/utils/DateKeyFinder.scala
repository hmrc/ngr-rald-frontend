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

package uk.gov.hmrc.ngrraldfrontend.utils

import java.time.LocalDate
import java.time.temporal.TemporalAdjusters.*
import java.util.regex.Pattern
import play.api.data.FormError

trait DateKeyFinder {
  private val dayOrMonthRegexPattern: Pattern = Pattern.compile("^[0-9]{1,2}$")

  def findKey(data: Map[String, String], fieldName: String): String = {
    val dayOpt: Option[String] = data.get(s"$fieldName.day")
    val monthOpt: Option[String] = data.get(s"$fieldName.month")
    val yearOpt: Option[String] = data.get(s"$fieldName.year")
    if (dayOpt.exists(day => isDayInvalid(day, monthOpt)))
      s"$fieldName.day"
    else if (monthOpt.exists(isMonthInvalid))
      s"$fieldName.month"
    else
      s"$fieldName.year"
  }

  private def isDayInvalid(day: String, monthOpt: Option[String]): Boolean =
    !dayOrMonthRegexPattern.matcher(day).matches() ||
      (dayOrMonthRegexPattern.matcher(day).matches() && !isMonthInvalid(monthOpt.getOrElse("")) && isDayOutOfTheRange(day, monthOpt.get))

  private def isMonthInvalid(month: String): Boolean =
    !dayOrMonthRegexPattern.matcher(month).matches() ||
      (dayOrMonthRegexPattern.matcher(month).matches() && isMonthOutOfTheRange(month))

  private def isDayOutOfTheRange(day: String, month: String): Boolean = day.toInt < 1 ||
    day.toInt > LocalDate.now().withMonth(month.toInt).`with`(lastDayOfMonth()).getDayOfMonth

  private def isMonthOutOfTheRange(month: String): Boolean = month.toInt < 1 || month.toInt > 12

  def setCorrectKey(formError: FormError, pageName: String, fieldName: String): FormError =
    (formError.key, formError.messages.head) match
      case (key, message) if message.contains(s"$pageName.$fieldName.day.required.error") =>
        formError.copy(key = s"$fieldName.day")
      case (key, message) if message.contains(s"$pageName.$fieldName.month.required.error") =>
        formError.copy(key = s"$fieldName.month")
      case (key, message) if message.contains(s"$pageName.$fieldName.year.required.error") ||
        message.contains(s"$pageName.$fieldName.before.1900.error") =>
        formError.copy(key = s"$fieldName.year")
      case ("", message) if message.contains(s"$pageName.$fieldName.required.error") ||
        message.contains(s"$pageName.$fieldName.invalid.error") || message.contains(s"$pageName.$fieldName.before.startDate.error") =>
        formError.copy(key = s"$fieldName")
      case _ =>
        formError
}

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

package uk.gov.hmrc.ngrraldfrontend.models

import play.api.data.Forms.{mapping, text}
import play.api.data.Mapping
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate
import java.time.format.{DateTimeFormatter, TextStyle}
import java.util.Locale

final case class NGRMonthYear(month: String, year: String) {
  def makeString: String = {
    val monthStr = f"${month.toIntOption.getOrElse(0)}%02d"
    s"$year-$monthStr"
  }
}

object NGRMonthYear {
  implicit val format: OFormat[NGRMonthYear] = Json.format[NGRMonthYear]
  
  def formatDate(dateString: String): String = {
    val date = LocalDate.parse(dateString)
    val outputFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.UK)
    date.format(outputFormatter)
  }

  def fromString(dateString: String): NGRMonthYear = {
    val parts = dateString.split("-").map(_.toInt)
    val year = parts(0).toString
    val month = f"${parts(1)}%02d"
    NGRMonthYear(month, year)
  }


  def unapply(ngrMonthYear: NGRMonthYear): Option[(String, String)] =
    Some(ngrMonthYear.month, ngrMonthYear.year)
}

trait MonthYearMappings {
  def monthYearMapping: Mapping[NGRMonthYear] = {
    mapping(
      "month" -> text().transform(_.strip(), identity),
      "year" -> text().transform(_.strip(), identity),
    )(NGRMonthYear.apply)(NGRMonthYear.unapply)
  }
}
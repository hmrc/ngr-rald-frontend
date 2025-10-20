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

final case class NGRDate(day: String, month: String, year: String) {
  lazy val ngrDate: LocalDate = LocalDate.of(year.toInt, month.toInt, day.toInt)

  def makeString: String = {
    val monthStr = f"${month.toInt}%02d"
    val dayStr = f"${day.toInt}%02d"
    s"$year-$monthStr-$dayStr"
  }

  def toLocalDate: LocalDate = LocalDate.of(year.toInt, month.toInt, day.toInt)
}

object NGRDate {
  implicit val format: OFormat[NGRDate] = Json.format[NGRDate]

  def formatDate(dateString: String): String = {
    val date = LocalDate.parse(dateString)
    val outputFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)
    date.format(outputFormatter)
  }

  def fromLocalDate(date: LocalDate): NGRDate =
    NGRDate(date.getDayOfMonth.toString, date.getMonthValue.toString, date.getYear.toString)

  def fromString(dateString: String): NGRDate =
    fromLocalDate(LocalDate.parse(dateString))

  def unapply(ngrDate: NGRDate): Option[(String, String, String)] =
    Some(ngrDate.day, ngrDate.month, ngrDate.year)
}

trait DateMappings {
  def dateMapping: Mapping[NGRDate] = {
    mapping(
      "day" -> text().transform(_.strip(), identity),
      "month" -> text().transform(_.strip(), identity),
      "year" -> text().transform(_.strip(), identity),
    )(NGRDate.apply)(NGRDate.unapply)
  }
}

sealed trait DateErrorKeys

case object Day extends DateErrorKeys

case object Month extends DateErrorKeys

case object Year extends DateErrorKeys

case object DayAndMonth extends DateErrorKeys

case object MonthAndYear extends DateErrorKeys

case object DayAndYear extends DateErrorKeys

case object Required extends DateErrorKeys

def errorKeys(pageName: String, whichDate: String): Map[DateErrorKeys, String] = Map(
  Required     -> s"$pageName.$whichDate.required.error",
  DayAndMonth  -> s"$pageName.$whichDate.dayAndMonth.required.error",
  DayAndYear   -> s"$pageName.$whichDate.dayAndYear.required.error",
  MonthAndYear -> s"$pageName.$whichDate.monthAndYear.required.error",
  Day          -> s"$pageName.$whichDate.day.required.error",
  Month        -> s"$pageName.$whichDate.month.required.error",
  Year         -> s"$pageName.$whichDate.year.required.error"
)

def unbindNGRDate(key: String, ngrDate: Option[NGRDate]): Map[String, String] =
  Map(
    s"$key.day"   -> ngrDate.map(_.day).getOrElse(""),
    s"$key.month" -> ngrDate.map(_.month).getOrElse(""),
    s"$key.year"  -> ngrDate.map(_.year).getOrElse("")
  )
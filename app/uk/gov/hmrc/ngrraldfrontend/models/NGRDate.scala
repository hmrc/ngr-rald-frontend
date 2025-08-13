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

final case class NGRDate(day: String, month: String, year: String) {
  lazy val ngrDate: LocalDate = LocalDate.of(year.toInt, month.toInt, day.toInt)

  def makeString: String = {
    s"$year-$month-$day"
  }
}

object NGRDate {
  implicit val format: OFormat[NGRDate] = Json.format[NGRDate]

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
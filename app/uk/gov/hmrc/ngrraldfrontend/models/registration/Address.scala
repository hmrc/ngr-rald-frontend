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

package uk.gov.hmrc.ngrraldfrontend.models.registration

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.ngrraldfrontend.models.Postcode

final case class Address(line1: String,
                         line2: Option[String],
                         town: String,
                         county: Option[String],
                         postcode: Postcode,
                        ) {
  override def toString: String = Seq(line1, line2.getOrElse(""), town, county.getOrElse(""), postcode.toString).filter(_.nonEmpty).mkString(", ")
}

object Address {
  implicit val format: OFormat[Address] = Json.format[Address]
}
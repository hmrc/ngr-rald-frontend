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

import play.api.data.*
import play.api.data.Forms.*
import play.api.data.format.Formatter
import play.api.libs.json.{Json, OFormat}

import scala.util.Try


final case class HowMuchIsTotalAnnualRentForm(annualRent: BigDecimal)

object HowMuchIsTotalAnnualRentForm {
  implicit val format: OFormat[HowMuchIsTotalAnnualRentForm] = Json.format[HowMuchIsTotalAnnualRentForm]

  private lazy val annualRent = "how–much–is–total–annual–rent-value"
  private lazy val annualRentEmptyError  = "howMuchIsTotalAnnualRent.empty.error"
  private lazy val annualRentMaxError    = "howMuchIsTotalAnnualRent.tooLarge.error"
  private lazy val annualRentFormatError = "howMuchIsTotalAnnualRent.format.error"


  def unapply(howMuchIsTotalAnnualRentForm: HowMuchIsTotalAnnualRentForm): Option[BigDecimal] = Some(howMuchIsTotalAnnualRentForm.annualRent)
  
  def bigDecimalWithFormatError: Formatter[BigDecimal] = new Formatter[BigDecimal] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], BigDecimal] = {
      data.get(key).filter(_.nonEmpty) match {
        case Some(value) =>
          Try(BigDecimal(value)).toEither.left.map(_ =>
            Seq(FormError(key, annualRentFormatError))
          )
        case None =>
          Left(Seq(FormError(key, annualRentEmptyError)))
      }
    }
    override def unbind(key: String, value: BigDecimal): Map[String, String] =
      Map(key -> value.toString())
  }

  val annualRentFormMapping: (String, Mapping[BigDecimal]) =
    annualRent -> of(bigDecimalWithFormatError)
      .verifying(annualRentMaxError, _ <= BigDecimal("9999999.99"))

  val form: Form[HowMuchIsTotalAnnualRentForm] = Form(
    mapping(
      annualRentFormMapping
    )(HowMuchIsTotalAnnualRentForm.apply)(HowMuchIsTotalAnnualRentForm.unapply)
  )
  
}


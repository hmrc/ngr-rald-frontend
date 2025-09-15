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
import uk.gov.hmrc.ngrraldfrontend.models.*

import scala.util.Try


final case class InterimRentSetByTheCourtForm(amount: BigDecimal, date: NGRMonthYear)

object InterimRentSetByTheCourtForm extends CommonFormValidators with MonthYearMappings {
  implicit val format: OFormat[InterimRentSetByTheCourtForm] = Json.format[InterimRentSetByTheCourtForm]

  private lazy val howMuch = "howMuch"
  private lazy val howMuchEmptyError  = "howMuch.empty.error"
  private lazy val howMuchMaxError    = "howMuch.tooLarge.error"
  private lazy val howMuchFormatError = "howMuch.format.error"
  private val dateInput = "interimRentSetByTheCourt.date"

  def unapply(interimRentSetByTheCourtForm: InterimRentSetByTheCourtForm): Option[(BigDecimal, NGRMonthYear)] = Some(interimRentSetByTheCourtForm.amount, interimRentSetByTheCourtForm.date)

  private def errorKeys(whichDate: String): Map[DateErrorKeys, String] = Map(
    Required -> s"agreementVerbal.$whichDate.required.error",
    MonthAndYear -> s"agreementVerbal.$whichDate.monthAndYear.required.error",
    Month -> s"agreementVerbal.$whichDate.month.required.error",
    Year -> s"agreementVerbal.$whichDate.year.required.error"
  )
  
  def bigDecimalWithFormatError: Formatter[BigDecimal] = new Formatter[BigDecimal] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], BigDecimal] = {
      data.get(key).filter(_.nonEmpty) match {
        case Some(value) =>
          Try(BigDecimal(value)).toEither.left.map(_ =>
            Seq(FormError(key, howMuchFormatError))
          )
        case None =>
          Left(Seq(FormError(key, howMuchEmptyError)))
      }
    }
    override def unbind(key: String, value: BigDecimal): Map[String, String] =
      Map(key -> value.toString())
  }

  val annualRentFormMapping: (String, Mapping[BigDecimal]) =
    howMuch -> of(bigDecimalWithFormatError)
      .verifying(howMuchMaxError, _ <= BigDecimal("9999999.99"))

  val form: Form[InterimRentSetByTheCourtForm] = Form(
    mapping(
      annualRentFormMapping,
      dateInput -> monthYearMapping
        .verifying(
          firstError(
            isMonthYearEmpty(errorKeys("startDate")),
            isMonthYearValid("agreement.startDate.format.error")
          )
        ),
    )(InterimRentSetByTheCourtForm.apply)(InterimRentSetByTheCourtForm.unapply)
  )

}



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
import uk.gov.hmrc.ngrraldfrontend.models.forms.ProvideDetailsOfFirstSecondRentPeriodForm.isDateAfter1900
import uk.gov.hmrc.ngrraldfrontend.models.{Month, *}

import scala.util.Try


final case class InterimRentSetByTheCourtForm(amount: BigDecimal, date: NGRMonthYear)

object InterimRentSetByTheCourtForm extends CommonFormValidators with MonthYearMappings {
  implicit val format: OFormat[InterimRentSetByTheCourtForm] = Json.format[InterimRentSetByTheCourtForm]

  private lazy val howMuch = "howMuch"
  private lazy val howMuchEmptyError  = "interimRentSetByTheCourt.howMany.required.error"
  private lazy val howMuchMaxError    = "interimRentSetByTheCourt.howMany.tooLarge.error"
  private lazy val howMuchFormatError = "interimRentSetByTheCourt.howMany.format.error"
  private val dateInput = "date"

  def unapply(interimRentSetByTheCourtForm: InterimRentSetByTheCourtForm): Option[(BigDecimal, NGRMonthYear)] = Some(interimRentSetByTheCourtForm.amount, interimRentSetByTheCourtForm.date)

  private def errorKeys(whichDate: String): Map[DateErrorKeys, String] = Map(
    Required -> s"$whichDate.required.error",
    Month -> s"$whichDate.month.required.error",
    Year -> s"$whichDate.year.required.error"
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
      "date" -> monthYearMapping
        .verifying(
          firstError(
            isMonthYearEmpty(errorKeys("interimRentSetByTheCourt")),
            isMonthYearValid("interimRentSetByTheCourt.monthYear.format.error"),
            isMonthYearAfter1900("interimRentSetByTheCourt.startDate.before.1900.error")
          )
        ),
    )(InterimRentSetByTheCourtForm.apply)(InterimRentSetByTheCourtForm.unapply)
  )

}



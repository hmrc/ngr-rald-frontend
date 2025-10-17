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
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings

import scala.math.BigDecimal.RoundingMode


final case class InterimRentSetByTheCourtForm(amount: BigDecimal, date: NGRMonthYear)

object InterimRentSetByTheCourtForm extends Mappings with MonthYearMappings {
  implicit val format: OFormat[InterimRentSetByTheCourtForm] = Json.format[InterimRentSetByTheCourtForm]

  private lazy val howMuchEmptyError  = "interimRentSetByTheCourt.interimAmount.required.error"
  private lazy val howMuchMaxError    = "interimRentSetByTheCourt.interimAmount.tooLarge.error"
  private lazy val howMuchFormatError = "interimRentSetByTheCourt.interimAmount.format.error"

  def unapply(interimRentSetByTheCourtForm: InterimRentSetByTheCourtForm): Option[(BigDecimal, NGRMonthYear)] = Some(interimRentSetByTheCourtForm.amount, interimRentSetByTheCourtForm.date)

  val form: Form[InterimRentSetByTheCourtForm] = Form(
    mapping(
      "interimAmount" -> text()
        .transform[String](_.strip().replaceAll("[£|,|\\s]", ""), identity)
        .verifying(
          firstError(
            isNotEmpty("interimAmount", howMuchEmptyError),
            regexp(amountRegex.pattern(), howMuchFormatError)
          )
        )
        .transform[BigDecimal](BigDecimal(_).setScale(2, RoundingMode.HALF_UP), _.toString)
        .verifying(
          maximumValue[BigDecimal](BigDecimal("9999999.99"), howMuchMaxError)
        ),
      "date" -> monthYearMapping
        .verifying(
          firstError(
            isMonthYearEmpty(errorKeys("interimRentSetByTheCourt", "date")),
            isMonthYearValid("interimRentSetByTheCourt.date.invalid.error"),
            isMonthYearAfter1900("interimRentSetByTheCourt.date.before.1900.error")
          )
        ),
    )(InterimRentSetByTheCourtForm.apply)(InterimRentSetByTheCourtForm.unapply)
  )

}



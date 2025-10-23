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

import play.api.data.Form
import play.api.data.Forms.*
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.ngrraldfrontend.models.*

import scala.math.BigDecimal.RoundingMode

final case class AboutRepairsAndFittingOutForm(
                                                date: NGRMonthYear,
                                                cost: BigDecimal
                                               
                                              )

object AboutRepairsAndFittingOutForm extends CommonFormValidators with MonthYearMappings {
  implicit val format: OFormat[AboutRepairsAndFittingOutForm] = Json.format[AboutRepairsAndFittingOutForm]
  
  private lazy val dateMissingError    = "aboutRepairsAndFittingOut.date.error.missing"
  private lazy val datePartialError    = "aboutRepairsAndFittingOut.date.error.partial"
  private lazy val dateInvalidError    = "aboutRepairsAndFittingOut.date.error.invalid"
  private lazy val dateBefore1900Error = "aboutRepairsAndFittingOut.date.error.1900"
  
  private lazy val costEmptyError      = "aboutRepairsAndFittingOut.cost.error.missing"
  private lazy val costFormatError     = "aboutRepairsAndFittingOut.cost.error.nonNumeric"
  private lazy val costMaxError        = "aboutRepairsAndFittingOut.cost.error.exceed"



  def unapply(form: AboutRepairsAndFittingOutForm): Option[(NGRMonthYear, BigDecimal )] =
    Some(form.date, form.cost )

  private val dateErrorKeys: Map[DateErrorKeys, String] = Map(
    Required -> dateMissingError,
    Month    -> datePartialError,
    Year     -> datePartialError
  )

  val form: Form[AboutRepairsAndFittingOutForm] = Form(
    mapping(

      "date" -> monthYearMapping.verifying(
        firstError(
          isMonthYearEmpty(dateErrorKeys),
          isMonthYearValid(dateInvalidError),
          isMonthYearAfter1900(dateBefore1900Error)
        )
      ),
      "cost" -> text()
        .transform[String](_.strip().replaceAll("[£|\\s]", "").replace(",", "."), identity)
        .verifying(
          firstError(
            isNotEmpty("aboutRepairsAndFittingOut", costEmptyError),
            regexp(amountRegex.pattern(), costFormatError)
          )
        )
        .transform[BigDecimal](BigDecimal(_).setScale(2, RoundingMode.HALF_UP), _.toString)
        .verifying(
          maximumValue[BigDecimal](BigDecimal("9999999.99"), costMaxError)
        )
    )(AboutRepairsAndFittingOutForm.apply)(AboutRepairsAndFittingOutForm.unapply)
  )
}









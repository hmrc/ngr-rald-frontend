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
  def unapply(form: AboutRepairsAndFittingOutForm): Option[(NGRMonthYear, BigDecimal )] =
    Some(form.date, form.cost )

  val form: Form[AboutRepairsAndFittingOutForm] = Form(
    mapping(
      "date" -> monthYearMapping
        .verifying(
          firstError(
            isMonthYearEmpty(errorKeys("aboutRepairsAndFittingOut", "date")),
            isMonthYearValid("aboutRepairsAndFittingOut.date.invalid.error"),
            isMonthYearAfter1900("aboutRepairsAndFittingOut.date.before.1900.error")
          )
        ),
      "cost" -> text()
        .transform[String](_.strip().replaceAll("[£|,|\\s]", ""), identity) 
        .verifying(
          firstError(
            isNotEmpty("cost", "aboutRepairsAndFittingOut.cost.error.missing"),
            regexp(amountRegex.pattern(), "aboutRepairsAndFittingOut.cost.error.nonNumeric")
          )
        )
        .transform[BigDecimal](BigDecimal(_).setScale(2, RoundingMode.HALF_UP), _.toString)
        .verifying(
          maximumValue[BigDecimal](BigDecimal("9999999.99"), "aboutRepairsAndFittingOut.cost.error.exceed")
        )
    )(AboutRepairsAndFittingOutForm.apply)(AboutRepairsAndFittingOutForm.unapply)
  )
}











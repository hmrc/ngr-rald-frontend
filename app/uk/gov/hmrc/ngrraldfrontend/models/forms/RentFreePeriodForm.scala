/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.data.Forms.{mapping, text}
import play.api.data.Form
import play.api.data.validation.Constraint
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.ngrraldfrontend.models.*

final case class RentFreePeriodForm(rentFreePeriodMonths: Int, reasons: String)

object RentFreePeriodForm extends CommonFormValidators {
  implicit val format: OFormat[RentFreePeriodForm] = Json.format[RentFreePeriodForm]

  private val rentFreePeriodMonths =  "rentFreePeriodMonths"
  private val reasons = "reasons"

  def unapply(rentFreePeriodForm: RentFreePeriodForm): Option[(Int, String)] =
    Some(rentFreePeriodForm.rentFreePeriodMonths, rentFreePeriodForm.reasons)

  def form: Form[RentFreePeriodForm] = {
    Form(
      mapping(
        rentFreePeriodMonths -> text()
          .transform[String](_.strip(), identity)
          .verifying(
            firstError(
              isNotEmpty(rentFreePeriodMonths, "rentFreePeriod.months.required.error"),
              regexp(wholePositiveNumberRegexp.pattern(), "rentFreePeriod.months.invalid.error"),
              isLargerThanInt(99, "rentFreePeriod.months.maximum.error")
            )
          )
          .transform[Int](_.toInt, _.toString)
          .verifying(
            firstError(
              minimumValue(1, "rentFreePeriod.months.minimum.error"),
              maximumValue(99, "rentFreePeriod.months.maximum.error")
            )
          ),
        reasons -> text()
          .verifying(
            isNotEmpty(reasons, "rentFreePeriod.reasons.required.error")
          )
      )(RentFreePeriodForm.apply)(RentFreePeriodForm.unapply)
    )
  }

}




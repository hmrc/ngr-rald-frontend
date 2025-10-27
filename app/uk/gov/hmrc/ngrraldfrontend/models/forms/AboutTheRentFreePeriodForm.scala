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

import scala.math.BigDecimal.RoundingMode


final case class AboutTheRentFreePeriodForm(howManyMonths: Int, date: NGRDate)

object AboutTheRentFreePeriodForm extends CommonFormValidators with DateMappings {
  implicit val format: OFormat[AboutTheRentFreePeriodForm] = Json.format[AboutTheRentFreePeriodForm]

  private lazy val howMuchEmptyError = "aboutTheRentFreePeriod.months.required.error"
  private lazy val howMuchMaxError = "aboutTheRentFreePeriod.months.tooLarge.error"
  private lazy val howMuchFormatError = "aboutTheRentFreePeriod.months.format.error"

  def unapply(aboutTheRentFreePeriodForm: AboutTheRentFreePeriodForm): Option[(Int, NGRDate)] = Some(aboutTheRentFreePeriodForm.howManyMonths, aboutTheRentFreePeriodForm.date)

  val form: Form[AboutTheRentFreePeriodForm] = Form(
    mapping(
      "howManyMonths" -> text()
        .transform[String](_.strip(), identity)
        .verifying(
          firstError(
            isNotEmpty("howManyMonths", "aboutTheRentFreePeriod.months.required.error"),
            regexp(wholePositiveNumberRegexp.pattern(), "aboutTheRentFreePeriod.months.invalid.error"),
            isLargerThanInt(99, "aboutTheRentFreePeriod.months.maximum.error")
          )
        )
        .transform[Int](_.toInt, _.toString)
        .verifying(
          firstError(
            minimumValue(1, "aboutTheRentFreePeriod.months.minimum.error")
          )
        ),
      "date" -> dateMapping
        .verifying(
          firstError(
            isDateEmpty(errorKeys("aboutTheRentFreePeriod", "date")),
            isDateValid("aboutTheRentFreePeriod.date.invalid.error"),
            isDateAfter1900("aboutTheRentFreePeriod.date.before.1900.error")
          )
        )
    )(AboutTheRentFreePeriodForm.apply)(AboutTheRentFreePeriodForm.unapply)
  )

}



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


final case class MoneyYouPaidInAdvanceToLandlordForm(amount: BigDecimal, date: NGRDate)

object MoneyYouPaidInAdvanceToLandlordForm extends CommonFormValidators with DateMappings {
  implicit val format: OFormat[MoneyYouPaidInAdvanceToLandlordForm] = Json.format[MoneyYouPaidInAdvanceToLandlordForm]

  private lazy val howMuchEmptyError  = "moneyYouPaidInAdvanceToLandlord.advanceMoney.required.error"
  private lazy val howMuchMaxError    = "moneyYouPaidInAdvanceToLandlord.advanceMoney.tooLarge.error"
  private lazy val howMuchFormatError = "moneyYouPaidInAdvanceToLandlord.advanceMoney.format.error"

  def unapply(moneyYouPaidInAdvanceToLandlordForm: MoneyYouPaidInAdvanceToLandlordForm): Option[(BigDecimal, NGRDate)] = Some(moneyYouPaidInAdvanceToLandlordForm.amount, moneyYouPaidInAdvanceToLandlordForm.date)

  val form: Form[MoneyYouPaidInAdvanceToLandlordForm] = Form(
    mapping(
      "advanceMoney" -> text()
        .transform[String](_.strip().replaceAll("[£|,|\\s]", ""), identity)
        .verifying(
          firstError(
            isNotEmpty("moneyYouPaidInAdvanceToLandlord", howMuchEmptyError),
            regexp(amountRegex.pattern(), howMuchFormatError)
          )
        )
        .transform[BigDecimal](BigDecimal(_).setScale(2, RoundingMode.HALF_UP), _.toString)
        .verifying(
          maximumValue[BigDecimal](BigDecimal("9999999.99"), howMuchMaxError)
        ),
      "date" -> dateMapping
        .verifying(
          firstError(
            isDateEmpty(errorKeys("moneyYouPaidInAdvanceToLandlord", "date")),
            isDateValid("moneyYouPaidInAdvanceToLandlord.date.invalid.error"),
            isDateAfter1900("moneyYouPaidInAdvanceToLandlord.date.before.1900.error")
          )
        )
    )(MoneyYouPaidInAdvanceToLandlordForm.apply)(MoneyYouPaidInAdvanceToLandlordForm.unapply)
  )

}



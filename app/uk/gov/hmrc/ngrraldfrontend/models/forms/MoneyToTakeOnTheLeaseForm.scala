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
import play.api.data.Forms.{mapping, text}
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.ngrraldfrontend.models.{DateMappings, NGRDate, errorKeys}
import uk.gov.hmrc.ngrraldfrontend.models.forms.MoneyToTakeOnTheLeaseForm.dateMapping

import scala.math.BigDecimal.RoundingMode

final case class MoneyToTakeOnTheLeaseForm(amount: BigDecimal, date: NGRDate)

object MoneyToTakeOnTheLeaseForm extends CommonFormValidators with DateMappings {
  implicit val format: OFormat[MoneyToTakeOnTheLeaseForm] = Json.format[MoneyToTakeOnTheLeaseForm]

  private lazy val howMuchEmptyError  = "moneyToTakeOnTheLease.amount.required.error"
  private lazy val howMuchMaxError    = "moneyToTakeOnTheLease.amount.tooLarge.error"
  private lazy val howMuchFormatError = "moneyToTakeOnTheLease.amount.format.error"

  def unapply(moneyToTakeOnTheLeaseForm: MoneyToTakeOnTheLeaseForm): Option[(BigDecimal, NGRDate)] = Some(moneyToTakeOnTheLeaseForm.amount, moneyToTakeOnTheLeaseForm.date)

  val form: Form[MoneyToTakeOnTheLeaseForm] = Form(
    mapping(
      "amount" -> text()
        .transform[String](_.strip().replaceAll("[£|,|\\s]", ""), identity)
        .verifying(
          firstError(
            isNotEmpty("moneyToTakeOnTheLease", howMuchEmptyError),
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
            isDateEmpty(errorKeys("moneyToTakeOnTheLease", "date")),
            isDateValid("moneyToTakeOnTheLease.date.invalid.error"),
            isDateAfter1900("moneyToTakeOnTheLease.date.before.1900.error")
          )
        )
    )(MoneyToTakeOnTheLeaseForm.apply)(MoneyToTakeOnTheLeaseForm.unapply)
  )

}
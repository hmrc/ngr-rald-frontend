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
import play.api.i18n.*
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.dateinput.DateInput
import uk.gov.hmrc.ngrraldfrontend.models.*

import java.time.LocalDate
import scala.math.BigDecimal.RoundingMode

case class ProvideDetailsOfSecondRentPeriodForm(endDate: NGRDate, rentPeriodAmount: BigDecimal)

object ProvideDetailsOfSecondRentPeriodForm extends CommonFormValidators with NGRDateInput with DateMappings {

  implicit val format: OFormat[ProvideDetailsOfSecondRentPeriodForm] = Json.format[ProvideDetailsOfSecondRentPeriodForm]

  private val endDate = "endDate"
  private val rentPeriodAmount = "rentPeriodAmount"


  def unapply(provideDetailsOfSecondRentPeriodForm: ProvideDetailsOfSecondRentPeriodForm): Option[(NGRDate, BigDecimal)] =
    Some(provideDetailsOfSecondRentPeriodForm.endDate, provideDetailsOfSecondRentPeriodForm.rentPeriodAmount)

  def answerToForm(provideDetailsOfSecondRentPeriod: ProvideDetailsOfRentPeriod, previousEndDate: LocalDate): Form[ProvideDetailsOfSecondRentPeriodForm] =
    form(previousEndDate).fill(
      ProvideDetailsOfSecondRentPeriodForm(
        endDate = NGRDate.fromString(provideDetailsOfSecondRentPeriod.endDate),
        rentPeriodAmount = provideDetailsOfSecondRentPeriod.rentPeriodAmount,
      )
    )

  def formToAnswers(provideDetailsOfSecondRentPeriodForm: ProvideDetailsOfSecondRentPeriodForm, rentPeriods: Seq[ProvideDetailsOfRentPeriod], index: Int): Seq[ProvideDetailsOfRentPeriod] = {
    val element = ProvideDetailsOfRentPeriod(
      provideDetailsOfSecondRentPeriodForm.endDate.makeString,
      provideDetailsOfSecondRentPeriodForm.rentPeriodAmount
    )
    println(Console.BLUE_B + rentPeriods + "============" + (rentPeriods.nonEmpty && rentPeriods.size > index) + "=======" + index + Console.RESET)
    if (rentPeriods.nonEmpty && rentPeriods.size > index)
      val newSeq = rentPeriods.updated(index, element)
      println(Console.GREEN_B + newSeq + Console.RESET)
      newSeq
    else
      val newSeq = rentPeriods :+ element
      println(Console.RED_B + newSeq + Console.RESET)
      newSeq
  }

  def endDateInput(index: Int)(using messages: Messages): DateInput =
    dateInput(endDate, "provideDetailsOfSecondRentPeriod.endDate.label", Some(s"rentPeriod.${index + 2}.title"))


  def form(previousEndDate: LocalDate): Form[ProvideDetailsOfSecondRentPeriodForm] = {
    Form(
      mapping(
        endDate -> dateMapping
          .verifying(
            firstError(
              isDateEmpty(errorKeys("provideDetailsOfSecondRentPeriod", "endDate")),
              isDateValid("provideDetailsOfSecondRentPeriod.endDate.invalid.error"),
              isDateAfter1900("provideDetailsOfSecondRentPeriod.endDate.before.1900.error"),
              isDateBeforeStartDate("provideDetailsOfSecondRentPeriod.endDate.before.startDate.error", NGRDate.fromLocalDate(previousEndDate))
            )
          ),
        rentPeriodAmount -> text()
          .transform[String](_.strip().replaceAll("[£|,|\\s]", ""), identity)
          .verifying(
            firstError(
              isNotEmpty(rentPeriodAmount, "provideDetailsOfSecondRentPeriod.rentPeriodAmount.required.error"),
              regexp(amountRegex.pattern(), "provideDetailsOfSecondRentPeriod.rentPeriodAmount.invalid.error")
            )
          )
          .transform[BigDecimal](BigDecimal(_).setScale(2, RoundingMode.HALF_UP), _.toString)
          .verifying(
            maximumValue[BigDecimal](BigDecimal("9999999.99"), "provideDetailsOfSecondRentPeriod.rentPeriodAmount.max.error")
          )
      )(ProvideDetailsOfSecondRentPeriodForm.apply)(ProvideDetailsOfSecondRentPeriodForm.unapply)
    )
  }
}
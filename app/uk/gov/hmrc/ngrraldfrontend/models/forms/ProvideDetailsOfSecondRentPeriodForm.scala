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

  def answerToForm(provideDetailsOfSecondRentPeriod: DetailsOfRentPeriod, previousEndDate: LocalDate, index: Int)(implicit messages: Messages): Form[ProvideDetailsOfSecondRentPeriodForm] =
    form(previousEndDate, index).fill(
      ProvideDetailsOfSecondRentPeriodForm(
        endDate = NGRDate.fromString(provideDetailsOfSecondRentPeriod.endDate),
        rentPeriodAmount = provideDetailsOfSecondRentPeriod.rentPeriodAmount,
      )
    )

  def formToAnswers(provideDetailsOfSecondRentPeriodForm: ProvideDetailsOfSecondRentPeriodForm, rentPeriods: Seq[DetailsOfRentPeriod], index: Int): Seq[DetailsOfRentPeriod] = {
    val element = DetailsOfRentPeriod(
      provideDetailsOfSecondRentPeriodForm.endDate.makeString,
      provideDetailsOfSecondRentPeriodForm.rentPeriodAmount
    )
    
    if (rentPeriods.nonEmpty && rentPeriods.size > index)
      rentPeriods.updated(index, element)
    else
      rentPeriods :+ element
  }

  def endDateInput(index: Int)(implicit messages: Messages): DateInput =
    dateInput(endDate, "provideDetailsOfSecondRentPeriod.endDate.label", Some(s"rentPeriod.${index + 2}.sequence"))


  def form(previousEndDate: LocalDate, index: Int)(implicit messages: Messages): Form[ProvideDetailsOfSecondRentPeriodForm] = {
    val periodSequence: Option[String] = Some(messages(s"rentPeriod.${index + 2}.sequence").toLowerCase)
    Form(
      mapping(
        endDate -> dateMapping
          .verifying(
            firstError(
              isDateEmpty(errorKeys("provideDetailsOfSecondRentPeriod", "endDate"), periodSequence),
              isDateValid("provideDetailsOfSecondRentPeriod.endDate.invalid.error", periodSequence),
              isDateAfter1900("provideDetailsOfSecondRentPeriod.endDate.before.1900.error", periodSequence),
              isDateBeforeStartDate("provideDetailsOfSecondRentPeriod.endDate.before.startDate.error", NGRDate.fromLocalDate(previousEndDate))
            )
          ),
        rentPeriodAmount -> text()
          .transform[String](_.strip().replaceAll("[£|,|\\s]", ""), identity)
          .verifying(
            firstError(
              isNotEmpty(rentPeriodAmount, "provideDetailsOfSecondRentPeriod.rentPeriodAmount.required.error", periodSequence),
              regexp(amountRegex.pattern(), "provideDetailsOfSecondRentPeriod.rentPeriodAmount.invalid.error", periodSequence)
            )
          )
          .transform[BigDecimal](BigDecimal(_).setScale(2, RoundingMode.HALF_UP), _.toString)
          .verifying(
            maximumValue[BigDecimal](BigDecimal("9999999.99"), "provideDetailsOfSecondRentPeriod.rentPeriodAmount.max.error", periodSequence)
          )
      )(ProvideDetailsOfSecondRentPeriodForm.apply)(ProvideDetailsOfSecondRentPeriodForm.unapply)
    )
  }
}
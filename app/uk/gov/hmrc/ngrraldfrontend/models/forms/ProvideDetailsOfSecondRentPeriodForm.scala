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
import uk.gov.hmrc.govukfrontend.views.viewmodels.dateinput.DateInput
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.{CompareWithAnotherDateValidation, DateValidation, Mappings}
import scala.math.BigDecimal.RoundingMode

import java.time.LocalDate

object ProvideDetailsOfSecondRentPeriodForm extends CommonFormValidators with NGRDateInput with Mappings:

  private val endDate = "endDate"
  private val rentPeriodAmount = "provideDetailsOfSecondRentPeriod"
  private lazy val rentPeriodAmountEmptyError = "provideDetailsOfSecondRentPeriod.empty.error"
  private lazy val rentPeriodAmountMaxError = "provideDetailsOfSecondRentPeriod.amount.max.error"
  private lazy val rentPeriodAmountFormatError = "provideDetailsOfSecondRentPeriod.format.error"

  def endDateInput(using messages: Messages): DateInput =
    dateInput(endDate, "provideDetailsOfSecondRentPeriod.endDate.label")


  val form: Form[ProvideDetailsOfSecondRentPeriod] =
    Form(
      mapping(
        endDate -> dateMapping("provideDetailsOfSecondRentPeriod.endDate"),
        rentPeriodAmount -> money(rentPeriodAmount)
      )(ProvideDetailsOfSecondRentPeriod.apply)(o => Some(
        o.endDate,
        o.rentPeriodAmount
      ))
    )

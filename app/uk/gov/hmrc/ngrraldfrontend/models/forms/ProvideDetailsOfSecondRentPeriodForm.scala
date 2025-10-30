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
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.dateinput.DateInput
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.{Fieldset, Legend}
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.input.PrefixOrSuffix
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.{ngrRadio, noButton, yesButton}
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.{CompareWithAnotherDateValidation, DateValidation, Mappings}
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText
import scala.math.BigDecimal.RoundingMode

import java.time.LocalDate

object ProvideDetailsOfSecondRentPeriodForm extends CommonFormValidators with NGRDateInput with Mappings:

  private val endDate = "endDate"
  private val rentPeriodAmount = "provideDetailsOfSecondRentPeriod.rentPeriodAmount"
  private lazy val rentPeriodAmountEmptyError = "provideDetailsOfSecondRentPeriod.rentPeriodAmount.empty.error"
  private lazy val rentPeriodAmountMaxError = "provideDetailsOfSecondRentPeriod.rentPeriodAmount.amount.max.error"
  private lazy val rentPeriodAmountFormatError = "provideDetailsOfSecondRentPeriod.rentPeriodAmount.format.error"

//  def startDateInput(using messages: Messages) = LocalDate()

  def endDateInput(using messages: Messages): DateInput =
    dateInput(endDate, "provideDetailsOfSecondRentPeriod.endDate.label", "date.hint")


  val form: Form[ProvideDetailsOfSecondRentPeriod] =
    Form(
      mapping(
        endDate -> dateMapping(
          "provideDetailsOfSecondRentPeriod.endDate",
          CompareWithAnotherDateValidation("before.startDate.error", "startDate", (end, start) => start.isBefore(end))
        ),
        rentPeriodAmount -> text()
          .transform[String](_.strip().replaceAll("[£|,|\\s]", ""), identity)
          .verifying(
            firstError(
              isNotEmpty(rentPeriodAmount, rentPeriodAmountEmptyError),
              regexp(amountRegex.pattern(),rentPeriodAmountFormatError)
            )
          )
          .transform[BigDecimal](BigDecimal(_).setScale(2, RoundingMode.HALF_UP), _.toString)
          .verifying(
            maximumValue[BigDecimal](BigDecimal("9999999.99"), rentPeriodAmountMaxError)
          )
      )(ProvideDetailsOfSecondRentPeriod.apply)(o => Some(
        o.endDate,
        o.rentPeriodAmount
      ))
    )

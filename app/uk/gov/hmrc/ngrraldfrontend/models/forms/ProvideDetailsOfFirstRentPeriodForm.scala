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
import play.api.data.Forms.mapping
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

import java.time.LocalDate

object ProvideDetailsOfFirstRentPeriodForm extends CommonFormValidators with NGRDateInput with Mappings:

  private val startDate = "startDate"
  private val endDate = "endDate"
  private val firstRentPeriodRadio = "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod"
  private val rentPeriodAmount = "rentPeriodAmount"

  def startDateInput(using messages: Messages): DateInput =
    dateInput(startDate, "provideDetailsOfFirstRentPeriod.startDate.label")

  def endDateInput(using messages: Messages): DateInput =
    dateInput(endDate, "provideDetailsOfFirstRentPeriod.endDate.label")

  def firstRentPeriodRadio(form: Form[ProvideDetailsOfFirstRentPeriod], inputText: InputText)(using messages: Messages): NGRRadio =
    ngrRadio(
      radioName = firstRentPeriodRadio,
      radioButtons = Seq(
        yesButton(
          conditionalHtml = Some(inputText(
            form = form,
            id = rentPeriodAmount,
            name = rentPeriodAmount,
            label = messages("provideDetailsOfFirstRentPeriod.firstPeriod.radio.conditional.hint.bold"),
            isVisible = true,
            hint = Some(Hint(
              content = Text(messages("provideDetailsOfFirstRentPeriod.firstPeriod.radio.conditional.hint"))
            )),
            classes = Some("govuk-input--width-10"),
            prefix = Some(PrefixOrSuffix(content = Text(messages("common.pound"))))
          ))
        ),
        noButton(radioContent = "provideDetailsOfFirstRentPeriod.firstPeriod.radio.no")
      ),
      ngrTitle = "provideDetailsOfFirstRentPeriod.firstPeriod.radio.label",
      ngrTitleClass = "govuk-fieldset__legend--s",
      isPageHeading = false
    )

  val form: Form[ProvideDetailsOfFirstRentPeriod] =
    Form(
      mapping(
        startDate -> dateMapping("provideDetailsOfFirstRentPeriod.startDate"),
        endDate -> dateMapping(
          "provideDetailsOfFirstRentPeriod.endDate",
          CompareWithAnotherDateValidation("before.startDate.error", "startDate", (end, start) => start.isBefore(end))
        ),
        firstRentPeriodRadio -> radioBoolean("provideDetailsOfFirstRentPeriod.firstPeriod.radio.error.required"),
        rentPeriodAmount -> conditionalMoney(
          "provideDetailsOfFirstRentPeriod.firstPeriod.amount",
          _.get(firstRentPeriodRadio).contains("true")
        )
      )(ProvideDetailsOfFirstRentPeriod.apply)(o => Some(
        o.startDate,
        o.endDate,
        o.isRentPayablePeriod,
        o.rentPeriodAmount
      ))
    )

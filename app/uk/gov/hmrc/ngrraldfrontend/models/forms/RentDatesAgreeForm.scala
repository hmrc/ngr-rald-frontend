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
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.components.{NGRRadio, NGRRadioButtons, NGRRadioName}
import uk.gov.hmrc.ngrraldfrontend.models.forms.ProvideDetailsOfFirstSecondRentPeriodForm.{dateMapping, errorKeys, firstError, isDateEmpty, isDateValid}
import uk.gov.hmrc.ngrraldfrontend.models.forms.WhatTypeOfAgreementForm.text
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings

case class RentDatesAgreeForm(dateInput: NGRDate)

object RentDatesAgreeForm extends CommonFormValidators with Mappings with DateMappings{
  implicit val format: OFormat[RentDatesAgreeForm] = Json.format[RentDatesAgreeForm]

  private lazy val radioUnselectedError = "agreedRentChange.empty.error"


  private def errorKeys(whichDate: String): Map[DateErrorKeys, String] = Map(
    Required -> s"rentDatesAgree.$whichDate.required.error",
    DayAndMonth -> s"rentDatesAgree.$whichDate.day.month.required.error",
    DayAndYear -> s"rentDatesAgree.$whichDate.day.year.required.error",
    MonthAndYear -> s"rentDatesAgree.$whichDate.month.year.required.error",
    Day -> s"rentDatesAgree.$whichDate.day.required.error",
    Month -> s"rentDatesAgree.$whichDate.month.required.error",
    Year -> s"rentDatesAgree.$whichDate.year.required.error"
  )

  private lazy val dateInput = "rentDatesAgreeInput"
  private val firstDateStartInput = "provideDetailsOfFirstSecondRentPeriod.firstPeriod.start.date"

  def unapply(agreedRentChangeForm: RentDatesAgreeForm): Option[NGRDate] = Some(agreedRentChangeForm.dateInput)

  def form: Form[RentDatesAgreeForm] = {
    Form(
      mapping(
        dateInput -> dateMapping
          .verifying(
            firstError(
              isDateEmpty(errorKeys("date")),
              isDateValid("rentDatesAgree.date.invalid.error")
            )
          ),
      )(RentDatesAgreeForm.apply)(RentDatesAgreeForm.unapply)
    )
  }
  
}
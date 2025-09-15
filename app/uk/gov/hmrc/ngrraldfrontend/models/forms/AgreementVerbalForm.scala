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

import play.api.data.Form
import play.api.data.Forms.{mapping, optional}
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings

import scala.util.Try

final case class AgreementVerbalForm(radioValue: String, agreementStartDate: NGRDate, agreementEndDate: Option[NGRDate])

object AgreementVerbalForm extends CommonFormValidators with DateMappings with Mappings {
  implicit val format: OFormat[AgreementVerbalForm] = Json.format[AgreementVerbalForm]

  private val radioUnselectedError = "agreementVerbal.radio.unselected.error"
  private val agreementVerbalRadio = "agreement-verbal-radio"
  private def errorKeys(whichDate: String): Map[DateErrorKeys, String] = Map(
    Required     -> s"agreementVerbal.$whichDate.required.error",
    DayAndMonth  -> s"agreementVerbal.$whichDate.dayAndMonth.required.error",
    DayAndYear   -> s"agreementVerbal.$whichDate.dayAndYear.required.error",
    MonthAndYear -> s"agreementVerbal.$whichDate.monthAndYear.required.error",
    Day          -> s"agreementVerbal.$whichDate.day.required.error",
    Month        -> s"agreementVerbal.$whichDate.month.required.error",
    Year         -> s"agreementVerbal.$whichDate.year.required.error"
  )

  private def isEndDateEmpty[A](errorKeys: Map[DateErrorKeys, String]): Constraint[A] =
    Constraint((input: A) =>
      val agreementVerbalForm = input.asInstanceOf[AgreementVerbalForm]
      val date = agreementVerbalForm.agreementEndDate.getOrElse(NGRDate("", "", ""))
      if (agreementVerbalForm.radioValue.equals("No"))
        dateEmptyValidation(date, errorKeys)
      else
        Valid
    )

  private def isEndDateValid[A](errorKey: String): Constraint[A] =
    Constraint((input: A) =>
      val agreementVerbalForm = input.asInstanceOf[AgreementVerbalForm]
      val date = agreementVerbalForm.agreementEndDate.getOrElse(NGRDate("", "", ""))
      if (agreementVerbalForm.radioValue.equals("No"))
        dateValidation(date, errorKey)
      else
        Valid
    )

  private def isEndDateAfterStartDate[A](errorKey: String): Constraint[A] =
    Constraint((input: A) =>
      val agreementVerbalForm = input.asInstanceOf[AgreementVerbalForm]
      val startDate = agreementVerbalForm.agreementStartDate.ngrDate
      val endDate = agreementVerbalForm.agreementEndDate.getOrElse(NGRDate("", "", ""))
      if (agreementVerbalForm.radioValue.equals("No") && (Try(endDate.ngrDate).isFailure || endDate.ngrDate.isBefore(startDate)))
        Invalid(errorKey)
      else
        Valid
    )

  def unapply(agreementVerbalForm: AgreementVerbalForm): Option[(String, NGRDate, Option[NGRDate])] =
    Some(agreementVerbalForm.radioValue, agreementVerbalForm.agreementStartDate, agreementVerbalForm.agreementEndDate)

  def form: Form[AgreementVerbalForm] = {
    Form(
      mapping(
        agreementVerbalRadio -> radioText(radioUnselectedError),
        "agreementStartDate" -> dateMapping
          .verifying(
            firstError(
              isDateEmpty(errorKeys("startDate")),
              isDateValid("agreementVerbal.startDate.invalid.error")
            )
          ),
        "agreementEndDate" -> optional(
          dateMapping
        )
      )(AgreementVerbalForm.apply)(AgreementVerbalForm.unapply)
        .verifying(
          firstError(
            isEndDateEmpty(errorKeys("endDate")),
            isEndDateValid("agreementVerbal.endDate.invalid.error"),
            isEndDateAfterStartDate("agreementVerbal.endDate.isBefore.startDate.error")
          )
        )
    )
  }

}




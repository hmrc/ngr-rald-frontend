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
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.dateinput.DateInput
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.{Fieldset, Legend}
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.{ngrRadio, noButtonWithFalseValue, yesButtonWithTrueValue}
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings
import uk.gov.hmrc.ngrraldfrontend.views.html.components.DateTextFields

import scala.util.Try

final case class AgreementVerbalForm(radioValue: String, agreementStartDate: NGRDate, agreementEndDate: Option[NGRDate])

object AgreementVerbalForm extends CommonFormValidators with DateMappings with Mappings {
  implicit val format: OFormat[AgreementVerbalForm] = Json.format[AgreementVerbalForm]

  private val radioUnselectedError = "agreementVerbal.radio.unselected.error"
  private val agreementVerbalRadio = "agreement-verbal-radio"

  def agreementVerbalRadio(form: Form[AgreementVerbalForm], dateTextFields: DateTextFields)(implicit messages: Messages): NGRRadio =
    ngrRadio(
      radioName = "agreement-verbal-radio",
      radioButtons = Seq(
        yesButtonWithTrueValue(radioContent = "agreementVerbal.yes"),
        noButtonWithFalseValue(
          radioContent = "agreementVerbal.no",
          conditionalHtml = Some(dateTextFields(form, DateInput(id = "agreementEndDate",
            fieldset = Some(Fieldset(legend = Some(Legend(content = Text(messages("agreementVerbal.endDate.title")), classes = "govuk-fieldset__legend--s")))),
            hint = Some(Hint(content = Text(messages("agreementVerbal.endDate.hint")))))))
        )
      ),
      ngrTitle = "agreementVerbal.radio.title",
      hint = Some("agreementVerbal.radio.hint")
    )

  private def isEndDateEmpty[A](errorKeys: Map[DateErrorKeys, String]): Constraint[A] =
    Constraint((input: A) =>
      val agreementVerbalForm = input.asInstanceOf[AgreementVerbalForm]
      val date = agreementVerbalForm.agreementEndDate.getOrElse(NGRDate("", "", ""))
      if (agreementVerbalForm.radioValue.equals("false"))
        dateEmptyValidation(date, errorKeys)
      else
        Valid
    )

  private def isEndDateValid[A](errorKey: String): Constraint[A] =
    Constraint((input: A) =>
      val agreementVerbalForm = input.asInstanceOf[AgreementVerbalForm]
      val date = agreementVerbalForm.agreementEndDate.getOrElse(NGRDate("", "", ""))
      if (agreementVerbalForm.radioValue.equals("false"))
        dateValidation(date, errorKey)
      else
        Valid
    )

  private def isEndDateAfterStartDate[A](errorKey: String): Constraint[A] =
    Constraint((input: A) =>
      val agreementVerbalForm = input.asInstanceOf[AgreementVerbalForm]
      val startDate = agreementVerbalForm.agreementStartDate.ngrDate
      val endDate = agreementVerbalForm.agreementEndDate.getOrElse(NGRDate("", "", ""))
      if (agreementVerbalForm.radioValue.equals("false") && (Try(endDate.ngrDate).isFailure || endDate.ngrDate.isBefore(startDate)))
        Invalid(errorKey)
      else
        Valid
    )

  def unapply(agreementVerbalForm: AgreementVerbalForm): Option[(String, NGRDate, Option[NGRDate])] =
    Some(agreementVerbalForm.radioValue, agreementVerbalForm.agreementStartDate, agreementVerbalForm.agreementEndDate)

  def answerToForm(agreementVerbal: AgreementVerbal): Form[AgreementVerbalForm] =
    form.fill(
      AgreementVerbalForm(
        agreementVerbal.openEnded.toString,
        NGRDate.fromString(agreementVerbal.startDate),
        agreementVerbal.endDate.map(NGRDate.fromString(_))
      )
    )

  def formToAnswers(agreementVerbalForm: AgreementVerbalForm): AgreementVerbal =
    val openEnded: Boolean = agreementVerbalForm.radioValue.toBoolean
    AgreementVerbal(
      agreementVerbalForm.agreementStartDate.makeString,
      openEnded,
      if (openEnded) None else agreementVerbalForm.agreementEndDate.map(_.makeString))
    
  def form: Form[AgreementVerbalForm] = {
    Form(
      mapping(
        agreementVerbalRadio -> radioText(radioUnselectedError),
        "agreementStartDate" -> dateMapping
          .verifying(
            firstError(
              isDateEmpty(errorKeys("agreementVerbal", "agreementStartDate")),
              isDateValid("agreementVerbal.agreementStartDate.invalid.error")
            )
          ),
        "agreementEndDate" -> optional(
          dateMapping
        )
      )(AgreementVerbalForm.apply)(AgreementVerbalForm.unapply)
        .verifying(
          firstError(
            isEndDateEmpty(errorKeys("agreementVerbal", "agreementEndDate")),
            isEndDateValid("agreementVerbal.agreementEndDate.invalid.error"),
            isEndDateAfterStartDate("agreementVerbal.endDate.isBefore.startDate.error")
          )
        )
    )
  }

}




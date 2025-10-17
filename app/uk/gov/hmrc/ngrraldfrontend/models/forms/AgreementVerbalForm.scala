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

import play.api.data.{Form, FormError}
import play.api.data.Forms.{mapping, optional, of}
import play.api.data.format.Formatter
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.dateinput.DateInput
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.{Fieldset, Legend}
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.{ngrRadio, noButton, yesButton}
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings
import uk.gov.hmrc.ngrraldfrontend.views.html.components.DateTextFields

import scala.util.Try

final case class AgreementVerbalForm(radioValue: String, agreementStartDate: NGRDate, agreementEndDate: Option[NGRDate])

object AgreementVerbalForm extends Mappings with DateMappings{
  implicit val format: OFormat[AgreementVerbalForm] = Json.format[AgreementVerbalForm]

  private val radioUnselectedError = "agreementVerbal.radio.unselected.error"
  private val agreementVerbalRadio = "agreement-verbal-radio"

  def agreementVerbalRadio(form: Form[AgreementVerbalForm], dateTextFields: DateTextFields)(implicit messages: Messages): NGRRadio =
    ngrRadio(
      radioName = "agreement-verbal-radio",
      radioButtons = Seq(
        yesButton(radioContent = "agreementVerbal.yes"),
        noButton(
          radioContent = "agreementVerbal.no",
          conditionalHtml = Some(dateTextFields(form, DateInput(id = "agreementEndDate",
            fieldset = Some(Fieldset(legend = Some(Legend(content = Text(messages("agreementVerbal.endDate.title")), classes = "govuk-fieldset__legend--s")))),
            hint = Some(Hint(content = Text(messages("agreementVerbal.endDate.hint")))))))
        )
      ),
      ngrTitle = "agreementVerbal.radio.title",
      hint = Some("agreementVerbal.radio.hint")
    )

  private def endDateFormatter(args: Seq[String] = Seq.empty): Formatter[Option[NGRDate]] = new Formatter[Option[NGRDate]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[NGRDate]] =
      val isNotOpenEnded = data.get(agreementVerbalRadio).exists(_ == "false")
      (data.get(s"$key.day"), data.get(s"$key.month"), data.get(s"$key.year")) match {
        case (None, None, None) if isNotOpenEnded => Left(Seq(FormError(key, "agreementVerbal.agreementEndDate.required.error", args)))
        case (Some(day), Some(month), Some(year)) if isNotOpenEnded => isEndDateValid(day, month, year, key, args)
        case (Some(day), Some(month), Some(year)) => Right(Some(NGRDate(day, month, year)))
        case (None, None, None) => Right(None)
      }

    override def unbind(key: String, value: Option[NGRDate]): Map[String, String] =
      unbindNGRDate(key, value)
  }

  private def isEndDateValid(day: String, month: String, year: String, key: String, args: Seq[String]): Either[Seq[FormError], Option[NGRDate]] =
    val endDate: NGRDate = NGRDate(day, month, year)
    val errorKey: String = getDateErrorKey(endDate, errorKeys("agreementVerbal", "agreementEndDate"))
    if (errorKey.nonEmpty)
      Left(Seq(FormError(key, errorKey, args)))
    else if (isDateInvalid(endDate))
      Left(Seq(FormError(key, "agreementVerbal.agreementEndDate.invalid.error", args)))
    else if (isDateBefore1900(endDate))
      Left(Seq(FormError(key, "agreementVerbal.agreementEndDate.before.1900.error", args)))
    else
      Right(Some(endDate))

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
        "agreementEndDate" -> of(endDateFormatter())
      )(AgreementVerbalForm.apply)(AgreementVerbalForm.unapply)
    )
  }

}




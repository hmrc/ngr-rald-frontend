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
import play.api.data.Forms.{mapping, optional}
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.i18n.*
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.govukfrontend.views.Aliases.Label
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.dateinput.DateInput
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.{Fieldset, Legend}
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.components.{NGRCharacterCount, NGRRadio}
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.{ngrRadio, noButton, yesButton}
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings
import uk.gov.hmrc.ngrraldfrontend.views.html.components.{DateTextFields, NGRCharacterCountComponent}

final case class AgreementForm(
                                agreementStart: NGRDate,
                                openEndedRadio: String,
                                openEndedDate: Option[NGRDate],
                                breakClauseRadio: String,
                                breakClauseInfo: Option[String]
                              )

object AgreementForm extends CommonFormValidators with Mappings with DateMappings{
  implicit val format: OFormat[AgreementForm] = Json.format[AgreementForm]

  private lazy val radioOpenEndedUnselectedError = "agreement.radio.openEnded.required.error"
  private lazy val radioBreakClauseUnselectedError = "agreement.radio.breakClause.required.error"

  private lazy val isBreakClauseEmptyError = "agreement.radio.conditional.breakClause.required.error"
  private lazy val isBreakClauseTooLongError = "agreement.radio.conditional.breakClause.tooLong.error"

  private val agreemenrStartDate = "agreementStartDate"
  private val openEndedRadio = "agreement-radio-openEnded"
  private val endDate = "agreementEndDate"
  private val breakClauseRadio = "agreement-breakClause-radio"
  private val aboutBreakClause = "about-break-clause"

  val messagesApi: MessagesApi = new DefaultMessagesApi()
  val lang: Lang = Lang.defaultLang
  val messages: Messages = MessagesImpl(lang, messagesApi)

  private def errorKeys(whichDate: String): Map[DateErrorKeys, String] = Map(
    Required -> s"agreement.$whichDate.required.error",
    DayAndMonth -> s"agreement.$whichDate.day.month.required.error",
    DayAndYear -> s"agreement.$whichDate.day.year.required.error",
    MonthAndYear -> s"agreement.$whichDate.month.year.required.error",
    Day -> s"agreement.$whichDate.day.required.error",
    Month -> s"agreement.$whichDate.month.required.error",
    Year -> s"agreement.$whichDate.year.required.error"
  )

  def dateInput()(implicit messages: Messages): DateInput = DateInput(
    id = agreemenrStartDate,
    namePrefix = Some(agreemenrStartDate),
    fieldset = Some(Fieldset(
      legend = Some(Legend(
        content = Text(messages("agreement.subheading.1")),
        classes = "govuk-fieldset__legend--m",
        isPageHeading = true
      ))
    )),
    hint = Some(Hint(
      id = Some("agreement.start.date.hint"),
      content = Text(messages("agreement.radio.conditional.hint.1"))
    ))
  )

  def openEndedRadio(form: Form[AgreementForm], dateTextFields: DateTextFields)(implicit messages: Messages): NGRRadio =
    ngrRadio(
      radioName = openEndedRadio,
      radioButtons = Seq(
        yesButton(radioContent = "agreement.radio.1"),
        noButton(
          radioContent = "agreement.radio.2",
          conditionalHtml = Some(dateTextFields(form, DateInput(
            id = endDate,
            namePrefix = Some(""),
            fieldset = Some(Fieldset(
              legend = Some(Legend(
                content = Text(messages("agreement.radio.conditional.subheading.1")),
                classes = "govuk-fieldset__legend--s",
                isPageHeading = true
              ))
            )),
            hint = Some(Hint(
              content = Text(messages("agreement.radio.conditional.hint.1"))
            ))
          )))
        )
      ),
      ngrTitle = "agreement.subheading.2",
      hint = Some("agreement.hint.2")
    )

  def breakClauseRadio(form: Form[AgreementForm], ngrCharacterCountComponent: NGRCharacterCountComponent)(implicit messages: Messages): NGRRadio =
    ngrRadio(
      radioName = breakClauseRadio,
      radioButtons = Seq(
        yesButton(
          conditionalHtml = Some(ngrCharacterCountComponent(form,
            NGRCharacterCount(
              id = aboutBreakClause,
              name = aboutBreakClause,
              maxLength = Some(250),
              label = Label(
                classes = "govuk-label govuk-label--s",
                content = Text(Messages("agreement.radio.conditional.subheading.2"))
              ),
              hint = Some(
                Hint(
                  id = Some("agreement-breakClause-hint"),
                  classes = "",
                  attributes = Map.empty,
                  content = Text(messages("agreement.radio.conditional.hint.2"))
                )
              )
            )))
        ),
        noButton()
      ),
      ngrTitle = "agreement.subheading.3",
      hint = Some("agreement.hint.3")
    )

  def unapply(agreementForm: AgreementForm): Option[(NGRDate, String, Option[NGRDate], String, Option[String])] =
    Some(
      (agreementForm.agreementStart,
       agreementForm.openEndedRadio,
       agreementForm.openEndedDate,
       agreementForm.breakClauseRadio,
       agreementForm.breakClauseInfo)
    )

  def answerToForm(agreement: Agreement): Form[AgreementForm] =
    form.fill(
      AgreementForm(
        NGRDate.fromString(agreement.agreementStart),
        agreement.isOpenEnded.toString,
        agreement.openEndedDate match {
          case Some(value) => Some(NGRDate.fromString(value))
          case None => None
        },
        agreement.haveBreakClause.toString,
        agreement.breakClauseInfo
      )
    )

  def formToAnswers(agreementForm: AgreementForm) =
    Agreement(
      agreementForm.agreementStart.makeString,
      agreementForm.openEndedRadio.toBoolean,
      if (agreementForm.openEndedRadio.toBoolean) None else agreementForm.openEndedDate.map(_.makeString),
      agreementForm.breakClauseRadio.toBoolean,
      if (agreementForm.breakClauseRadio.toBoolean) agreementForm.breakClauseInfo else None
    )

  private def isEndDateEmpty[A](errorKeys: Map[DateErrorKeys, String]): Constraint[A] = {
    Constraint((input: A) =>
      val agreementForm = input.asInstanceOf[AgreementForm]
      val date = agreementForm.openEndedDate.getOrElse(NGRDate("", "", ""))
      if (agreementForm.openEndedRadio.equals("false"))
        dateEmptyValidation(date, errorKeys)
      else
        Valid
    )
  }

  private def isEndDateValid[A](errorKey: String): Constraint[A] =
    Constraint((input: A) =>
      val agreementForm = input.asInstanceOf[AgreementForm]
      val date = agreementForm.openEndedDate.getOrElse(NGRDate("", "", ""))
      if (agreementForm.openEndedRadio.equals("false"))
        dateValidation(date, errorKey)
      else
        Valid
    )

  private def isBreakClauseTextEmpty[A]: Constraint[A] =
    Constraint((input: A) =>
      val agreementForm = input.asInstanceOf[AgreementForm]
      if (agreementForm.breakClauseRadio.equals("true") && agreementForm.breakClauseInfo.getOrElse("").isBlank)
        Invalid(isBreakClauseEmptyError)
      else
        Valid
    )

  private def otherTextMaxLength[A]: Constraint[A] =
    Constraint((input: A) =>
      val agreementForm = input.asInstanceOf[AgreementForm]
      if (agreementForm.breakClauseRadio.equals("true") && agreementForm.breakClauseInfo.getOrElse("").length > 250)
        Invalid(isBreakClauseTooLongError)
      else
        Valid
    )

  def form: Form[AgreementForm] = {
    Form(
      mapping(
        agreemenrStartDate -> dateMapping
          .verifying(
            firstError(
              isDateEmpty(errorKeys("startDate")),
              isDateValid("agreement.startDate.format.error")
            )
          ),
        openEndedRadio -> radioText(radioOpenEndedUnselectedError),
        endDate -> optional(
          dateMapping
        ),
        breakClauseRadio -> radioText(radioBreakClauseUnselectedError),
        aboutBreakClause -> optional(
          play.api.data.Forms.text
            .transform[String](_.strip(), identity)
        )
      )(AgreementForm.apply)(AgreementForm.unapply)
        .verifying(
          firstError(
            isBreakClauseTextEmpty,
            otherTextMaxLength
          )
        )
        .verifying(
          firstError(
            isEndDateEmpty(errorKeys("endDate")),
            isEndDateValid("agreement.endDate.format.error")
          )
        )
    )
  }
}

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

import play.api.data.{Form, FormError}
import play.api.data.Forms.{mapping, optional, of}
import play.api.data.format.Formatter
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.i18n.*
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.govukfrontend.views.Aliases.Label
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.dateinput.DateInput
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.{Fieldset, Legend}
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.{ngrRadio, noButton, yesButton}
import uk.gov.hmrc.ngrraldfrontend.models.components.{NGRCharacterCount, NGRRadio}
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

  private def endDateFormatter(args: Seq[String] = Seq.empty): Formatter[Option[NGRDate]] = new Formatter[Option[NGRDate]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[NGRDate]] =
      val isNotOpenEnded = data.get(openEndedRadio).exists(_ == "false")
      (data.get(s"$key.day"), data.get(s"$key.month"), data.get(s"$key.year")) match {
        case (None, None, None) if isNotOpenEnded => Left(Seq(FormError(key, "agreement.agreementEndDate.required.error", args)))
        case (Some(day), Some(month), Some(year)) if isNotOpenEnded => isEndDateValid(day, month, year, key, args)
        case (Some(day), Some(month), Some(year)) => Right(Some(NGRDate(day, month, year)))
        case (None, None, None) => Right(None)
      }

    override def unbind(key: String, value: Option[NGRDate]): Map[String, String] =
      unbindNGRDate(key, value)
  }

  private def isEndDateValid(day: String, month: String, year: String, key: String, args: Seq[String]): Either[Seq[FormError], Option[NGRDate]] =
    val endDate: NGRDate = NGRDate(day, month, year)
    val errorKey: String = getDateErrorKey(endDate, errorKeys("agreement", "agreementEndDate"))
    if (errorKey.nonEmpty)
      Left(Seq(FormError(key, errorKey, args)))
    else if (isDateInvalid(endDate))
      Left(Seq(FormError(key, "agreement.agreementEndDate.invalid.error", args)))
    else if (isDateBefore1900(endDate))
      Left(Seq(FormError(key, "agreement.agreementEndDate.before.1900.error", args)))
    else
      Right(Some(endDate))

  private def aboutBreakClauseFormatter(args: Seq[String] = Seq.empty): Formatter[Option[String]] = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] =
      val hasBreakClause = data.get(breakClauseRadio).exists(_ == "true")
      data.get(key) match {
        case None if hasBreakClause => Left(Seq(FormError(key, isBreakClauseEmptyError, args)))
        case Some(s) if hasBreakClause => isBreakClauseInfoValid(s.trim, key, args)
        case Some(s) => Right(Some(s))
        case None => Right(None)
      }

    override def unbind(key: String, value: Option[String]): Map[String, String] =
      Map(key -> value.getOrElse(""))
  }

  private def isBreakClauseInfoValid(breakClauseInfo: String, key: String, args: Seq[String]): Either[Seq[FormError], Option[String]] =
    if (breakClauseInfo.isEmpty)
      Left(Seq(FormError(key, isBreakClauseEmptyError, args)))
    else if (breakClauseInfo.length > 250)
      Left(Seq(FormError(key, isBreakClauseTooLongError, args)))
    else
      Right(Some(breakClauseInfo))

  def form: Form[AgreementForm] = {
    Form(
      mapping(
        agreemenrStartDate -> dateMapping
          .verifying(
            firstError(
              isDateEmpty(errorKeys("agreement", "agreementStartDate")),
              isDateValid("agreement.agreementStartDate.invalid.error"),
              isDateAfter1900("agreement.agreementStartDate.before.1900.error")
            )
          ),
        openEndedRadio -> radioText(radioOpenEndedUnselectedError),
        endDate -> of(endDateFormatter()),
        breakClauseRadio -> radioText(radioBreakClauseUnselectedError),
        aboutBreakClause -> of(aboutBreakClauseFormatter())
      )(AgreementForm.apply)(AgreementForm.unapply)
    )
  }
}

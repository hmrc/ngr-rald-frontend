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
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.forms.AgreementVerbalForm.{dateValidation, firstError, isDateEmpty, isDateValid}
import uk.gov.hmrc.ngrraldfrontend.models.forms.WhatIsYourRentBasedOnForm.firstError
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings
import scala.util.Try

final case class AgreementForm(
                                agreementStart: NGRDate,
                                openEndedRadio: String,
                                openEndedDate: Option[NGRDate],
                                breakClauseRadio: String,
                                breakClauseInfo: Option[String]
                              )

object AgreementForm extends CommonFormValidators with Mappings with DateMappings{
  implicit val format: OFormat[AgreementForm] = Json.format[AgreementForm]

  private def errorKeys(whichDate: String): Map[DateErrorKeys, String] = Map(
    Required -> s"agreement.$whichDate.required.error",
    DayAndMonth -> s"agreement.$whichDate.day.month.required.error",
    DayAndYear -> s"agreement.$whichDate.day.year.required.error",
    MonthAndYear -> s"agreement.$whichDate.month.year.required.error",
    Day -> s"agreement.$whichDate.day.required.error",
    Month -> s"agreement.$whichDate.month.required.error",
    Year -> s"agreement.$whichDate.year.required.error"
  )

  private def isEndDateEmpty[A](errorKeys: Map[DateErrorKeys, String]): Constraint[A] = {
    Constraint((input: A) =>
      val agreementForm = input.asInstanceOf[AgreementForm]
      val date = agreementForm.openEndedDate.getOrElse(NGRDate("", "", ""))
      if (agreementForm.openEndedRadio.equals("NoOpenEnded"))
        dateEmptyValidation(date, errorKeys)
      else
        Valid
    )
  }

  private def isEndDateValid[A](errorKey: String): Constraint[A] =
      Constraint((input: A) =>
        val agreementForm = input.asInstanceOf[AgreementForm]
        val date = agreementForm.openEndedDate.getOrElse(NGRDate("", "", ""))
        if (agreementForm.openEndedRadio.equals("NoOpenEnded"))
          dateValidation(date, errorKey)
        else
          Valid
      )

  private val radioUnselectedError = "agreementVerbal.radio.unselected.error"
  private val agreementVerbalRadio = "agreement-verbal-radio"

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

  def unapply(agreementForm: AgreementForm): Option[(NGRDate, String, Option[NGRDate], String, Option[String])] =
    Some(
      (agreementForm.agreementStart,
       agreementForm.openEndedRadio,
       agreementForm.openEndedDate,
       agreementForm.breakClauseRadio,
       agreementForm.breakClauseInfo)
    )

  private def isBreakClauseTextEmpty[A]: Constraint[A] =
    Constraint((input: A) =>
      val agreementForm = input.asInstanceOf[AgreementForm]
      if (agreementForm.breakClauseRadio.equals("YesBreakClause") && agreementForm.breakClauseInfo.getOrElse("").isBlank)
        Invalid(isBreakClauseEmptyError)
      else
        Valid
    )

  private def otherTextMaxLength[A]: Constraint[A] =
    Constraint((input: A) =>
      val agreementForm = input.asInstanceOf[AgreementForm]
      if (agreementForm.breakClauseRadio.equals("YesBreakClause") && agreementForm.breakClauseInfo.getOrElse("").length > 250)
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
        openEndedRadio -> text(radioOpenEndedUnselectedError),
        endDate -> optional(
          dateMapping
        ),
        breakClauseRadio -> text(radioBreakClauseUnselectedError),
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

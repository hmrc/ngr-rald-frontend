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
import uk.gov.hmrc.ngrraldfrontend.models.{NGRDate, DateMappings}
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


  private lazy val radioUnselectedError = "landlord.radio.empty.error"
  private lazy val radioOpenEndedUnselectedError = "agreement.radio.openEnded.empty.error"
  private lazy val radioBreakClauseUnselectedError = "agreement.radio.breakClause.empty.error"

  private lazy val isBreakClauseEmptyError = "agreement.radio.conditional.breakClause.empty.error"
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


  private def isDateDefined(agreementForm: AgreementForm): Boolean = {
    agreementForm.openEndedRadio.equals("NoOpenEnded") && agreementForm.openEndedDate.nonEmpty
  }

  private def isDateDigits(ratepayerDate: NGRDate): Boolean = {
      Try(ratepayerDate.day.toInt).isSuccess &&
      Try(ratepayerDate.month.toInt).isSuccess &&
      Try(ratepayerDate.year.toInt).isSuccess
  }


  private def isValidDate(ratepayerDate: NGRDate): Boolean = {
    try {
      val day = ratepayerDate.day.toInt
      val month = ratepayerDate.month.toInt
      val year = ratepayerDate.year.toInt

      val maxDay = month match {
        case 1 | 3 | 5 | 7 | 8 | 10 | 12 => 31
        case 4 | 6 | 9 | 11 => 30
        case 2 =>
          if (java.time.Year.isLeap(year)) 29 else 28
        case _ => return false // Invalid month
      }

      day >= 1 && day <= maxDay
    } catch {
      case _: Exception => false
    }
  }


  private def isDateEmpty(agreementForm: AgreementForm): Boolean = {
    agreementForm.openEndedRadio.equals("NoOpenEnded") && agreementForm.openEndedDate.isEmpty
  }

  private def isDateNonEmpty[A]: Constraint[A] =
    Constraint((input: A) =>
      val agreementForm = input.asInstanceOf[AgreementForm]
      if (isDateEmpty(agreementForm))
        Invalid("agreement.endDate.empty.error")
      else
        Valid
    )

  private def isFieldsNonEmpty[A]: Constraint[A] = {
    Constraint((input: A) =>
      val currentRatepayer = input.asInstanceOf[AgreementForm]
      if (isDateDefined(currentRatepayer))
        val ratepayerDate = currentRatepayer.openEndedDate.get
        (ratepayerDate.day.isEmpty, ratepayerDate.month.isEmpty, ratepayerDate.year.isEmpty) match
          case (true, true, false) => Invalid("agreement.endDate.day.month.empty.error")
          case (true, false, true) => Invalid("agreement.endDate.day.year.empty.error")
          case (false, true, true) => Invalid("agreement.endDate.month.year.empty.error")
          case (true, false, false) => Invalid("agreement.endDate.day.empty.error")
          case (false, true, false) => Invalid("agreement.endDate.month.empty.error")
          case (false, false, true) => Invalid("agreement.endDate.year.empty.error")
          case (false, false, false) if(!isDateDigits(ratepayerDate)) => Invalid("agreement.endDate.format.error")
          case (false, false, false) if(!isValidDate(ratepayerDate)) => Invalid("agreement.startDate.format.error")
          case (_, _, _) => Valid
      else
        Valid
    )
  }


  def isNonEmptyDate: Constraint[NGRDate] = Constraint("isNonEmptyDate") { date =>
    val dayEmpty = date.day.trim.isEmpty
    val monthEmpty = date.month.trim.isEmpty
    val yearEmpty = date.year.trim.isEmpty
    (dayEmpty, monthEmpty, yearEmpty) match {
      case (true, true, true) => Invalid("agreement.startDate.empty.error")
      case (true, true, false) => Invalid("agreement.startDate.day.month.empty.error")
      case (true, false, true) => Invalid("agreement.startDate.day.year.empty.error")
      case (false, true, true) => Invalid("agreement.startDate.month.year.empty.error")
      case (true, false, false) => Invalid("agreement.startDate.day.empty.error")
      case (false, true, false) => Invalid("agreement.startDate.month.empty.error")
      case (false, false, true) => Invalid("agreement.startDate.year.empty.error")
      case (false, false, false) if(!isDateDigits(date)) => Invalid("agreement.startDate.format.error")
      case (false, false, false) if(!isValidDate(date)) => Invalid("agreement.startDate.format.error")
      case _ => Valid
    }
  }


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
        agreemenrStartDate -> dateMapping.verifying(isNonEmptyDate),
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
            isDateNonEmpty,
            isFieldsNonEmpty
          )
        )

    )
  }
}

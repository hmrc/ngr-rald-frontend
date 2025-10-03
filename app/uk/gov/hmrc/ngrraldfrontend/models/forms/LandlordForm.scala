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
import play.api.data.Forms.{mapping, optional, text}
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.i18n.*
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.govukfrontend.views.Aliases.Label
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.ngrraldfrontend.models.Landlord
import uk.gov.hmrc.ngrraldfrontend.models.components.{NGRCharacterCount, NGRRadio}
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.{ngrRadio, noButtonWithFalseValue, yesButtonWithTrueValue}
import uk.gov.hmrc.ngrraldfrontend.models.forms.WhatIsYourRentBasedOnForm.firstError
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings
import uk.gov.hmrc.ngrraldfrontend.views.html.components.NGRCharacterCountComponent

final case class LandlordForm(landlordName: String, hasRelationship: String, landlordRelationship: Option[String])

object LandlordForm extends CommonFormValidators with Mappings{
  implicit val format: OFormat[LandlordForm] = Json.format[LandlordForm]

  private lazy val landlordNameEmptyError = "landlord.name.empty.error"
  private lazy val landlordNameTooLongError = "landlord.name.empty.tooLong.error"
  private lazy val radioUnselectedError = "landlord.radio.empty.error"
  private lazy val landlordRelationshipEmptyError = "landlord.radio.emptyText.error"
  private lazy val landlordRelationshipTooLongError = "landlord.radio.tooLong.error"

  private val landlord = "landlord-name-value"
  private val landlordRadio = "landlord-radio"
  private val landlordRelationshipYes = "landlord-relationship"
  
  val messagesApi: MessagesApi = new DefaultMessagesApi()
  val lang: Lang = Lang.defaultLang
  val messages: Messages = MessagesImpl(lang, messagesApi)
  
  def landlordRadio(form: Form[LandlordForm], ngrCharacterCountComponent: NGRCharacterCountComponent)(implicit messages: Messages): NGRRadio =
    ngrRadio(
      radioName = landlordRadio,
      radioButtons = Seq(
        yesButtonWithTrueValue(
          conditionalHtml = Some(ngrCharacterCountComponent(form,
            NGRCharacterCount(
              id = landlordRelationshipYes,
              name = landlordRelationshipYes,
              maxLength = Some(250),
              label = Label(
                classes = "govuk-label govuk-label--s",
                content = Text(Messages("landlord.radio.yes"))
              ),
              hint = Some(
                Hint(
                  id = Some("landlord-relationship-hint"),
                  classes = "",
                  attributes = Map.empty,
                  content = Text(messages("landlord.radio.yes.hint"))
                )
              ))))
        ),
        noButtonWithFalseValue()
      ),
      ngrTitle = "landlord.p2"
    )
    
  def unapply(landlordForm: LandlordForm): Option[(String, String, Option[String])] =
    Some((landlordForm.landlordName, landlordForm.hasRelationship, landlordForm.landlordRelationship))

  def answerToForm(landlord: Landlord): Form[LandlordForm] =
    form.fill(
      LandlordForm(
        landlord.landlordName,
        landlord.hasRelationship.toString,
        landlord.landlordRelationship
      )
    )

  def formToAnswers(landlordForm: LandlordForm): Landlord =
    Landlord(
      landlordForm.landlordName,
      landlordForm.hasRelationship.toBoolean,
      if (landlordForm.hasRelationship.toBoolean) landlordForm.landlordRelationship else None
    )
  
  private def isLandlordRelationshipTextEmpty[A]: Constraint[A] =
    Constraint((input: A) =>
      val landlordForm = input.asInstanceOf[LandlordForm]
      if (landlordForm.hasRelationship.equals("true") && landlordForm.landlordRelationship.getOrElse("").isBlank)
        Invalid(landlordRelationshipEmptyError)
      else
        Valid
    )

  private def isLandlordRelationshipTextMaxLength[A]: Constraint[A] =
    Constraint((input: A) =>
      val landlordForm = input.asInstanceOf[LandlordForm]
      if (landlordForm.hasRelationship.equals("true") && landlordForm.landlordRelationship.getOrElse("").length > 250)
        Invalid(landlordRelationshipTooLongError)
      else
        Valid
    )

  def form: Form[LandlordForm] = {
    Form(
      mapping(
        landlord -> text()
          .verifying(
            firstError(
              isNotEmpty(landlord, landlordNameEmptyError),
              maxLength(50, landlordNameTooLongError)
            )
            
          ),
        landlordRadio -> radioText(radioUnselectedError),
        landlordRelationshipYes -> optional(
          play.api.data.Forms.text
          .transform[String](_.strip(), identity)
        )
      )(LandlordForm.apply)(LandlordForm.unapply)
        .verifying(
          firstError(
            isLandlordRelationshipTextEmpty,
            isLandlordRelationshipTextMaxLength
          )
        )
    )
  }
}

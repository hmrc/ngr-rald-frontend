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
import play.api.i18n.*
import play.api.libs.json.{Json, OFormat}

final case class LandlordForm(landlordName: String, landLordType: String, landlordOther: Option[String])

object LandlordForm extends CommonFormValidators  {
  implicit val format: OFormat[LandlordForm] = Json.format[LandlordForm]

  private lazy val landlordNameEmptyError = "landlord.name.empty.error"
  private lazy val radioUnselectedError = "whatTypeOfAgreement.error.required"
  private lazy val otherRadioEmptyError = "landlord.radio.other.empty.error"

  private val landlord = "landlord-name-value"
  private val landlordRadio = "landlord-radio"
  private val landlordOther = "landlord-radio-other"


  val messagesApi: MessagesApi = new DefaultMessagesApi()
  val lang: Lang = Lang.defaultLang
  val messages: Messages = MessagesImpl(lang, messagesApi)


  def unapply(landlordForm: LandlordForm): Option[(String, String, Option[String])] =
    Some((landlordForm.landlordName, landlordForm.landLordType, landlordForm.landlordOther))

  def form: Form[LandlordForm] = {
    Form(
      mapping(
        landlord -> text()
          .verifying(
              isNotEmpty(landlord, landlordNameEmptyError)
          ),
        landlordRadio -> text().verifying(
          isNotEmpty(landlord, landlordNameEmptyError)
        ),
        landlordOther -> optional(text)
      )(LandlordForm.apply)(LandlordForm.unapply)
        .verifying(otherRadioEmptyError, landlordForm =>
          landlordForm.landLordType != "OtherRelationship" || landlordForm.landlordOther.exists(_.trim.nonEmpty)
          ))
  }
}

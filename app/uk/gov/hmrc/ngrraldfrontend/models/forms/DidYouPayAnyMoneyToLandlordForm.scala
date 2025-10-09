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
import play.api.i18n.*
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings

final case class DidYouPayAnyMoneyToLandlordForm(radioValue: String)

object DidYouPayAnyMoneyToLandlordForm extends CommonFormValidators with Mappings {
  implicit val format: OFormat[DidYouPayAnyMoneyToLandlordForm] = Json.format[DidYouPayAnyMoneyToLandlordForm]

  private lazy val radioUnselectedError = "didYouPayAnyMoneyToLandlord.empty.error"
  val radio = "didYouPayAnyMoneyToLandlord-radio-value"

  val messagesApi: MessagesApi = new DefaultMessagesApi()
  val lang: Lang = Lang.defaultLang
  val messages: Messages = MessagesImpl(lang, messagesApi)

  def unapply(didYouPayAnyMoneyToLandlordForm: DidYouPayAnyMoneyToLandlordForm): Option[(String)] =
    Some(didYouPayAnyMoneyToLandlordForm.radioValue)

  def form: Form[DidYouPayAnyMoneyToLandlordForm] = {
    Form(
      mapping(
        radio -> radioText(radioUnselectedError),
      )(DidYouPayAnyMoneyToLandlordForm.apply)(DidYouPayAnyMoneyToLandlordForm.unapply)
    )
  }
}

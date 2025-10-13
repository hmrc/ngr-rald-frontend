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
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.{ngrRadio, noButton, yesButton}

final case class RepairsAndFittingOutForm(radioValue: String)

object RepairsAndFittingOutForm extends CommonFormValidators with Mappings {
  implicit val format: OFormat[RepairsAndFittingOutForm] = Json.format[RepairsAndFittingOutForm]

  private lazy val radioUnselectedError = "repairsAndFittingOut.empty.error"
  val radio = "repairsAndFittingOut-radio-value"

  val messagesApi: MessagesApi = new DefaultMessagesApi()
  val lang: Lang = Lang.defaultLang
  val messages: Messages = MessagesImpl(lang, messagesApi)

  def unapply(repairsAndFittingOutForm: RepairsAndFittingOutForm): Option[(String)] =
    Some(repairsAndFittingOutForm.radioValue)

  def repairsAndFittingOutRadio(implicit messages: Messages): NGRRadio =
    ngrRadio(
      radioName = radio,
      radioButtons = Seq(
        yesButton(),
        noButton()
      ),
      ngrTitle = "repairsAndFittingOut.header"
    )

  def form: Form[RepairsAndFittingOutForm] = {
    Form(
      mapping(
        radio -> radioText(radioUnselectedError),
      )(RepairsAndFittingOutForm.apply)(RepairsAndFittingOutForm.unapply)
    )
  }


}

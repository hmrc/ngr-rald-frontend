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
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.Legend
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.{noButton, yesButton}
import uk.gov.hmrc.ngrraldfrontend.models.components.{NGRRadio, NGRRadioButtons, NGRRadioName}
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings

final case class DidYouGetMoneyFromLandlordForm(radio: String)

object DidYouGetMoneyFromLandlordForm extends CommonFormValidators with Mappings{
  implicit val format: OFormat[DidYouGetMoneyFromLandlordForm] = Json.format[DidYouGetMoneyFromLandlordForm]

  private lazy val radioUnselectedError = "didYouGetMoneyFromLandlord.empty.error"
  private val radio = "didYouGetMoneyFromLandlord-radio-value"

  val messagesApi: MessagesApi = new DefaultMessagesApi()
  val lang: Lang = Lang.defaultLang
  val messages: Messages = MessagesImpl(lang, messagesApi)

  def unapply(didYouGetMoneyFromLandlordForm: DidYouGetMoneyFromLandlordForm): Option[(String)] =
    Some(DidYouGetMoneyFromLandlordForm.radio)

  def ngrRadio(form: Form[DidYouGetMoneyFromLandlordForm])(implicit messages: Messages): NGRRadio =
    NGRRadio(NGRRadioName(radio), ngrTitle = Some(Legend(content = Text(messages("didYouGetMoneyFromLandlord.title")), classes = "govuk-fieldset__legend--l", isPageHeading = true)), NGRRadioButtons = Seq(yesButton, noButton))

  def form: Form[DidYouGetMoneyFromLandlordForm] = {
    Form(
      mapping(
        radio -> radioText(radioUnselectedError),
      )(DidYouGetMoneyFromLandlordForm.apply)(DidYouGetMoneyFromLandlordForm.unapply)
    )
  }
}

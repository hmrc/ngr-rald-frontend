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
import play.api.i18n.{DefaultMessagesApi, Lang, Messages, MessagesApi, MessagesImpl}
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.Legend
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.{noButton, yesButton}
import uk.gov.hmrc.ngrraldfrontend.models.components.{NGRRadio, NGRRadioButtons, NGRRadioName, No, Yes}
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings

final case class DoesYourRentIncludeParkingForm(radio: String)

object DoesYourRentIncludeParkingForm extends CommonFormValidators with Mappings{
  implicit val format: OFormat[DoesYourRentIncludeParkingForm] = Json.format[DoesYourRentIncludeParkingForm]
  
  private lazy val radioUnselectedError = "doesYourRentIncludeParking.empty.error"
  private val radio = "doesYourRentIncludeParking-radio-value"
  
  val messagesApi: MessagesApi = new DefaultMessagesApi()
  val lang: Lang = Lang.defaultLang
  val messages: Messages = MessagesImpl(lang, messagesApi)
  
  def unapply(doesYourRentIncludeParkingForm: DoesYourRentIncludeParkingForm): Option[(String)] =
    Some(doesYourRentIncludeParkingForm.radio)

  def ngrRadio(form: Form[DoesYourRentIncludeParkingForm])(implicit messages: Messages): NGRRadio =
    NGRRadio(NGRRadioName(radio),ngrTitle = Some(Legend(content = Text(messages("doesYourRentIncludeParking.title")), classes = "govuk-fieldset__legend--l", isPageHeading = true)) ,NGRRadioButtons = Seq(yesButton, noButton))

  def form: Form[DoesYourRentIncludeParkingForm] = {
    Form(
      mapping(
        radio -> text(radioUnselectedError),
      )(DoesYourRentIncludeParkingForm.apply)(DoesYourRentIncludeParkingForm.unapply)
    )
  }
}

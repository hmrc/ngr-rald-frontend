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
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.ngrraldfrontend.models.components.{NGRRadio, NGRRadioButtons, NGRRadioName}
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings

case class AgreedRentChangeForm(radioValue: String)

object AgreedRentChangeForm extends Mappings {
  implicit val format: OFormat[AgreedRentChangeForm] = Json.format[AgreedRentChangeForm]

  private lazy val radioUnselectedError = "agreedRentChange.empty.error"
  val agreedRentChangeRadio    = "agreed-rent-change-radio"

  def unapply(agreedRentChangeForm: AgreedRentChangeForm): Option[String] = Some(agreedRentChangeForm.radioValue)

  def form: Form[AgreedRentChangeForm] = {
    Form(
      mapping(
        agreedRentChangeRadio -> radioText(radioUnselectedError)
      )(AgreedRentChangeForm.apply)(AgreedRentChangeForm.unapply)
    )
  }
  
  def ngrRadio(form: Form[AgreedRentChangeForm])(implicit messages: Messages): NGRRadio =
    NGRRadio(NGRRadioName("agreed-rent-change-radio"), NGRRadioButtons = Seq(NGRRadio.yesButton, NGRRadio.noButton))

}

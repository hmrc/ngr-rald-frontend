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
import uk.gov.hmrc.govukfrontend.views.Aliases.{Legend, Text}
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings

final case class DidYouAgreeRentWithLandlordForm (radioValue: String)

object DidYouAgreeRentWithLandlordForm extends Mappings {
  implicit val format: OFormat[DidYouAgreeRentWithLandlordForm] = Json.format[DidYouAgreeRentWithLandlordForm]

  private lazy val radioUnselectedError = "didYouAgreeRentWithLandlord.error.required"
  private val didYouAgreeRentWithLandlordRadio    = "did-you-agree-rent-with-landlord-radio"

  def unapply(didYouAgreeRentWithLandlordForm: DidYouAgreeRentWithLandlordForm): Option[String] = Some(didYouAgreeRentWithLandlordForm.radioValue)

  def form: Form[DidYouAgreeRentWithLandlordForm] = {
    Form(
      mapping(
        didYouAgreeRentWithLandlordRadio -> text(radioUnselectedError)
      )(DidYouAgreeRentWithLandlordForm.apply)(DidYouAgreeRentWithLandlordForm.unapply)
    )
  }

  private val yes: NGRRadioButtons = NGRRadioButtons(radioContent = "didYouAgreeRentWithLandlord.yes", radioValue = YesTheLandlord)
  private val no: NGRRadioButtons = NGRRadioButtons(radioContent  = "didYouAgreeRentWithLandlord.no", radioValue = NoACourtSet)

  def ngrRadio(form: Form[DidYouAgreeRentWithLandlordForm])(implicit messages: Messages): NGRRadio =
    NGRRadio(
      NGRRadioName("did-you-agree-rent-with-landlord-radio"),
      NGRRadioButtons = Seq(yes,no),
      Some(
        Legend(
          content = Text(messages("didYouAgreeRentWithLandlord.title")),
          classes = "govuk-fieldset__legend--l", isPageHeading = true
        )
      )
    )
}


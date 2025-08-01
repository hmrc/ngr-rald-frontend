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
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings

final case class WhatTypeOfAgreementForm(radioValue: String)

object WhatTypeOfAgreementForm extends Mappings {
  implicit val format: OFormat[WhatTypeOfAgreementForm] = Json.format[WhatTypeOfAgreementForm]

  private lazy val radioUnselectedError = "whatTypeOfAgreement.error.required"
  private val whatTypeOfAgreementRadio    = "what-type-of-agreement-radio"

  def unapply(whatTypeOfAgreementForm: WhatTypeOfAgreementForm): Option[String] = Some(whatTypeOfAgreementForm.radioValue)

  def form: Form[WhatTypeOfAgreementForm] = {
    Form(
      mapping(
        whatTypeOfAgreementRadio -> text(radioUnselectedError)
      )(WhatTypeOfAgreementForm.apply)(WhatTypeOfAgreementForm.unapply)
    )
  }

  private val leaseOrTenancy: NGRRadioButtons = NGRRadioButtons(radioContent = "whatTypeOfAgreement.LeaseOrTenancy", radioValue = LeaseOrTenancy)
  private val written: NGRRadioButtons = NGRRadioButtons(radioContent = "whatTypeOfAgreement.written", radioValue = Written)
  private val verbal: NGRRadioButtons = NGRRadioButtons(radioContent = "whatTypeOfAgreement.verbal", radioValue = Verbal)

  def ngrRadio(form: Form[WhatTypeOfAgreementForm])(implicit messages: Messages): NGRRadio =
    NGRRadio(NGRRadioName("what-type-of-agreement-radio"),NGRRadioButtons = Seq(leaseOrTenancy, written, verbal))

}

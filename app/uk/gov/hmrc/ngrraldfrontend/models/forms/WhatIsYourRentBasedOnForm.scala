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
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings

final case class WhatIsYourRentBasedOnForm(radioValue: String, rentBasedOnOther: Option[String])

object WhatIsYourRentBasedOnForm extends CommonFormValidators with Mappings {
  implicit val format: OFormat[WhatIsYourRentBasedOnForm] = Json.format[WhatIsYourRentBasedOnForm]

  private lazy val radioUnselectedError = "whatIsYourRentBasedOn.error.required"

  private val rentBasedOnRadio = "rent-based-on-radio"
  private val rentBasedOnOther = "rent-based-on-other-desc"


  val messagesApi: MessagesApi = new DefaultMessagesApi()
  val lang: Lang = Lang.defaultLang
  val messages: Messages = MessagesImpl(lang, messagesApi)


  def unapply(whatIsYourRentBasedOnForm: WhatIsYourRentBasedOnForm): Option[(String, Option[String])] =
    Some((whatIsYourRentBasedOnForm.radioValue, whatIsYourRentBasedOnForm.rentBasedOnOther))

  private def isOtherTextEmpty[A]: Constraint[A] =
    Constraint((input: A) =>
      val rentBasedOnForm = input.asInstanceOf[WhatIsYourRentBasedOnForm]
      if (rentBasedOnForm.radioValue.equals("Other") && rentBasedOnForm.rentBasedOnOther.getOrElse("").isBlank)
        Invalid("whatIsYourRentBasedOn.otherText.error.required")
      else
        Valid
    )

  private def otherTextMaxLength[A]: Constraint[A] =
    Constraint((input: A) =>
      val rentBasedOnForm = input.asInstanceOf[WhatIsYourRentBasedOnForm]
      if (rentBasedOnForm.radioValue.equals("Other") && rentBasedOnForm.rentBasedOnOther.getOrElse("").length > 250)
        Invalid("whatIsYourRentBasedOn.otherText.error.maxLength")
      else
        Valid
    )

  def form: Form[WhatIsYourRentBasedOnForm] = {
    Form(
      mapping(
        rentBasedOnRadio -> radioText(radioUnselectedError),
        rentBasedOnOther -> optional(
          play.api.data.Forms.text
            .transform[String](_.strip(), identity)
        )
      )(WhatIsYourRentBasedOnForm.apply)(WhatIsYourRentBasedOnForm.unapply)
        .verifying(
          firstError(
            isOtherTextEmpty,
            otherTextMaxLength
          )
        )
    )
  }
}

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

import play.api.data.{Form, FormError}
import play.api.data.Forms.{mapping, of, optional}
import play.api.data.format.Formatter
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

  private def otherDescriptionFormatter(args: Seq[String] = Seq.empty): Formatter[Option[String]] = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] =
      val isOtherSelected = data.get(rentBasedOnRadio).exists(_ == "Other")
      data.get(key) match {
        case None if isOtherSelected => Left(Seq(FormError(key, "whatIsYourRentBasedOn.otherText.error.required", args)))
        case Some(s) if isOtherSelected => isOtherDescriptionValid(s.trim, key, args)
        case Some(s) => Right(Some(s.trim))
        case None => Right(None)
      }

    override def unbind(key: String, value: Option[String]): Map[String, String] =
      Map(key -> value.getOrElse(""))
  }

  private def isOtherDescriptionValid(otherStr: String, key: String, args: Seq[String]): Either[Seq[FormError], Option[String]] =
    if (otherStr.isEmpty)
      Left(Seq(FormError(key, "whatIsYourRentBasedOn.otherText.error.required", args)))
    else if (otherStr.length > 250)
      Left(Seq(FormError(key, "whatIsYourRentBasedOn.otherText.error.maxLength", args)))
    else
      Right(Some(otherStr))


  def form: Form[WhatIsYourRentBasedOnForm] = {
    Form(
      mapping(
        rentBasedOnRadio -> radioText(radioUnselectedError),
        rentBasedOnOther -> of(otherDescriptionFormatter())
      )(WhatIsYourRentBasedOnForm.apply)(WhatIsYourRentBasedOnForm.unapply)
    )
  }
}

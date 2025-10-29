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

import play.api.data.Forms.*
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}
import play.api.i18n.*
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings

final case class HasAnythingElseAffectedTheRentForm(radioValue: String, reason: Option[String])

object HasAnythingElseAffectedTheRentForm extends Mappings {
  implicit val format: OFormat[HasAnythingElseAffectedTheRentForm] = Json.format[HasAnythingElseAffectedTheRentForm]
  val hasAnythingElseAffectedTheRentRadio = "hasAnythingElseAffectedTheRent"
  val reasonInput = "reason"

  val messagesApi: MessagesApi = new DefaultMessagesApi()
  val lang: Lang = Lang.defaultLang
  val messages: Messages = MessagesImpl(lang, messagesApi)

  def unapply(hasAnythingElseAffectedTheRentForm: HasAnythingElseAffectedTheRentForm): Option[(String, Option[String])] =
    Some((hasAnythingElseAffectedTheRentForm.radioValue, hasAnythingElseAffectedTheRentForm.reason))

  private def reasonFormatter(args: Seq[String] = Seq.empty): Formatter[Option[String]] = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] =
      val isYesSelected: Boolean = data.get(hasAnythingElseAffectedTheRentRadio).exists(_ == "true")
      data.get(key) match {
        case None if isYesSelected => Left(Seq(FormError(key, "hasAnythingElseAffectedTheRent.reason.error.required", args)))
        case Some(s) if isYesSelected => isReasonValid(s.trim, key, args)
        case Some(s) => Right(Some(s.trim))
        case None => Right(None)
      }

    override def unbind(key: String, value: Option[String]): Map[String, String] =
      Map(key -> value.getOrElse(""))
  }

  private def isReasonValid(reasonStr: String, key: String, args: Seq[String]): Either[Seq[FormError], Option[String]] =
    if (reasonStr.isEmpty)
      Left(Seq(FormError(key, "hasAnythingElseAffectedTheRent.reason.error.required", args)))
    else if (reasonStr.length > 250)
      Left(Seq(FormError(key, "hasAnythingElseAffectedTheRent.reason.error.maxLength", args)))
    else
      Right(Some(reasonStr))

  def form: Form[HasAnythingElseAffectedTheRentForm] = {
    Form(
      mapping(
        hasAnythingElseAffectedTheRentRadio -> radioText("hasAnythingElseAffectedTheRent.required.error"),
        reasonInput -> of(reasonFormatter())
      )(HasAnythingElseAffectedTheRentForm.apply)(HasAnythingElseAffectedTheRentForm.unapply)
    )
  }
}

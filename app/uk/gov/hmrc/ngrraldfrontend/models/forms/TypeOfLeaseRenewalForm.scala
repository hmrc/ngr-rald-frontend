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

import play.api.data.Forms.single
import play.api.data.format.Formatter
import play.api.data.{Form, FormError, Forms}
import uk.gov.hmrc.ngrraldfrontend.models.{NGRRadio, NGRRadioButtons, NGRRadioName, RadioEntry}

sealed trait TypeOfLeaseRenewalForm extends RadioEntry

object TypeOfLeaseRenewalForm {
  val formName = "type-of-renewal"

  case object RenewedAgreement extends TypeOfLeaseRenewalForm

  case object SurrenderAndRenewal extends TypeOfLeaseRenewalForm

  val values: Set[TypeOfLeaseRenewalForm] = Set(RenewedAgreement, SurrenderAndRenewal)

  implicit val formatter: Formatter[TypeOfLeaseRenewalForm] = new Formatter[TypeOfLeaseRenewalForm] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], TypeOfLeaseRenewalForm] = {
      data.get(key).collectFirst {
        case "RenewedAgreement" => RenewedAgreement
        case "SurrenderAndRenewal" => SurrenderAndRenewal
      }.toRight(Seq(FormError(key, "typeOfLeaseRenewal.required.error")))
    }

    override def unbind(key: String, value: TypeOfLeaseRenewalForm): Map[String, String] = Map(
      key -> (value match {
        case RenewedAgreement => "RenewedAgreement"
        case SurrenderAndRenewal => "SurrenderAndRenewal"
      })
    )
  }

  private val renewedAgreementButton: NGRRadioButtons = NGRRadioButtons("typeOfLeaseRenewal.option1", RenewedAgreement)
  private val surrenderAndRenewalButton: NGRRadioButtons = NGRRadioButtons("typeOfLeaseRenewal.option2", SurrenderAndRenewal)
  val ngrRadio: NGRRadio = NGRRadio(NGRRadioName(TypeOfLeaseRenewalForm.formName), Seq(renewedAgreementButton, surrenderAndRenewalButton))


  def form: Form[TypeOfLeaseRenewalForm] = Form(
    single(formName -> Forms.of[TypeOfLeaseRenewalForm])
  )
}

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
import uk.gov.hmrc.ngrraldfrontend.utils.Constants
import uk.gov.hmrc.ngrraldfrontend.utils.Constants.{renewedAgreement, surrenderAndRenewal}

sealed trait WhatTypeOfLeaseRenewalForm extends RadioEntry

object WhatTypeOfLeaseRenewalForm {
  val formName = "type-of-renewal"

  case object RenewedAgreement extends WhatTypeOfLeaseRenewalForm

  case object SurrenderAndRenewal extends WhatTypeOfLeaseRenewalForm

  val values: Set[WhatTypeOfLeaseRenewalForm] = Set(RenewedAgreement, SurrenderAndRenewal)

  implicit val formatter: Formatter[WhatTypeOfLeaseRenewalForm] = new Formatter[WhatTypeOfLeaseRenewalForm] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], WhatTypeOfLeaseRenewalForm] = {
      data.get(key).collectFirst {
        case Constants.renewedAgreement => RenewedAgreement
        case Constants.surrenderAndRenewal => SurrenderAndRenewal
      }.toRight(Seq(FormError(key, "typeOfLeaseRenewal.required.error")))
    }

    override def unbind(key: String, value: WhatTypeOfLeaseRenewalForm): Map[String, String] = Map(
      key -> (value match {
        case RenewedAgreement => renewedAgreement
        case SurrenderAndRenewal => surrenderAndRenewal
      })
    )
  }

  private val renewedAgreementButton: NGRRadioButtons = NGRRadioButtons("typeOfLeaseRenewal.option1", RenewedAgreement)
  private val surrenderAndRenewalButton: NGRRadioButtons = NGRRadioButtons("typeOfLeaseRenewal.option2", SurrenderAndRenewal)
  val ngrRadio: NGRRadio = NGRRadio(NGRRadioName(WhatTypeOfLeaseRenewalForm.formName), Seq(renewedAgreementButton, surrenderAndRenewalButton))


  def form: Form[WhatTypeOfLeaseRenewalForm] = Form(
    single(formName -> Forms.of[WhatTypeOfLeaseRenewalForm])
  )
}

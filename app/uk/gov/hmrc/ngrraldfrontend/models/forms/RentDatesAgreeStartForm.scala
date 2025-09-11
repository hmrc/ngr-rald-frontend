/*
 * Copyright 2024 HM Revenue & Customs
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
import play.api.data.validation.Constraint
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings

final case class RentDatesAgreeStartForm(agreedDate: NGRDate, startPayingDate: NGRDate)

object RentDatesAgreeStartForm extends CommonFormValidators with DateMappings with Mappings {
  implicit val format: OFormat[RentDatesAgreeStartForm] = Json.format[RentDatesAgreeStartForm]

  def unapply(rentDatesAgreeStartForm: RentDatesAgreeStartForm): Option[(NGRDate, NGRDate)] =
    Some(rentDatesAgreeStartForm.agreedDate, rentDatesAgreeStartForm.startPayingDate)

  def form: Form[RentDatesAgreeStartForm] = {
    Form(
      mapping(
        "agreedDate" -> dateMapping
          .verifying(
            firstError(
              isDateEmpty(errorKeys("rentDatesAgreeStart", "agreedDate")),
              isDateValid("rentDatesAgreeStart.agreedDate.invalid.error"),
              isDateAfter1900("rentDatesAgreeStart.agreedDate.before.1900.error")
            )
          ),
        "startPayingDate" -> dateMapping
          .verifying(
            firstError(
              isDateEmpty(errorKeys("rentDatesAgreeStart", "startPayingDate")),
              isDateValid("rentDatesAgreeStart.startPayingDate.invalid.error"),
              isDateAfter1900("rentDatesAgreeStart.startPayingDate.before.1900.error")
            )
          )
      )(RentDatesAgreeStartForm.apply)(RentDatesAgreeStartForm.unapply)
    )
  }

}




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

import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Legend, Text}
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings

final case class RentPeriodsForm(radioValue: String)

object RentPeriodsForm extends Mappings {
  implicit val format: OFormat[RentPeriodsForm] = Json.format[RentPeriodsForm]

  private lazy val radioUnselectedError = "rentPeriods.error.required"
  private val rentPeriodsRadio    = "rent-periods-radio"

  def unapply(rentPeriodsForm: RentPeriodsForm): Option[String] = Some(rentPeriodsForm.radioValue)

  def form: Form[RentPeriodsForm] = {
    Form(
      mapping(
        rentPeriodsRadio -> radioText(radioUnselectedError)
      )(RentPeriodsForm.apply)(RentPeriodsForm.unapply)
    )
  }

  private val yes: NGRRadioButtons = NGRRadioButtons(radioContent = "rentPeriods.yes", radioValue = No)
  private val no: NGRRadioButtons = NGRRadioButtons(radioContent = "rentPeriods.no", radioValue = Yes)

  def ngrRadio(form: Form[RentPeriodsForm])(implicit messages: Messages): NGRRadio =
    NGRRadio(ngrTitle = Some(Legend(content = Text(messages("rentPeriods.radio.heading")), classes = "govuk-fieldset__legend--m", isPageHeading = true)), radioGroupName =  NGRRadioName("rent-periods-radio"), NGRRadioButtons = Seq(yes, no))

}


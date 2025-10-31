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

import play.api.i18n.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.dateinput.DateInput
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.{Fieldset, Legend}
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint

/**
 * @author Yuriy Tumakha
 */
trait NGRDateInput:

  def dateInput(dateInputName: String, label: String, isPageHeading: Boolean = false)(using messages: Messages): DateInput =
    DateInput(
      id = dateInputName,
      namePrefix = Some(dateInputName),
      fieldset = Some(Fieldset(
        legend = Some(Legend(
          content = Text(messages(label)),
          classes = "govuk-fieldset__legend--s",
          isPageHeading = isPageHeading
        ))
      )),
      hint = Some(Hint(
        content = Text(messages("date.hint"))
      ))
    )

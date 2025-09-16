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

package uk.gov.hmrc.ngrraldfrontend.views

import play.api.data.Form
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.forms.HowMuchIsTotalAnnualRentForm
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText

class InputTextSpec extends ViewBaseSpec {
  val form: Form[HowMuchIsTotalAnnualRentForm] = HowMuchIsTotalAnnualRentForm.form
  val inputText: InputText = inject[InputText]

  "InputText" when {
    "produce the same output for apply() and render()" in {
      val htmlApply = inputText(form, "how–much–is–total–annual–rent-value", "how–much–is–total–annual–rent-value", "", true)(messages).body
      val htmlRender = inputText.render(form, id = "how–much–is–total–annual–rent-value", name = "how–much–is–total–annual–rent-value", label = "", isVisible = true, isPageHeading =  false, headingMessageArgs = Seq.empty, None, None, false, "", false, None, None, messages).body
      htmlApply must not be empty
      htmlRender must not be empty
    }
  }
}

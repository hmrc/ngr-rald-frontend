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

package uk.gov.hmrc.ngrraldfrontend.models

import play.api.data.Form
import uk.gov.hmrc.govukfrontend.views.Aliases.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.FormGroup
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.Fieldset
import uk.gov.hmrc.ngrraldfrontend.helpers.TestSupport
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.forms.WhatTypeOfAgreementForm

class NGRRadioSpec extends TestSupport {

  "buildRadios" must {
    "generate a radio button with page heading" in {
      val ngrButton1 = NGRRadioButtons("whatTypeOfAgreement.LeaseOrTenancy", LeaseOrTenancy)
      val ngrButton2 = NGRRadioButtons("whatTypeOfAgreement.written", Written)
      val ngrButton3 = NGRRadioButtons("whatTypeOfAgreement.verbal", Verbal)
      val ngrRadioName = NGRRadioName("radioName")
      val testnNgrTitle = Some(NGRRadioHeader(title = "radioName", classes = "", isPageHeading = true))
      val ngrRadios = NGRRadio(radioGroupName = ngrRadioName, NGRRadioButtons = Seq(ngrButton1, ngrButton2, ngrButton3), ngrTitle = testnNgrTitle)

      lazy val form: Form[WhatTypeOfAgreementForm] =  WhatTypeOfAgreementForm.form
      NGRRadio.buildRadios(form, ngrRadios) mustBe
        Radios(
          Some(Fieldset(None, Some(Legend(Text("radioName"), "", true)), "", None, Map())),
          None, None, FormGroup(None, Map(), None, None), Some("radioName"), "radioName",
          List(
            RadioItem(Text("Lease or tenancy agreement"), None, Some("LeaseOrTenancy"), None, Some(Hint(None, "", Map(), Text(""))), None, false, None, false, Map()),
            RadioItem(Text("Licence or other type of written agreement"), None, Some("Written"), None, Some(Hint(None, "", Map(), Text(""))), None, false, None, false, Map()),
            RadioItem(Text("Verbal agreement"), None, Some("Verbal"), None, Some(Hint(None, "", Map(), Text(""))), None, false, None, false, Map())
          ),
          "govuk-radios", Map(), None
        )
    }

    "generate a radio button with a warning" in {
      val ngrButton1 = NGRRadioButtons("Yes", Yes)
      val ngrButton2 = NGRRadioButtons("No", No)
      val ngrRadioName = NGRRadioName("radioName")
      val testnNgrTitle = Some(NGRRadioHeader(title = "radioName", classes = "", isPageHeading = true))
      val ngrRadios = NGRRadio(radioGroupName = ngrRadioName, NGRRadioButtons = Seq(ngrButton1, ngrButton2), ngrTitle = testnNgrTitle)

      lazy val form: Form[WhatTypeOfAgreementForm] =  WhatTypeOfAgreementForm.form.withError("radioName", "error message")
      NGRRadio.buildRadios(form ,ngrRadios) mustBe
        Radios(Some(Fieldset(None, Some(Legend(Text("radioName"), "", true)), "",
          None, Map())), None, Some(ErrorMessage(None, "", Map(), Some("Error"),
          Text("error message"))), FormGroup(None, Map(), None, None),
          Some("radioName"), "radioName",
          List(RadioItem(Text("Yes"), None, Some("Yes"),
            None, Some(Hint(None, "", Map(), Text(""))),
            None, false, None, false, Map()), RadioItem(Text("No"),
            None, Some("No"), None, Some(Hint(None, "", Map(), Text(""))),
            None, false, None, false, Map())), "govuk-radios", Map(), None)
    }
  }
}

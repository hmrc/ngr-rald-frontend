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

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.FormError
import play.api.libs.json.Json

class WhatTypeOfAgreementFormSpec extends AnyWordSpec with Matchers {

  "WhatTypeOfAgreementForm" should {

    "bind successfully with a valid whatTypeOfAgreementRadio value" in {
      val data = Map("what-type-of-agreement-radio" -> "Written")
      val boundForm = WhatTypeOfAgreementForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(WhatTypeOfAgreementForm("Written"))
    }

    "fail to bind when businessRatesBillRadio is missing" in {
      val data = Map.empty[String, String]
      val boundForm = WhatTypeOfAgreementForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("what-type-of-agreement-radio", List("whatTypeOfAgreement.error.required")))
    }

    "fail to bind when whatTypeOfAgreementRadio is empty" in {
      val data = Map("what-type-of-agreement-radio" -> "")
      val boundForm = WhatTypeOfAgreementForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("what-type-of-agreement-radio", List("whatTypeOfAgreement.error.required")))
    }


    "serialize to JSON correctly" in {
      val form = WhatTypeOfAgreementForm("Written")
      val json = Json.toJson(form)

      json shouldBe Json.obj(
        "radioValue" -> "Written"
      )
    }

    "deserialize from JSON correctly" in {
      val json = Json.obj("radioValue" -> "Verbal")
      val result = json.validate[WhatTypeOfAgreementForm]

      result.isSuccess shouldBe true
      result.get shouldBe WhatTypeOfAgreementForm("Verbal")
    }

    "fail deserialization if businessRatesBillRadio is missing" in {
      val json = Json.obj()
      val result = json.validate[WhatTypeOfAgreementForm]

      result.isError shouldBe true
    }
  }
}
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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.FormError
import play.api.libs.json.{JsValue, Json}

class WhatIsYourRentBasedOnFormSpec extends AnyWordSpec with Matchers {
  val over250Characters = "Bug Me Not PVT LTD, RODLEY LANE, RODLEY, LEEDS, BH1 1HU What is your rent based on?Open market value This is the rent a landlord could rent the property for if, it was available to anyoneA percentage of open market value This is a percentage of the rent a landlord could rent the property for if, it was available to anyoneTurnover top-up The rent is a fixed base rent with an additional payment based on a percentage of your turnoverA percentage of expected turnover The rent paid is based on a percentage of turnoverTotal Occupancy Cost leases (TOCs)The rent is the total cost of leasing the property. It includes base rent, business rates, insurance and utilities. It also includes common area maintenance and tenant improvements Indexation The rent is reviewed according to an index (such as Retail Price Index)Other The rent was agreed another way Can you tell us how your rent was agreed?"

  "WhatIsYourRentBasedOnForm" should {

    "bind successfully with a valid input value 'TotalOccupancyCost'" in {
      val data = Map("rent-based-on-radio" -> "TotalOccupancyCost")
      val boundForm = WhatIsYourRentBasedOnForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(WhatIsYourRentBasedOnForm("TotalOccupancyCost", None))
    }

    "bind successfully with a valid input value 'Other' and some description for Other" in {
      val data = Map("rent-based-on-radio" -> "Other",
      "rent-based-on-other-desc" -> "The rent was agreed")
      val boundForm = WhatIsYourRentBasedOnForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(WhatIsYourRentBasedOnForm("Other", Some("The rent was agreed")))
    }

    "bind successfully with a valid input value 'TotalOccupancyCost' and Other description is empty" in {
      val data = Map("rent-based-on-radio" -> "TotalOccupancyCost",
        "rent-based-on-other-desc" -> "")
      val boundForm = WhatIsYourRentBasedOnForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(WhatIsYourRentBasedOnForm("TotalOccupancyCost", Some("")))
    }

    "bind successfully with a valid input value 'PercentageTurnover' and Other description is blank" in {
      val data = Map("rent-based-on-radio" -> "PercentageTurnover",
        "rent-based-on-other-desc" -> "        ")
      val boundForm = WhatIsYourRentBasedOnForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(WhatIsYourRentBasedOnForm("PercentageTurnover", Some("")))
    }

    "bind successfully with a valid input value 'PercentageTurnover' and Other description is over 250 characters" in {
      val data = Map("rent-based-on-radio" -> "PercentageTurnover",
        "rent-based-on-other-desc" -> over250Characters)
      val boundForm = WhatIsYourRentBasedOnForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(WhatIsYourRentBasedOnForm("PercentageTurnover", Some(over250Characters)))
    }

    "fail to bind when input value 'Other' and Other description is blank" in {
      val data = Map("rent-based-on-radio" -> "Other",
        "rent-based-on-other-desc" -> "      ")
      val boundForm = WhatIsYourRentBasedOnForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("rent-based-on-other-desc", List("whatIsYourRentBasedOn.otherText.error.required")))
    }

    "fail to bind when input value 'Other' and Other description is over 250 characters" in {
      val data = Map("rent-based-on-radio" -> "Other",
        "rent-based-on-other-desc" -> over250Characters)
      val boundForm = WhatIsYourRentBasedOnForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("rent-based-on-other-desc", List("whatIsYourRentBasedOn.otherText.error.maxLength")))
    }

    "fail to bind when input is missing" in {
      val data = Map.empty[String, String]
      val boundForm = WhatIsYourRentBasedOnForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("rent-based-on-radio", List("whatIsYourRentBasedOn.error.required")))
    }

    "serialize to JSON correctly" in {
      val form = WhatIsYourRentBasedOnForm("Other", Some("The rent agreement"))
      val json = Json.toJson(form)

      json shouldBe Json.obj(
        "radioValue" -> "Other",
        "rentBasedOnOther" -> "The rent agreement"
      )
    }

    "deserialize from JSON correctly" in {
      val json = Json.obj(
        "radioValue" -> "Other",
        "rentBasedOnOther" -> "The rent agreement"
      )
      val result = json.validate[WhatIsYourRentBasedOnForm]

      result.isSuccess shouldBe true
      result.get shouldBe WhatIsYourRentBasedOnForm("Other", Some("The rent agreement"))
    }

    "fail deserialization if value is missing" in {
      val json = Json.obj()
      val result = json.validate[WhatIsYourRentBasedOnForm]

      result.isError shouldBe true
    }
  }
}

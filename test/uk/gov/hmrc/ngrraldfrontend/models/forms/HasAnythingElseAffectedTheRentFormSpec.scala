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
import play.api.libs.json.Json

class HasAnythingElseAffectedTheRentFormSpec extends AnyWordSpec with Matchers {
  val over250Characters = "Bug Me Not PVT LTD, RODLEY LANE, RODLEY, LEEDS, BH1 1HU What is your rent based on?Open market value This is the rent a landlord could rent the property for if, it was available to anyoneA percentage of open market value This is a percentage of the rent a landlord could rent the property for if, it was available to anyoneTurnover top-up The rent is a fixed base rent with an additional payment based on a percentage of your turnoverA percentage of expected turnover The rent paid is based on a percentage of turnoverTotal Occupancy Cost leases (TOCs)The rent is the total cost of leasing the property. It includes base rent, business rates, insurance and utilities. It also includes common area maintenance and tenant improvements Indexation The rent is reviewed according to an index (such as Retail Price Index)Other The rent was agreed another way Can you tell us how your rent was agreed?"

  "HasAnythingElseAffectedTheRentForm" should {

    "bind successfully with a valid input value 'false'" in {
      val data = Map("hasAnythingElseAffectedTheRent" -> "false")
      val boundForm = HasAnythingElseAffectedTheRentForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(HasAnythingElseAffectedTheRentForm("false", None))
    }

    "bind successfully with a valid input value 'true' and reason of 'A new train station opened near me'" in {
      val data = Map(
        "hasAnythingElseAffectedTheRent" -> "true",
        "reason" -> "A new train station opened near me"
      )
      val boundForm = HasAnythingElseAffectedTheRentForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(HasAnythingElseAffectedTheRentForm("true", Some("A new train station opened near me")))
    }

    "bind successfully with a valid input value 'false' and reason description is empty" in {
      val data = Map("hasAnythingElseAffectedTheRent" -> "false",
                     "reason" -> "")
      val boundForm = HasAnythingElseAffectedTheRentForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(HasAnythingElseAffectedTheRentForm("false", Some("")))
    }

    "bind successfully with a valid input value 'false' and Other description is over 250 characters" in {
      val data = Map("hasAnythingElseAffectedTheRent" -> "false",
                     "reason" -> over250Characters)
      val boundForm = HasAnythingElseAffectedTheRentForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(HasAnythingElseAffectedTheRentForm("false", Some(over250Characters)))
    }

    "fail to bind when input value 'true' and reason is blank" in {
      val data = Map("hasAnythingElseAffectedTheRent" -> "true",
                     "reason" -> "      ")
      val boundForm = HasAnythingElseAffectedTheRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("reason", List("hasAnythingElseAffectedTheRent.reason.error.required")))
    }

    "fail to bind when input value 'Other' and Other description is over 250 characters" in {
      val data = Map("hasAnythingElseAffectedTheRent" -> "true",
                     "reason" -> over250Characters)
      val boundForm = HasAnythingElseAffectedTheRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("reason", List("hasAnythingElseAffectedTheRent.reason.error.maxLength")))
    }

    "fail to bind when input is missing" in {
      val data = Map.empty[String, String]
      val boundForm = HasAnythingElseAffectedTheRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("hasAnythingElseAffectedTheRent", List("hasAnythingElseAffectedTheRent.required.error")))
    }

    "serialize to JSON correctly" in {
      val form = HasAnythingElseAffectedTheRentForm("true", Some("A new train station opened near me"))
      val json = Json.toJson(form)

      json shouldBe Json.obj(
        "radioValue" -> "true",
        "reason" -> "A new train station opened near me"
      )
    }

    "deserialize from JSON correctly" in {
      val json = Json.obj(
        "radioValue" -> "true",
        "reason" -> "A new train station opened near me"
      )
      val result = json.validate[HasAnythingElseAffectedTheRentForm]

      result.isSuccess shouldBe true
      result.get shouldBe HasAnythingElseAffectedTheRentForm("true", Some("A new train station opened near me"))
    }

    "fail deserialization if value is missing" in {
      val json = Json.obj()
      val result = json.validate[HasAnythingElseAffectedTheRentForm]

      result.isError shouldBe true
    }
  }
}

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

import scala.collection.immutable.ArraySeq

class RentFreePeriodFormSpec extends AnyWordSpec with Matchers {
  "RentFreePeriodForm" should {

    "bind successfully with a valid input values" in {
      val data = Map("rentFreePeriodMonths" -> "5",
        "reasons" -> "Any reasons"
      )
      val boundForm = RentFreePeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(RentFreePeriodForm(5, "Any reasons"))
    }

    "fail to bind when months is missing" in {
      val data = Map("rentFreePeriodMonths" -> "",
        "reasons" -> "Any reasons"
      )
      val boundForm = RentFreePeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("rentFreePeriodMonths", List("rentFreePeriod.months.required.error"), ArraySeq("rentFreePeriodMonths")))
    }

    "fail to bind when reasons is missing" in {
      val data = Map("rentFreePeriodMonths" -> "5",
        "reasons" -> ""
      )
      val boundForm = RentFreePeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("reasons", List("rentFreePeriod.reasons.required.error"), ArraySeq("reasons")))
    }

    "fail to bind when months isn't numberic" in {
      val data = Map("rentFreePeriodMonths" -> "&A",
        "reasons" -> "Any reasons"
      )
      val boundForm = RentFreePeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("rentFreePeriodMonths", List("rentFreePeriod.months.invalid.error"), ArraySeq("^\\d+$")))
    }

    "fail to bind when months is less than 1" in {
      val data = Map("rentFreePeriodMonths" -> "0",
        "reasons" -> "Any reasons"
      )
      val boundForm = RentFreePeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("rentFreePeriodMonths", List("rentFreePeriod.months.minimum.error"), ArraySeq(1)))
    }

    "fail to bind when months is greater than 99" in {
      val data = Map("rentFreePeriodMonths" -> "100",
        "reasons" -> "Any reasons"
      )
      val boundForm = RentFreePeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("rentFreePeriodMonths", List("rentFreePeriod.months.maximum.error"), ArraySeq(99)))
    }

    "serialize to JSON correctly" in {
      val form = RentFreePeriodForm(5, "Any reasons")
      val json = Json.toJson(form)

      json shouldBe Json.obj(
        "rentFreePeriodMonths" -> 5,
        "reasons" -> "Any reasons"
      )
    }

    "deserialize from JSON correctly" in {
      val json = Json.obj(
        "rentFreePeriodMonths" -> 5,
        "reasons" -> "Any reasons"
      )
      val result = json.validate[RentFreePeriodForm]

      result.isSuccess shouldBe true
      result.get shouldBe RentFreePeriodForm(5, "Any reasons")
    }

    "fail deserialization if value is missing" in {
      val json = Json.obj()
      val result = json.validate[RentFreePeriodForm]

      result.isError shouldBe true
    }
  }
}

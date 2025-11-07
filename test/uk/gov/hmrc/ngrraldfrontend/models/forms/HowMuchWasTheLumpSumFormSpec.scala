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
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.FormError
import play.api.libs.json.Json

import scala.collection.immutable.ArraySeq

class HowMuchWasTheLumpSumFormSpec extends AnyWordSpec with Matchers {

  "HowMuchWasTheLumpSumForm" should {

    "bind valid input" in {
      val data = Map("how–much–was–the–lump–sum-value" -> "123456.78")
      val boundForm = HowMuchWasTheLumpSumForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(HowMuchWasTheLumpSumForm(BigDecimal("123456.78")))
    }

    "bind valid input when rounding up" in {
      val data = Map("how–much–was–the–lump–sum-value" -> "123456.78561")
      val boundForm = HowMuchWasTheLumpSumForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(HowMuchWasTheLumpSumForm(BigDecimal("123456.79")))
    }


    "bind amount with commas" in {
      val data = Map(
        "how–much–was–the–lump–sum-value" -> "9,999,999.99",
      )
      val boundForm = HowMuchWasTheLumpSumForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(HowMuchWasTheLumpSumForm(BigDecimal("9999999.99")))
    }

    "fail to bind empty input" in {
      val data = Map("how–much–was–the–lump–sum-value" -> "")
      val boundForm = HowMuchWasTheLumpSumForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("how–much–was–the–lump–sum-value", List("howMuchWasTheLumpSum.empty.error"), ArraySeq("how–much–was–the–lump–sum-value")))
    }

    "fail to bind non-numeric input" in {
      val data = Map("how–much–was–the–lump–sum-value" -> "abc")
      val boundForm = HowMuchWasTheLumpSumForm.form.bind(data)

      boundForm.errors should contain(FormError("how–much–was–the–lump–sum-value", List("howMuchWasTheLumpSum.format.error"), ArraySeq("^\\d+\\.?\\d{0,}$")))
    }

    "fail to bind input greater than 9999999.99" in {
      val data = Map("how–much–was–the–lump–sum-value" -> "10000000.00")
      val boundForm = HowMuchWasTheLumpSumForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("how–much–was–the–lump–sum-value", List("howMuchWasTheLumpSum.tooLarge.error"), ArraySeq(9999999.99)))
    }

    "bind edge case of exactly 9999999.99" in {
      val data = Map("how–much–was–the–lump–sum-value" -> "9999999.99")
      val boundForm = HowMuchWasTheLumpSumForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(HowMuchWasTheLumpSumForm(BigDecimal("9999999.99")))
    }
  }

  "serialize to JSON correctly" in {
    val form = HowMuchWasTheLumpSumForm(BigDecimal("9999999.99"))
    val json = Json.toJson(form)

    json shouldBe Json.obj(
      "lumpSum" -> 9999999.99
    )
  }

  "deserialize from JSON correctly" in {
    val json = Json.obj(
      "lumpSum" -> 9999999.99
    )
    val result = json.validate[HowMuchWasTheLumpSumForm]

    result.isSuccess shouldBe true
    result.get shouldBe HowMuchWasTheLumpSumForm(BigDecimal("9999999.99"))
  }

  "fail deserialization if value is missing" in {
    val json = Json.obj()
    val result = json.validate[HowMuchWasTheLumpSumForm]

    result.isError shouldBe true
  }
}



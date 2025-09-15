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

class HowMuchIsTotalAnnualRentFormSpec extends AnyWordSpec with Matchers {

  "AnnualRentForm" should {

    "bind valid input" in {
      val data = Map("how–much–is–total–annual–rent-value" -> "123456.78")
      val boundForm = HowMuchIsTotalAnnualRentForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(HowMuchIsTotalAnnualRentForm(BigDecimal("123456.78")))
    }

    "fail to bind empty input" in {
      val data = Map("how–much–is–total–annual–rent-value" -> "")
      val boundForm = HowMuchIsTotalAnnualRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("how–much–is–total–annual–rent-value", List("howMuchIsTotalAnnualRent.empty.error"), ArraySeq("how–much–is–total–annual–rent-value")))
    }

    "fail to bind non-numeric input" in {
      val data = Map("how–much–is–total–annual–rent-value" -> "abc")
      val boundForm = HowMuchIsTotalAnnualRentForm.form.bind(data)

      boundForm.errors should contain(FormError("how–much–is–total–annual–rent-value", List("howMuchIsTotalAnnualRent.format.error"), ArraySeq("([0-9]+\\.[0-9]+|[0-9]+)")))
    }

    "fail to bind input greater than 9999999.99" in {
      val data = Map("how–much–is–total–annual–rent-value" -> "10000000.00")
      val boundForm = HowMuchIsTotalAnnualRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("how–much–is–total–annual–rent-value", List("howMuchIsTotalAnnualRent.tooLarge.error"), ArraySeq(9999999.99)))
    }

    "bind edge case of exactly 9999999.99" in {
      val data = Map("how–much–is–total–annual–rent-value" -> "9999999.99")
      val boundForm = HowMuchIsTotalAnnualRentForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(HowMuchIsTotalAnnualRentForm(BigDecimal("9999999.99")))
    }
  }

  "serialize to JSON correctly" in {
    val form = HowMuchIsTotalAnnualRentForm(BigDecimal("9999999.99"))
    val json = Json.toJson(form)

    json shouldBe Json.obj(
      "annualRent" -> 9999999.99
    )
  }

  "deserialize from JSON correctly" in {
    val json = Json.obj(
      "annualRent" -> 9999999.99
    )
    val result = json.validate[HowMuchIsTotalAnnualRentForm]

    result.isSuccess shouldBe true
    result.get shouldBe HowMuchIsTotalAnnualRentForm(BigDecimal("9999999.99"))
  }

  "fail deserialization if value is missing" in {
    val json = Json.obj()
    val result = json.validate[HowMuchIsTotalAnnualRentForm]

    result.isError shouldBe true
  }
}



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


import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.data.{Form, FormError}
import play.api.libs.json.Json

class HowMuchIsTotalAnnualRentFormSpec extends AnyFlatSpec with Matchers {

  val form: Form[HowMuchIsTotalAnnualRentForm] = HowMuchIsTotalAnnualRentForm.form
  private lazy val annualRent = "how–much–is–total–annual–rent-value"
  "AnnualRentForm" should "bind valid input" in {
    val data = Map(annualRent -> "123456.78")
    val boundForm = form.bind(data)

    boundForm.errors shouldBe empty
    boundForm.value shouldBe Some(HowMuchIsTotalAnnualRentForm(BigDecimal("123456.78")))
  }

  it should "fail to bind empty input" in {
    val data = Map(annualRent -> "")
    val boundForm = form.bind(data)

    boundForm.errors should contain(FormError(annualRent, "howMuchIsTotalAnnualRent.empty.error"))
  }

  it should "fail to bind non-numeric input" in {
    val data = Map(annualRent -> "abc")
    val boundForm = form.bind(data)

    boundForm.errors should contain(FormError(annualRent, "howMuchIsTotalAnnualRent.format.error"))
  }

  it should "fail to bind input greater than 9999999.99" in {
    val data = Map(annualRent -> "10000000.00")
    val boundForm = form.bind(data)

    boundForm.errors should contain(FormError(annualRent, "howMuchIsTotalAnnualRent.tooLarge.error"))
  }

  it should "bind edge case of exactly 9999999.99" in {
    val data = Map(annualRent -> "9999999.99")
    val boundForm = form.bind(data)

    boundForm.errors shouldBe empty
    boundForm.value shouldBe Some(HowMuchIsTotalAnnualRentForm(BigDecimal("9999999.99")))
  }

  it should "serialize to JSON correctly" in {
    val form = HowMuchIsTotalAnnualRentForm(BigDecimal("9999999.99"))
    val json = Json.toJson(form)

    json shouldBe Json.obj(
      "annualRent" -> 9999999.99
    )
  }

  it should "deserialize from JSON correctly" in {
    val json = Json.obj(
      "annualRent" -> 9999999.99
    )
    val result = json.validate[HowMuchIsTotalAnnualRentForm]

    result.isSuccess shouldBe true
    result.get shouldBe HowMuchIsTotalAnnualRentForm(BigDecimal("9999999.99"))
  }

  it should "fail deserialization if value is missing" in {
    val json = Json.obj()
    val result = json.validate[HowMuchIsTotalAnnualRentForm]

    result.isError shouldBe true
  }
}



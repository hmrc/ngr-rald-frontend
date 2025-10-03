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
import play.api.data.FormError

class RentPeriodsFormSpec extends AnyFlatSpec with Matchers {

  val validData = Map(
    "rent-periods-radio" -> "true"
  )

  "RentPeriodsForm" should "bind valid data successfully" in {
    val boundForm = RentPeriodsForm.form.bind(validData)

    boundForm.errors shouldBe empty
    boundForm.value shouldBe Some(RentPeriodsForm("true"))
  }

  it should "fail when radio input is missing" in {
    val data = validData - "rent-periods-radio"
    val boundForm = RentPeriodsForm.form.bind(data)

    boundForm.errors shouldBe List(FormError("rent-periods-radio", List("rentPeriods.error.required"), List()))
  }

  "RentPeriods.unapply" should "extract fields correctly" in {
    val form = RentPeriodsForm("true")
    val result = RentPeriodsForm.unapply(form)
    result shouldBe Some("true")
  }
}

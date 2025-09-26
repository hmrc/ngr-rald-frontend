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

class RepairsAndInsuranceFormSpec extends AnyFlatSpec with Matchers {

  val validData = Map(
    "repairsAndInsurance-internalRepairs-radio-value" -> "InternalRepairsYou",
    "repairsAndInsurance-externalRepairs-radio-value" -> "ExternalRepairsYou",
    "repairsAndInsurance-buildingInsurance-radio-value" -> "BuildingInsuranceYou"
  )

  "RepairsAndInsuranceForm" should "bind valid data successfully" in {
    val boundForm = RepairsAndInsuranceForm.form.bind(validData)
    boundForm.errors shouldBe empty
    boundForm.value shouldBe Some(RepairsAndInsuranceForm("InternalRepairsYou", "ExternalRepairsYou", "BuildingInsuranceYou"))
  }

  it should "fail when internal repairs radio input is missing" in {
    val data = validData - "repairsAndInsurance-internalRepairs-radio-value"
    val boundForm = RepairsAndInsuranceForm.form.bind(data)

    boundForm.errors shouldBe List(FormError("repairsAndInsurance-internalRepairs-radio-value", List("repairsAndInsurance.internalRepairs.radio.required.error"), List()))
  }

  it should "fail when external repairs radio input is missing" in {
    val data = validData - "repairsAndInsurance-externalRepairs-radio-value"
    val boundForm = RepairsAndInsuranceForm.form.bind(data)

    boundForm.errors shouldBe List(FormError("repairsAndInsurance-externalRepairs-radio-value", List("repairsAndInsurance.externalRepairs.radio.required.error"), List()))
  }

  it should "fail when building insurance radio input is missing" in {
    val data = validData - "repairsAndInsurance-buildingInsurance-radio-value"
    val boundForm = RepairsAndInsuranceForm.form.bind(data)

    boundForm.errors shouldBe List(FormError("repairsAndInsurance-buildingInsurance-radio-value", List("repairsAndInsurance.buildingInsurance.radio.required.error"), List()))
  }

  "RentPeriods.unapply" should "extract fields correctly" in {
    val form = RepairsAndInsuranceForm("InternalRepairsYou", "ExternalRepairsYou", "BuildingInsuranceYou")
    val result = RepairsAndInsuranceForm.unapply(form)
    result shouldBe Some("InternalRepairsYou", "ExternalRepairsYou", "BuildingInsuranceYou")
  }

}


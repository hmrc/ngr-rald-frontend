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

class WhatYourRentIncludesFormSpec extends AnyWordSpec with Matchers {
  "WhatYourRentIncludesForm" should {

    "bind successfully with all radio inputs selected for not OTC lease" in {
      val data = Map(
        "livingAccommodationRadio" -> "true",
        "rentPartAddressRadio" -> "false",
        "rentEmptyShellRadio" -> "true",
        "rentIncBusinessRatesRadio" -> "false",
        "rentIncWaterChargesRadio" -> "false",
        "rentIncServiceRadio" -> "true",
        "bedroomNumbers" -> "6"
      )
      val boundForm = WhatYourRentIncludesForm.form(isOTCLease = false).bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(WhatYourRentIncludesForm("true", "false", "true", "false", "false", "true", Some("6")))
    }

    "bind successfully with all radio inputs selected for OTC lease" in {
      val data = Map(
        "livingAccommodationRadio" -> "true",
        "rentPartAddressRadio" -> "false",
        "rentEmptyShellRadio" -> "true",
        "bedroomNumbers" -> "6"
      )
      val boundForm = WhatYourRentIncludesForm.form(isOTCLease = true).bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(WhatYourRentIncludesForm("true", "false", "true", "", "", "", Some("6")))
    }

    "fail to bind when livingAccommodationRadio input is missing" in {
      val data = Map(
        "livingAccommodationRadio" -> "",
        "rentPartAddressRadio" -> "false",
        "rentEmptyShellRadio" -> "true",
        "rentIncBusinessRatesRadio" -> "false",
        "rentIncWaterChargesRadio" -> "false",
        "rentIncServiceRadio" -> "true",
        "bedroomNumbers" -> ""
      )
      val boundForm = WhatYourRentIncludesForm.form(isOTCLease = false).bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("livingAccommodationRadio", List("whatYourRentIncludes.radio.1.required")))
    }
    "fail to bind when no radios are selected for OTC lease" in {
      val data = Map(
        "livingAccommodationRadio" -> "",
        "rentPartAddressRadio" -> "",
        "rentEmptyShellRadio" -> "",
        "bedroomNumbers" -> ""
      )
      val boundForm = WhatYourRentIncludesForm.form(isOTCLease = true).bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 3
      boundForm.errors should contain(FormError("livingAccommodationRadio", List("whatYourRentIncludes.radio.1.required")))
      boundForm.errors should contain(FormError("rentPartAddressRadio", List("whatYourRentIncludes.radio.2.required")))
      boundForm.errors should contain(FormError("rentEmptyShellRadio", List("whatYourRentIncludes.radio.3.required")))
    }
    "fail to bind when 2 radios are unselected and bedroom numbers is missing for OTC lease" in {
      val data = Map(
        "livingAccommodationRadio" -> "true",
        "rentPartAddressRadio" -> "",
        "rentEmptyShellRadio" -> "",
        "bedroomNumbers" -> ""
      )
      val boundForm = WhatYourRentIncludesForm.form(isOTCLease = true).bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 3
      boundForm.errors should contain(FormError("bedroomNumbers", List("whatYourRentIncludes.bedroom.number.required.error")))
      boundForm.errors should contain(FormError("rentPartAddressRadio", List("whatYourRentIncludes.radio.2.required")))
      boundForm.errors should contain(FormError("rentEmptyShellRadio", List("whatYourRentIncludes.radio.3.required")))
    }
    "fail to bind when rentPartAddressRadio input is missing" in {
      val data = Map(
        "livingAccommodationRadio" -> "true",
        "rentPartAddressRadio" -> "",
        "rentEmptyShellRadio" -> "true",
        "rentIncBusinessRatesRadio" -> "false",
        "rentIncWaterChargesRadio" -> "false",
        "rentIncServiceRadio" -> "true",
        "bedroomNumbers" -> "6"
      )
      val boundForm = WhatYourRentIncludesForm.form(isOTCLease = false).bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("rentPartAddressRadio", List("whatYourRentIncludes.radio.2.required")))
    }
    "fail to bind when rentEmptyShellRadio input is missing" in {
      val data = Map(
        "livingAccommodationRadio" -> "true",
        "rentPartAddressRadio" -> "true",
        "rentEmptyShellRadio" -> "",
        "rentIncBusinessRatesRadio" -> "false",
        "rentIncWaterChargesRadio" -> "false",
        "rentIncServiceRadio" -> "true",
        "bedroomNumbers" -> "6"
      )
      val boundForm = WhatYourRentIncludesForm.form(isOTCLease = false).bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("rentEmptyShellRadio", List("whatYourRentIncludes.radio.3.required")))
    }
    "fail to bind when rentIncBusinessRatesRadio input is missing" in {
      val data = Map(
        "livingAccommodationRadio" -> "true",
        "rentPartAddressRadio" -> "true",
        "rentEmptyShellRadio" -> "true",
        "rentIncBusinessRatesRadio" -> "",
        "rentIncWaterChargesRadio" -> "false",
        "rentIncServiceRadio" -> "true",
        "bedroomNumbers" -> "6"
      )
      val boundForm = WhatYourRentIncludesForm.form(isOTCLease = false).bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("rentIncBusinessRatesRadio", List("whatYourRentIncludes.radio.4.required")))
    }
    "fail to bind when rentIncWaterChargesRadio input is missing" in {
      val data = Map(
        "livingAccommodationRadio" -> "true",
        "rentPartAddressRadio" -> "true",
        "rentEmptyShellRadio" -> "true",
        "rentIncBusinessRatesRadio" -> "true",
        "rentIncWaterChargesRadio" -> "",
        "rentIncServiceRadio" -> "true",
        "bedroomNumbers" -> "6"
      )
      val boundForm = WhatYourRentIncludesForm.form(isOTCLease = false).bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("rentIncWaterChargesRadio", List("whatYourRentIncludes.radio.5.required")))
    }
    "fail to bind when rentIncServiceRadio input is missing" in {
      val data = Map(
        "livingAccommodationRadio" -> "true",
        "rentPartAddressRadio" -> "true",
        "rentEmptyShellRadio" -> "true",
        "rentIncBusinessRatesRadio" -> "true",
        "rentIncWaterChargesRadio" -> "true",
        "rentIncServiceRadio" -> "",
        "bedroomNumbers" -> "6"
      )
      val boundForm = WhatYourRentIncludesForm.form(isOTCLease = false).bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("rentIncServiceRadio", List("whatYourRentIncludes.radio.6.required")))
    }
    "fail to bind when bedroomNumbers input is missing" in {
      val data = Map(
        "livingAccommodationRadio" -> "true",
        "rentPartAddressRadio" -> "true",
        "rentEmptyShellRadio" -> "true",
        "rentIncBusinessRatesRadio" -> "true",
        "rentIncWaterChargesRadio" -> "true",
        "rentIncServiceRadio" -> "true",
        "bedroomNumbers" -> ""
      )
      val boundForm = WhatYourRentIncludesForm.form(isOTCLease = false).bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("bedroomNumbers", List("whatYourRentIncludes.bedroom.number.required.error")))
    }
    "fail to bind when bedroomNumbers input is falset numeric" in {
      val data = Map(
        "livingAccommodationRadio" -> "true",
        "rentPartAddressRadio" -> "true",
        "rentEmptyShellRadio" -> "true",
        "rentIncBusinessRatesRadio" -> "true",
        "rentIncWaterChargesRadio" -> "true",
        "rentIncServiceRadio" -> "true",
        "bedroomNumbers" -> "A^&"
      )
      val boundForm = WhatYourRentIncludesForm.form(isOTCLease = false).bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("bedroomNumbers", List("whatYourRentIncludes.bedroom.number.invalid.error")))
    }
    "fail to bind when bedroomNumbers input is mines" in {
      val data = Map(
        "livingAccommodationRadio" -> "true",
        "rentPartAddressRadio" -> "true",
        "rentEmptyShellRadio" -> "true",
        "rentIncBusinessRatesRadio" -> "true",
        "rentIncWaterChargesRadio" -> "true",
        "rentIncServiceRadio" -> "true",
        "bedroomNumbers" -> "-2"
      )
      val boundForm = WhatYourRentIncludesForm.form(isOTCLease = false).bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("bedroomNumbers", List("whatYourRentIncludes.bedroom.number.invalid.error")))
    }
    "fail to bind when bedroomNumbers input is less than 1" in {
      val data = Map(
        "livingAccommodationRadio" -> "true",
        "rentPartAddressRadio" -> "true",
        "rentEmptyShellRadio" -> "true",
        "rentIncBusinessRatesRadio" -> "true",
        "rentIncWaterChargesRadio" -> "true",
        "rentIncServiceRadio" -> "true",
        "bedroomNumbers" -> "0"
      )
      val boundForm = WhatYourRentIncludesForm.form(isOTCLease = false).bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("bedroomNumbers", List("whatYourRentIncludes.bedroom.number.minimum.error")))
    }
    "fail to bind when bedroomNumbers input is greater than 99" in {
      val data = Map(
        "livingAccommodationRadio" -> "true",
        "rentPartAddressRadio" -> "true",
        "rentEmptyShellRadio" -> "true",
        "rentIncBusinessRatesRadio" -> "true",
        "rentIncWaterChargesRadio" -> "true",
        "rentIncServiceRadio" -> "true",
        "bedroomNumbers" -> "100"
      )
      val boundForm = WhatYourRentIncludesForm.form(isOTCLease = false).bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("bedroomNumbers", List("whatYourRentIncludes.bedroom.number.maximum.error")))
    }
    "fail to bind when bedroomNumbers input is 30 digits long" in {
      val data = Map(
        "livingAccommodationRadio" -> "true",
        "rentPartAddressRadio" -> "true",
        "rentEmptyShellRadio" -> "true",
        "rentIncBusinessRatesRadio" -> "true",
        "rentIncWaterChargesRadio" -> "true",
        "rentIncServiceRadio" -> "true",
        "bedroomNumbers" -> "123123123123123123123123345678"
      )
      val boundForm = WhatYourRentIncludesForm.form(isOTCLease = false).bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("bedroomNumbers", List("whatYourRentIncludes.bedroom.number.maximum.error")))
    }
    
    "serialize to JSON correctly" in {
      val form = WhatYourRentIncludesForm("true", "false", "true", "false", "false", "true", Some("6"))
      val json = Json.toJson(form)

      json shouldBe Json.obj(
        "livingAccommodationRadio" -> "true",
        "rentPartAddressRadio" -> "false",
        "rentEmptyShellRadio" -> "true",
        "rentIncBusinessRatesRadio" -> "false",
        "rentIncWaterChargesRadio" -> "false",
        "rentIncServiceRadio" -> "true",
        "bedroomNumbers" -> "6"
      )
    }

    "deserialize from JSON correctly" in {
      val json = Json.obj(
        "livingAccommodationRadio" -> "true",
        "rentPartAddressRadio" -> "false",
        "rentEmptyShellRadio" -> "true",
        "rentIncBusinessRatesRadio" -> "false",
        "rentIncWaterChargesRadio" -> "false",
        "rentIncServiceRadio" -> "true",
        "bedroomNumbers" -> "6"
      )
      val result = json.validate[WhatYourRentIncludesForm]

      result.isSuccess shouldBe true
      result.get shouldBe WhatYourRentIncludesForm("true", "false", "true", "false", "false", "true", Some("6"))
    }

    "fail deserialization if businessRatesBillRadio is missing" in {
      val json = Json.obj()
      val result = json.validate[WhatYourRentIncludesForm]

      result.isError shouldBe true
    }
  }
}

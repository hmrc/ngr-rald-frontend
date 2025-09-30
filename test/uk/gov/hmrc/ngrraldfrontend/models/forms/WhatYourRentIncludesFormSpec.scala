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

class WhatYourRentIncludesFormSpec extends AnyWordSpec with Matchers {
  "WhatYourRentIncludesForm" should {

    "bind successfully with all radio inputs selected" in {
      val data = Map(
        "livingAccommodationRadio" -> "Yes",
        "rentPartAddressRadio" -> "No",
        "rentEmptyShellRadio" -> "Yes",
        "rentIncBusinessRatesRadio" -> "No",
        "rentIncWaterChargesRadio" -> "No",
        "rentIncServiceRadio" -> "Yes",
        "bedroomNumbers" -> "6"
      )
      val boundForm = WhatYourRentIncludesForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(WhatYourRentIncludesForm("Yes", "No", "Yes", "No", "No", "Yes", Some("6")))
    }

    "fail to bind when livingAccommodationRadio input is missing" in {
      val data = Map(
        "livingAccommodationRadio" -> "",
        "rentPartAddressRadio" -> "No",
        "rentEmptyShellRadio" -> "Yes",
        "rentIncBusinessRatesRadio" -> "No",
        "rentIncWaterChargesRadio" -> "No",
        "rentIncServiceRadio" -> "Yes",
        "bedroomNumbers" -> ""
      )
      val boundForm = WhatYourRentIncludesForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("livingAccommodationRadio", List("whatYourRentIncludes.radio.1.required")))
    }
    "fail to bind when rentPartAddressRadio input is missing" in {
      val data = Map(
        "livingAccommodationRadio" -> "Yes",
        "rentPartAddressRadio" -> "",
        "rentEmptyShellRadio" -> "Yes",
        "rentIncBusinessRatesRadio" -> "No",
        "rentIncWaterChargesRadio" -> "No",
        "rentIncServiceRadio" -> "Yes",
        "bedroomNumbers" -> "6"
      )
      val boundForm = WhatYourRentIncludesForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("rentPartAddressRadio", List("whatYourRentIncludes.radio.2.required")))
    }
    "fail to bind when rentEmptyShellRadio input is missing" in {
      val data = Map(
        "livingAccommodationRadio" -> "Yes",
        "rentPartAddressRadio" -> "Yes",
        "rentEmptyShellRadio" -> "",
        "rentIncBusinessRatesRadio" -> "No",
        "rentIncWaterChargesRadio" -> "No",
        "rentIncServiceRadio" -> "Yes",
        "bedroomNumbers" -> "6"
      )
      val boundForm = WhatYourRentIncludesForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("rentEmptyShellRadio", List("whatYourRentIncludes.radio.3.required")))
    }
    "fail to bind when rentIncBusinessRatesRadio input is missing" in {
      val data = Map(
        "livingAccommodationRadio" -> "Yes",
        "rentPartAddressRadio" -> "Yes",
        "rentEmptyShellRadio" -> "Yes",
        "rentIncBusinessRatesRadio" -> "",
        "rentIncWaterChargesRadio" -> "No",
        "rentIncServiceRadio" -> "Yes",
        "bedroomNumbers" -> "6"
      )
      val boundForm = WhatYourRentIncludesForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("rentIncBusinessRatesRadio", List("whatYourRentIncludes.radio.4.required")))
    }
    "fail to bind when rentIncWaterChargesRadio input is missing" in {
      val data = Map(
        "livingAccommodationRadio" -> "Yes",
        "rentPartAddressRadio" -> "Yes",
        "rentEmptyShellRadio" -> "Yes",
        "rentIncBusinessRatesRadio" -> "Yes",
        "rentIncWaterChargesRadio" -> "",
        "rentIncServiceRadio" -> "Yes",
        "bedroomNumbers" -> "6"
      )
      val boundForm = WhatYourRentIncludesForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("rentIncWaterChargesRadio", List("whatYourRentIncludes.radio.5.required")))
    }
    "fail to bind when rentIncServiceRadio input is missing" in {
      val data = Map(
        "livingAccommodationRadio" -> "Yes",
        "rentPartAddressRadio" -> "Yes",
        "rentEmptyShellRadio" -> "Yes",
        "rentIncBusinessRatesRadio" -> "Yes",
        "rentIncWaterChargesRadio" -> "Yes",
        "rentIncServiceRadio" -> "",
        "bedroomNumbers" -> "6"
      )
      val boundForm = WhatYourRentIncludesForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("rentIncServiceRadio", List("whatYourRentIncludes.radio.6.required")))
    }
    "fail to bind when bedroomNumbers input is missing" in {
      val data = Map(
        "livingAccommodationRadio" -> "livingAccommodationYes",
        "rentPartAddressRadio" -> "Yes",
        "rentEmptyShellRadio" -> "Yes",
        "rentIncBusinessRatesRadio" -> "Yes",
        "rentIncWaterChargesRadio" -> "Yes",
        "rentIncServiceRadio" -> "Yes",
        "bedroomNumbers" -> ""
      )
      val boundForm = WhatYourRentIncludesForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("", List("whatYourRentIncludes.bedroom.number.required.error")))
    }
    "fail to bind when bedroomNumbers input is not numeric" in {
      val data = Map(
        "livingAccommodationRadio" -> "livingAccommodationYes",
        "rentPartAddressRadio" -> "Yes",
        "rentEmptyShellRadio" -> "Yes",
        "rentIncBusinessRatesRadio" -> "Yes",
        "rentIncWaterChargesRadio" -> "Yes",
        "rentIncServiceRadio" -> "Yes",
        "bedroomNumbers" -> "A^&"
      )
      val boundForm = WhatYourRentIncludesForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("", List("whatYourRentIncludes.bedroom.number.invalid.error")))
    }
    "fail to bind when bedroomNumbers input is mines" in {
      val data = Map(
        "livingAccommodationRadio" -> "livingAccommodationYes",
        "rentPartAddressRadio" -> "Yes",
        "rentEmptyShellRadio" -> "Yes",
        "rentIncBusinessRatesRadio" -> "Yes",
        "rentIncWaterChargesRadio" -> "Yes",
        "rentIncServiceRadio" -> "Yes",
        "bedroomNumbers" -> "-2"
      )
      val boundForm = WhatYourRentIncludesForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("", List("whatYourRentIncludes.bedroom.number.invalid.error")))
    }
    "fail to bind when bedroomNumbers input is less than 1" in {
      val data = Map(
        "livingAccommodationRadio" -> "livingAccommodationYes",
        "rentPartAddressRadio" -> "Yes",
        "rentEmptyShellRadio" -> "Yes",
        "rentIncBusinessRatesRadio" -> "Yes",
        "rentIncWaterChargesRadio" -> "Yes",
        "rentIncServiceRadio" -> "Yes",
        "bedroomNumbers" -> "0"
      )
      val boundForm = WhatYourRentIncludesForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("", List("whatYourRentIncludes.bedroom.number.minimum.error")))
    }
    "fail to bind when bedroomNumbers input is greater than 99" in {
      val data = Map(
        "livingAccommodationRadio" -> "livingAccommodationYes",
        "rentPartAddressRadio" -> "Yes",
        "rentEmptyShellRadio" -> "Yes",
        "rentIncBusinessRatesRadio" -> "Yes",
        "rentIncWaterChargesRadio" -> "Yes",
        "rentIncServiceRadio" -> "Yes",
        "bedroomNumbers" -> "100"
      )
      val boundForm = WhatYourRentIncludesForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("", List("whatYourRentIncludes.bedroom.number.maximum.error")))
    }
    "fail to bind when bedroomNumbers input is 30 digits long" in {
      val data = Map(
        "livingAccommodationRadio" -> "livingAccommodationYes",
        "rentPartAddressRadio" -> "Yes",
        "rentEmptyShellRadio" -> "Yes",
        "rentIncBusinessRatesRadio" -> "Yes",
        "rentIncWaterChargesRadio" -> "Yes",
        "rentIncServiceRadio" -> "Yes",
        "bedroomNumbers" -> "123123123123123123123123345678"
      )
      val boundForm = WhatYourRentIncludesForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("", List("whatYourRentIncludes.bedroom.number.maximum.error")))
    }
  }
}

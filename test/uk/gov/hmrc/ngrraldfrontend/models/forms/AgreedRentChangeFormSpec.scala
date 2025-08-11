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

class AgreedRentChangeFormSpec extends AnyWordSpec with Matchers {

  val fieldName = "agreed-rent-change-radio"
  val requiredError = "typeOfLeaseRenewal.required.error"

  "AgreedRentChangeForm" should {
    "bind successfully with a valid value yes" in {
      val data = Map(fieldName -> "yes")
      val boundForm = AgreedRentChangeForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(AgreedRentChangeForm("yes"))
    }
    "bind successfully with a valid value no" in {
      val data = Map(fieldName -> "no")
      val boundForm = AgreedRentChangeForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(AgreedRentChangeForm("no"))
    }

    "return an error when the radio value is missing" in {
      val data = Map.empty[String, String]
      val boundForm = AgreedRentChangeForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError(fieldName, requiredError))
    }

    "unbind correctly to a data map" in {
      val form = AgreedRentChangeForm.form.fill(AgreedRentChangeForm("no"))
      form.data shouldBe Map(fieldName -> "no")
    }
  }
}


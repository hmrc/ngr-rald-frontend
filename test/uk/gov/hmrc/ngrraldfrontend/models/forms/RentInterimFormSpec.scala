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
import play.api.data.{Form, FormError}

class RentInterimFormSpec extends AnyWordSpec with Matchers {


  "RentInterimForm" should {

    "bind successfully with valid data" in {
      val data = Map(RentInterimForm.agreedRentChangeRadio -> "yes")
      val boundForm: Form[RentInterimForm] = RentInterimForm.form.bind(data)

      boundForm.errors shouldBe empty
      boundForm.value shouldBe Some(RentInterimForm("yes"))
    }

    "fail to bind when radio value is missing" in {
      val data = Map.empty[String, String]
      val boundForm: Form[RentInterimForm] = RentInterimForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.head.message shouldBe "rentInterim.empty.error"
    }


    "unbind correctly to form data" in {
      val formData = RentInterimForm("no")
      val unbound = RentInterimForm.form.fill(formData).data

      unbound shouldBe Map(RentInterimForm.agreedRentChangeRadio -> "no")
    }
  }
}

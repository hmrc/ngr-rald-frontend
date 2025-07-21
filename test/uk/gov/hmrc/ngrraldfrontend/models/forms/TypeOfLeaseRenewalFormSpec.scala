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

class TypeOfLeaseRenewalFormSpec extends AnyWordSpec with Matchers {
  "ConnectionToPropertyForm" should {

    "bind successfully with a valid input value 'RenewedAgreement'" in {
      val data = Map("type-of-renewal" -> "RenewedAgreement")
      val boundForm = TypeOfLeaseRenewalForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(TypeOfLeaseRenewalForm.RenewedAgreement)
    }

    "bind successfully with a valid input value 'SurrenderAndRenewal'" in {
      val data = Map("type-of-renewal" -> "SurrenderAndRenewal")
      val boundForm = TypeOfLeaseRenewalForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(TypeOfLeaseRenewalForm.SurrenderAndRenewal)
    }

    "fail to bind when input is missing" in {
      val data = Map.empty[String, String]
      val boundForm = TypeOfLeaseRenewalForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("type-of-renewal", List("typeOfLeaseRenewal.required.error")))
    }
  }
}

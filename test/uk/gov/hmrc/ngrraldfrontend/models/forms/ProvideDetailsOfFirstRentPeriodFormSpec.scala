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
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.ngrraldfrontend.models.NGRDate

class ProvideDetailsOfFirstRentPeriodFormSpec extends AnyWordSpec with Matchers:

  val firstRentPeriodForm: ProvideDetailsOfFirstRentPeriodForm =
    ProvideDetailsOfFirstRentPeriodForm(
      NGRDate.fromString("2025-01-01"),
      NGRDate.fromString("2025-01-31"),
      true,
      Some(BigDecimal(1999000))
    )

  val firstRentPeriodJson: JsValue =
    Json.obj(
      "firstDateStartInput" -> Json.obj(
        "day" -> "1",
        "month" -> "1",
        "year" -> "2025"
      ),
      "firstDateEndInput" -> Json.obj(
        "day" -> "31",
        "month" -> "1",
        "year" -> "2025"
      ),
      "isRentPayablePeriod" -> true,
      "rentPeriodAmount" -> 1999000
    )

  "ProvideDetailsOfFirstRentPeriodForm" should {
    "serialize into json" in {
      Json.toJson(firstRentPeriodForm) shouldBe firstRentPeriodJson
    }

    "deserialize from json" in {
      firstRentPeriodJson.as[ProvideDetailsOfFirstRentPeriodForm] shouldBe firstRentPeriodForm
    }

    "bind successfully with all the data provided" in {
      val data = Map(
        "startDate.day" -> "1",
        "startDate.month" -> "1",
        "startDate.year" -> "2025",
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod" -> "true",
        "rentPeriodAmount" -> "1,777,000.00"
      )
      val boundForm = ProvideDetailsOfFirstRentPeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(ProvideDetailsOfFirstRentPeriodForm(
        NGRDate.fromString("2025-01-01"),
        NGRDate.fromString("2025-01-31"),
        true,
        Some(BigDecimal(1777000))
      ))
    }

    "bind successfully rent-free period" in {
      val data = Map(
        "startDate.day" -> "1",
        "startDate.month" -> "1",
        "startDate.year" -> "2025",
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod" -> "false"
      )
      val boundForm = ProvideDetailsOfFirstRentPeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(ProvideDetailsOfFirstRentPeriodForm(
        NGRDate.fromString("2025-01-01"),
        NGRDate.fromString("2025-01-31"),
        false,
        None
      ))
    }

    "unbind correctly to a data Map" in {
      val form = ProvideDetailsOfFirstRentPeriodForm.form.fill(firstRentPeriodForm)

      form.data shouldBe Map(
        "startDate.day" -> "1",
        "startDate.month" -> "1",
        "startDate.year" -> "2025",
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod" -> "true",
        "rentPeriodAmount" -> "1999000"
      )
    }

    "return errors for an empty form" in {
      val boundForm = ProvideDetailsOfFirstRentPeriodForm.form.bind(Map.empty)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("startDate.day", "error.required"))
      boundForm.errors should contain(FormError("endDate.day", "error.required"))
      boundForm.errors should contain(
        FormError("provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod", "provideDetailsOfFirstRentPeriod.firstPeriod.radio.error.required")
      )
    }

    "return errors if startDate is invalid and endDate is before 1900" in {
      val data = Map(
        "startDate.day" -> "33",
        "startDate.month" -> "13",
        "startDate.year" -> "2025",
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "1899",
        "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod" -> "true",
        "rentPeriodAmount" -> "777000"
      )
      val boundForm = ProvideDetailsOfFirstRentPeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 2
      boundForm.errors should contain(
        FormError("startDate", "provideDetailsOfFirstRentPeriod.startDate.invalid.error")
      )
      boundForm.errors should contain(
        FormError("endDate", "provideDetailsOfFirstRentPeriod.endDate.before.1900.error")
      )
    }

    "return error if endDate is before startDate" in {
      val data = Map(
        "startDate.day" -> "2",
        "startDate.month" -> "10",
        "startDate.year" -> "2025",
        "endDate.day" -> "1",
        "endDate.month" -> "10",
        "endDate.year" -> "2025",
        "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod" -> "false"
      )
      val boundForm = ProvideDetailsOfFirstRentPeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 1
      boundForm.errors should contain(
        FormError("", "provideDetailsOfFirstRentPeriod.endDate.before.startDate.error")
      )
    }

    "return error if rentPeriodAmount is empty" in {
      val data = Map(
        "startDate.day" -> "1",
        "startDate.month" -> "1",
        "startDate.year" -> "2025",
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod" -> "true",
        "rentPeriodAmount" -> ""
      )
      val boundForm = ProvideDetailsOfFirstRentPeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 1
      boundForm.errors should contain(
        FormError("rentPeriodAmount", "provideDetailsOfFirstRentPeriod.firstPeriod.amount.required.error")
      )
    }

    "return error if rentPeriodAmount contains disallowed characters" in {
      val data = Map(
        "startDate.day" -> "1",
        "startDate.month" -> "1",
        "startDate.year" -> "2025",
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod" -> "true",
        "rentPeriodAmount" -> "200bucks"
      )
      val boundForm = ProvideDetailsOfFirstRentPeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 1
      boundForm.errors should contain(
        FormError("rentPeriodAmount", "provideDetailsOfFirstRentPeriod.firstPeriod.amount.invalid.error")
      )
    }

    "return error if rentPeriodAmount exceeds £9,999,999.99" in {
      val data = Map(
        "startDate.day" -> "1",
        "startDate.month" -> "1",
        "startDate.year" -> "2025",
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod" -> "true",
        "rentPeriodAmount" -> "10,000,000.00"
      )
      val boundForm = ProvideDetailsOfFirstRentPeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 1
      boundForm.errors should contain(
        FormError("rentPeriodAmount", "provideDetailsOfFirstRentPeriod.firstPeriod.amount.max.error")
      )
    }

  }

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

package uk.gov.hmrc.ngrraldfrontend.models

import org.scalatestplus.play.PlaySpec
import play.api.data.Form
import play.api.libs.json.Json

import java.time.LocalDate

class DateSpec extends PlaySpec with DateMappings {

  "Date" should {

    "convert to LocalDate correctly" in {
      val date = Date("15", "08", "2025")
      date.date mustBe LocalDate.of(2025, 8, 15)
    }

    "format date as string correctly" in {
      val date = Date("01", "12", "2023")
      date.makeString mustBe "2023-12-01"
    }

    "serialize and deserialize to/from JSON" in {
      val date = Date("10", "11", "2022")
      val json = Json.toJson(date)
      json.toString must include("2022")
      val parsed = json.as[Date]
      parsed mustBe date
    }
  }

  "DateMappings" should {

    "bind valid form data to Date" in {
      val formData = Map("day" -> "05", "month" -> "09", "year" -> "2024")
      val boundForm = Form(dateMapping).bind(formData)
      boundForm.hasErrors mustBe false
      boundForm.value mustBe Some(Date("05", "09", "2024"))
    }
  }
}


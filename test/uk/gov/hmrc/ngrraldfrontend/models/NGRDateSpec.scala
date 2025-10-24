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

import play.api.data.Form
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.ngrraldfrontend.helpers.TestSupport
import uk.gov.hmrc.ngrraldfrontend.models.forms.AgreementForm.dateMapping

import java.time.LocalDate

class NGRDateSpec extends TestSupport {

  val ngrDate: NGRDate = NGRDate("30", "10", "2025")

  val ngrDateJson: JsValue = Json.parse(
    """
      |{
      |"day": "30",
      |"month": "10",
      |"year": "2025"
      |}
      |""".stripMargin
  )

  "NGRDate" should {
    "deserialize to json" in {
      Json.toJson(ngrDate) mustBe ngrDateJson
    }
    "serialize to json" in {
      ngrDateJson.as[NGRDate] mustBe ngrDate
    }
    "convert to LocalDate correctly" in {
      val date = NGRDate("15", "08", "2025")
      date.localDate mustBe LocalDate.of(2025, 8, 15)
    }

    "format date as string correctly" in {
      val date = NGRDate("01", "12", "2023")
      date.makeString mustBe "2023-12-01"
    }

    "serialize and deserialize to/from JSON" in {
      val date = NGRDate("10", "11", "2022")
      val json = Json.toJson(date)
      json.toString must include("2022")
      val parsed = json.as[NGRDate]
      parsed mustBe date
    }
  }

  "DateMappings" should {
    "bind valid form data to Date" in {
      val formData = Map("day" -> "05", "month" -> "09", "year" -> "2024")
      val boundForm = Form(dateMapping).bind(formData)
      boundForm.hasErrors mustBe false
      boundForm.value mustBe Some(NGRDate("05", "09", "2024"))
    }
  }
}

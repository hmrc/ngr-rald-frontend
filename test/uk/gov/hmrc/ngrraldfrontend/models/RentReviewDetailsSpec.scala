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

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.ngrraldfrontend.helpers.TestSupport

class RentReviewDetailsSpec extends TestSupport {

  val rentReviewDetails: RentReviewDetails = RentReviewDetails(BigDecimal("3000"), "OnlyGoUp", "2020-10-30", false, Some("IndependentExpert"))

  val rentReviewDetailsJson: JsValue = Json.parse(
    """
      |{
      |"annualRentAmount": 3000,
      |"whatHappensAtRentReview": "OnlyGoUp",
      |"startDate": "2020-10-30",
      |"hasAgreedNewRent": false,
      |"whoAgreed": "IndependentExpert"
      |}
      |""".stripMargin
  )

  "RentReviewDetails" should {
    "deserialize to json" in {
      Json.toJson(rentReviewDetails) mustBe rentReviewDetailsJson
    }
    "serialize to json" in {
      rentReviewDetailsJson.as[RentReviewDetails] mustBe rentReviewDetails
    }
  }
}

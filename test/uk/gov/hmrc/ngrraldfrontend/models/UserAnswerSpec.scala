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
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.RenewedAgreement
import uk.gov.hmrc.ngrraldfrontend.pages.{LandlordPage, TellUsAboutYourRenewedAgreementPage, WhatTypeOfLeaseRenewalPage}

import java.time.Instant

class UserAnswerSpec extends TestSupport {

  val userAnswers: UserAnswers = UserAnswers(credId, Json.obj(
    "tellUsAboutRenewedAgreement" -> "RenewedAgreement",
    "whatTypeOfLeaseRenewal" -> "SurrenderAndRenewal"
  ), Instant.ofEpochSecond(1759232590))

  val userAnswersJson: JsValue = Json.parse(
    """
      |{
      | "credId":"1234",
      | "data":{
      |   "tellUsAboutRenewedAgreement": "RenewedAgreement",
      |   "whatTypeOfLeaseRenewal":"SurrenderAndRenewal"
      | },
      | "lastUpdated":{
      |   "$date":{
      |     "$numberLong":"1759232590000"
      |   }
      | }
      |}
      |""".stripMargin
  )

  "UserAnswers" should {
    "deserialize to json" in {
      Json.toJson(userAnswers) mustBe userAnswersJson
    }
    "serialize to json" in {
      userAnswersJson.as[UserAnswers] mustBe userAnswers
    }
  }
  "remove method" should {
    "Remove value without error when the key isn't there and user answers shouldn't be changed" in {
      val actual = userAnswers.remove(LandlordPage).get
      actual mustBe userAnswers
    }
    "Remove the correct value and user answers shouldn't contain it any more" in {
      val actual = userAnswers.remove(WhatTypeOfLeaseRenewalPage).get
      actual.get(WhatTypeOfLeaseRenewalPage) mustBe None
    }
  }
}

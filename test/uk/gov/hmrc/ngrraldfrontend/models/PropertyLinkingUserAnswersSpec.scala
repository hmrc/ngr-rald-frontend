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

import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.ngrraldfrontend.helpers.TestSupport
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.vmvProperty.VMVProperty

class PropertyLinkingUserAnswersSpec extends TestSupport {
  implicit val credIdFormat: OFormat[CredId] = Json.format[CredId]
  implicit val vmvPropertyFormat: OFormat[VMVProperty] = Json.format[VMVProperty]
  implicit val currentRatepayerFormat: OFormat[CurrentRatepayer] = Json.format[CurrentRatepayer]

  "PropertyLinkingUserAnswers" should {

    "serialize to JSON" in {
      val answers = PropertyLinkingUserAnswers(
        credId = CredId("9900000000000101"),
        vmvProperty = property,
        currentRatepayer = Some(CurrentRatepayer(false, Some("2025-6-24"))),
        businessRatesBill = Some("BillRef123"),
        connectionToProperty = Some("Owner"),
        requestSentReference = Some("REQ123"),
        evidenceDocument = Some("evidence.pdf")
      )

      val json = Json.toJson(answers)

      (json \ "credId" \ "value").as[String] shouldBe "9900000000000101"
      (json \ "vmvProperty").as[VMVProperty] shouldBe property
      (json \ "currentRatepayer").as[CurrentRatepayer] shouldBe CurrentRatepayer(false, Some("2025-6-24"))
      (json \ "businessRatesBill").as[String] shouldBe "BillRef123"
      (json \ "connectionToProperty").as[String] shouldBe "Owner"
      (json \ "requestSentReference").as[String] shouldBe "REQ123"
      (json \ "evidenceDocument").as[String] shouldBe "evidence.pdf"
    }
  }
}

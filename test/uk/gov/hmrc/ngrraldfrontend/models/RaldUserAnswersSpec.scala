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

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.ngrraldfrontend.helpers.TestData
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.NewAgreement
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.vmvProperty.VMVProperty

class RaldUserAnswersSpec extends AnyWordSpec with Matchers with TestData {
  implicit val credIdFormat: OFormat[CredId] = Json.format[CredId]
  implicit val vmvPropertyFormat: OFormat[VMVProperty] = Json.format[VMVProperty]
  implicit val currentRatepayerFormat: OFormat[CurrentRatepayer] = Json.format[CurrentRatepayer]
  "RaldUserAnswers" should {
    "serialize to JSON" in {
      val answers = RaldUserAnswers(
        credId = CredId("cred123"),
        agreementType =  NewAgreement,
        selectedProperty = property,
        whatTypeOfAgreement = None
      )

      val json = Json.toJson(answers)

      (json \ "credId" \ "value").as[String] shouldBe "cred123"
      (json \ "selectedProperty" \ "localAuthorityReference").as[String] shouldBe property.localAuthorityReference
    }

    "deserialize from JSON" in {
      val json = Json.parse(
        """
          |{
          |   "credId" : {
          |        "value" : "9900000000000101"
          |    },
          |    "agreementType" : "NewAgreement",
          |    "selectedProperty" : {
          |        "localAuthorityReference" : "2191322564521",
          |        "valuations" : [
          |            {
          |                "descriptionText" : "GOLF",
          |                "rateableValue" : 109300,
          |                "assessmentRef" : 85141561000,
          |                "scatCode" : "249",
          |                "currentFromDate" : "2023-04-01",
          |                "effectiveDate" : "2023-04-01",
          |                "listYear" : "2023",
          |                "propertyLinkEarliestStartDate" : "2017-04-01",
          |                "primaryDescription" : "CS",
          |                "listType" : "current",
          |                "assessmentStatus" : "CURRENT",
          |                "allowedActions" : [
          |                    "check",
          |                    "challenge",
          |                    "viewDetailedValuation",
          |                    "propertyLink",
          |                    "similarProperties"
          |                ]
          |            }
          |        ],
          |        "addressFull" : "A, RODLEY LANE, RODLEY, LEEDS, BH1 7EY",
          |        "localAuthorityCode" : "4720",
          |        "uarn" : 11905603000
          |    }
          |}
          |""".stripMargin)

      val result = json.as[RaldUserAnswers]

      result.credId shouldBe CredId("9900000000000101")
      result.selectedProperty shouldBe property
    }
  }
}

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


import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.*
import uk.gov.hmrc.ngrraldfrontend.helpers.TestData
import uk.gov.hmrc.ngrraldfrontend.models.vmvProperty.{VMVProperty, Valuation}

class VMVPropertySpec extends AnyFlatSpec with Matchers with TestData {

  "VMVProperty JSON format" should "serialize and deserialize correctly" in {

    val json = Json.toJson(property)
    val expectedJson = Json.parse(
      """
        {
          "uarn":11905603000,
          "addressFull":"A, RODLEY LANE, RODLEY, LEEDS, BH1 7EY",
          "localAuthorityCode":"4720",
          "localAuthorityReference":"2191322564521",
          "valuations":[{
          "assessmentRef":85141561000,"assessmentStatus":"CURRENT","rateableValue":109300,"scatCode":"249","descriptionText":"GOLF","effectiveDate":"2023-04-01","currentFromDate":"2023-04-01","listYear":"2023","primaryDescription":"CS","allowedActions":["check","challenge","viewDetailedValuation","propertyLink","similarProperties"],"listType":"current","propertyLinkEarliestStartDate":"2017-04-01"}
          ]}
      """
    )

    json shouldEqual expectedJson
    json.validate[VMVProperty] shouldEqual JsSuccess(property)
  }

  it should "fail to deserialize invalid JSON" in {
    val invalidJson = Json.parse("""{ "uarn": "not-a-long" }""")
    invalidJson.validate[VMVProperty] shouldBe a[JsError]
  }
}


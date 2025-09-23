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

package uk.gov.hmrc.ngrraldfrontend.connectors

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.helpers.TestData
import uk.gov.hmrc.ngrraldfrontend.mocks.MockHttpV2
import uk.gov.hmrc.ngrraldfrontend.models.PropertyLinkingUserAnswers
import uk.gov.hmrc.ngrraldfrontend.models.registration.*
import uk.gov.hmrc.ngrraldfrontend.models.registration.ReferenceType.TRN
import uk.gov.hmrc.ngrraldfrontend.models.vmvProperty.VMVProperty

import scala.concurrent.Future

class NGRConnectorSpec extends MockHttpV2 with TestData {
  val ngrConnector: NGRConnector = new NGRConnector(mockHttpClientV2, mockConfig)
  val email: Email = Email("hello@me.com")
  val trn: TRNReferenceNumber = TRNReferenceNumber(TRN, "1234")

  "getPropertyLinkingUserAnswers" when {
    "Successfully return a PropertyLinkingUserAnswers" in {
      val propertyLinkingUserAnswers = PropertyLinkingUserAnswers(CredId("1234"), property)
      setupMockHttpV2Get(s"${mockConfig.nextGenerationRatesHost}/next-generation-rates/get-property-linking-user-answers")(Some(propertyLinkingUserAnswers))
      val result: Future[Option[PropertyLinkingUserAnswers]] = ngrConnector.getPropertyLinkingUserAnswers(credId)
      result.futureValue.get.credId mustBe credId
      result.futureValue.get.vmvProperty mustBe property
    }
    "PropertyLinkingUserAnswers not found" in {
      setupMockHttpV2Get(s"${mockConfig.nextGenerationRatesHost}/next-generation-rates/get-property-linking-user-answers")(None)
      val result: Future[Option[PropertyLinkingUserAnswers]] = ngrConnector.getPropertyLinkingUserAnswers(credId)
      result.futureValue mustBe None
    }
  }

  "getLinkedProperty" when {
    "Successfully return a Property" in {
      val propertyLinkingUserAnswers = PropertyLinkingUserAnswers(CredId("1234"), property)
      setupMockHttpV2Get(s"${mockConfig.nextGenerationRatesHost}/next-generation-rates/get-property-linking-user-answers")(Some(propertyLinkingUserAnswers))
      val result: Future[Option[VMVProperty]] = ngrConnector.getLinkedProperty(credId)
      result.futureValue mustBe  Some(property)
    }
    "Property not found" in {
      setupMockHttpV2Get(s"${mockConfig.nextGenerationRatesHost}/next-generation-rates/get-property-linking-user-answers")(None)
      val result = ngrConnector.getLinkedProperty(credId)
      result.futureValue mustBe None
    }
  }
}




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

package uk.gov.hmrc.ngrraldfrontend.helpers

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.registration.*
import uk.gov.hmrc.ngrraldfrontend.models.registration.ReferenceType.TRN
import uk.gov.hmrc.ngrraldfrontend.models.registration.UserType.Individual
import uk.gov.hmrc.ngrraldfrontend.models.vmvProperty.{VMVProperty, Valuation}

import java.time.LocalDate

trait TestData {
 val credId: CredId = CredId("1234")

 val property: VMVProperty = VMVProperty(
  uarn = 11905603000L,
  localAuthorityReference = "2191322564521",
  addressFull = "A, RODLEY LANE, RODLEY, LEEDS, BH1 7EY",
  localAuthorityCode = "4720",
  valuations = List(
   Valuation(
    assessmentStatus = "CURRENT",
    assessmentRef = 85141561000L,
    rateableValue = Some(109300),
    scatCode = Some("249"),
    currentFromDate = LocalDate.of(2023, 4, 1),
    effectiveDate = LocalDate.of(2023, 4, 1),
    descriptionText = "GOLF",
    listYear = "2023",
    primaryDescription = "CS",
    allowedActions = List(
     "check",
     "challenge",
     "viewDetailedValuation",
     "propertyLink",
     "similarProperties"
    ),
    propertyLinkEarliestStartDate = Some(LocalDate.of(2017, 4, 1)),
    listType = "current"
   )
  )
 )

 val firstSecondRentPeriod: ProvideDetailsOfFirstSecondRentPeriod = ProvideDetailsOfFirstSecondRentPeriod(
  firstDateStart = "2025-01-01",
  firstDateEnd = "2025-01-31",
  firstRentPeriodRadio = true,
  firstRentPeriodAmount = Some("1000"),
  secondDateStart = "2025-02-01",
  secondDateEnd = "2025-02-28",
  secondHowMuchIsRent = "1000"
 )

 val firstSecondRentPeriodNoRentPayed: ProvideDetailsOfFirstSecondRentPeriod = ProvideDetailsOfFirstSecondRentPeriod(
  firstDateStart = "2025-01-01",
  firstDateEnd = "2025-01-31",
  firstRentPeriodRadio = false,
  firstRentPeriodAmount = None,
  secondDateStart = "2025-02-01",
  secondDateEnd = "2025-02-28",
  secondHowMuchIsRent = "1000"
 )

 val testRegistrationModel: RatepayerRegistration = RatepayerRegistration(
  userType = Some(Individual),
  agentStatus = Some(AgentStatus.Agent),
  name = Some(Name("John Doe")),
  tradingName = Some(TradingName("CompanyLTD")),
  email = Some(Email("JohnDoe@digital.hmrc.gov.uk")),
  contactNumber = Some(PhoneNumber("07123456789")),
  secondaryNumber = Some(PhoneNumber("07123456789")),
  address = Some(
   Address(line1 = "99",
    line2 = Some("Wibble Rd"),
    town = "Worthing",
    county = Some("West Sussex"),
    postcode = Postcode("BN110AA")
   )
  ),
  trnReferenceNumber = Some(TRNReferenceNumber(TRN, "12345")),
  isRegistered = Some(false)
 )
 val testAddress: Address =
  Address(
   line1 = "99",
   line2 = Some("Wibble Rd"),
   town = "Worthing",
   county = Some("West Sussex"),
   postcode = Postcode("BN110AA")
  )
 val regResponseJson: JsValue = Json.parse(
  """{"userType":"Individual","agentStatus":"Agent","name":{"value":"John Doe"},"tradingName":{"value":"CompanyLTD"},"email":{"value":"JohnDoe@digital.hmrc.gov.uk"},"contactNumber":{"value":"07123456789"},"secondaryNumber":{"value":"07123456789"},"address":{"line1":"99","line2":"Wibble Rd","town":"Worthing","county":"West Sussex","postcode":{"value":"BN110AA"}},"trnReferenceNumber":{"referenceType":"TRN","value":"12345"},"isRegistered":false}
    |""".stripMargin
 )
 val minRegResponseModel: RatepayerRegistration = testRegistrationModel.copy(tradingName = None, secondaryNumber = None)
 val minRegResponseJson: JsValue = Json.parse(
  """{"userType":"Individual","agentStatus":"Agent","name":{"value":"John Doe"},"email":{"value":"JohnDoe@digital.hmrc.gov.uk"},"contactNumber":{"value":"07123456789"},"address":{"line1":"99","line2":"Wibble Rd","town":"Worthing","county":"West Sussex","postcode":{"value":"BN110AA"}},"trnReferenceNumber":{"referenceType":"TRN","value":"12345"},"isRegistered":false}
    |""".stripMargin
 )
 val regValuationModel: RatepayerRegistrationValuation = RatepayerRegistrationValuation(credId = credId, Some(testRegistrationModel))
 val regValuationJson: JsValue = Json.parse(
  """
    |{"credId":{"value":"1234"},"ratepayerRegistration":{"name":{"value":"John Doe"},"email":{"value":"JohnDoe@digital.hmrc.gov.uk"},"secondaryNumber":{"value":"07123456789"},"agentStatus":"Agent","trnReferenceNumber":{"referenceType":"TRN","value":"12345"},"userType":"Individual","contactNumber":{"value":"07123456789"},"address":{"postcode":{"value":"BN110AA"},"line1":"99","county":"West Sussex","line2":"Wibble Rd","town":"Worthing"},"tradingName":{"value":"CompanyLTD"},"isRegistered":false}}
    |""".stripMargin
 )
 val minRegValuationModel: RatepayerRegistrationValuation = RatepayerRegistrationValuation(credId = credId, None)
 val minRegValuationJson: JsValue = Json.parse(
  """
    |{"credId":{"value":"1234"}}
    |""".stripMargin
 )

 val agreementModel: Agreement = Agreement(
  agreementStart = "2025-01-01", isOpenEnded = true, openEndedDate = Some("2025-02-02"), haveBreakClause = true, breakClauseInfo = Some("he has a break clause")
 )

 val agreementModelMin: Agreement = Agreement(
  agreementStart = "2025-01-01", isOpenEnded = false, openEndedDate = None, haveBreakClause = false, breakClauseInfo = None
 )

 val agreementVerbalModelMin: AgreementVerbal = AgreementVerbal(startDate = "2025-01-01", openEnded = true, endDate = None)
 
 val agreementVerbalModel: AgreementVerbal = AgreementVerbal(startDate = "2025-01-01", openEnded = false, endDate = Some("2025-02-02"))

 val interimRentSetByTheCourtModel: InterimRentSetByTheCourt = InterimRentSetByTheCourt("10000", "1990-01")

 val landlordModel: Landlord = Landlord("Joe Bloggs", false, None)

 val rentDatesAgreeStartModel: RentDatesAgreeStart = RentDatesAgreeStart("2025-01-01", "2025-02-02")

 val whatYourRentIncludesModelAllYes: WhatYourRentIncludes = WhatYourRentIncludes(true,true,true,true,true,true,Some(5))

 val whatYourRentIncludesModelAllNo: WhatYourRentIncludes = WhatYourRentIncludes(false,false,false,false,false,false,None)
 
 val rentBasedOnModel: RentBasedOn = RentBasedOn("Other",Some("The rent was agreed"))
}
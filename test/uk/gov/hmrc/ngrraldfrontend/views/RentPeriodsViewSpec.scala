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

package uk.gov.hmrc.ngrraldfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.data.Form
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.ngrraldfrontend.actions.{FakeAuthenticatedRequest, FakeDataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.controllers.RentPeriodsController
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentPeriodsForm
import uk.gov.hmrc.ngrraldfrontend.models.{NormalMode, ProvideDetailsOfFirstSecondRentPeriod, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.views.html.RentPeriodView

import scala.concurrent.ExecutionContext

class RentPeriodsViewSpec extends ViewBaseSpec {
  implicit lazy val ec: ExecutionContext = inject[ExecutionContext]
  lazy val mcc: MessagesControllerComponents = inject[MessagesControllerComponents]
  val fakeAuth = new FakeAuthenticatedRequest(mcc.parsers.defaultBodyParser)
  val mockSessionRepository: SessionRepository = mock[SessionRepository]
  val mockNavigator: Navigator = inject[Navigator]

  def fakeData(answers: Option[UserAnswers]) = new FakeDataRetrievalAction(answers, None)

  lazy val view: RentPeriodView = inject[RentPeriodView]
  val rentPeriodsController: RentPeriodsController = new RentPeriodsController(view, fakeAuth, fakeData(None), mcc, mockSessionRepository, mockNavigator)(mockConfig, ec)
  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"

  val heading = "Rent periods"
  val title = s"$heading - GOV.UK"
  val firsPeriodStartDate = "1 February 2025"
  val firstPeriodEndDate = "1 March 2025"
  val secondPeriodStartDate = "1 April 2025"
  val secondPeriodEndDate = "1 August 2025"
  val firstPeriodRentQuestion = "Yes"
  val firstRentAmount = "£2,300.46"
  val secondRentAmount = "£1,350"
  val additionPeriodQuestion = "Do you need to add another rent period?"
  val yesRadio = "Yes"
  val noRadio = "No"
  val saveButton = "Continue"

  val firstSecondRentPeriods = ProvideDetailsOfFirstSecondRentPeriod(
    firstDateStart = "2025-02-01",
    firstDateEnd = "2025-03-01",
    firstRentPeriodRadio = true,
    firstRentPeriodAmount = Some("2300.4567"),
    secondDateStart = "2025-04-01",
    secondDateEnd = "2025-08-01",
    secondHowMuchIsRent = "1350")
  val firstTable = rentPeriodsController.firstTable(firstSecondRentPeriods)
  val secondTable = rentPeriodsController.secondTable(firstSecondRentPeriods)
  private val form: Form[RentPeriodsForm] = RentPeriodsForm.form.fillAndValidate(RentPeriodsForm("false"))
  val radio = buildRadios(form, RentPeriodsForm.rentPeriodsRadio(form))

  object Selectors {
    val navTitle = "head > title"
    val heading = "#main-content > div > div.govuk-grid-column-two-thirds > form > h1"
    val firsPeriodStartDate = "#first-period-start-date-id"
    val firstPeriodEndDate = "#first-period-end-date-id"
    val secondPeriodStartDate = "#second-period-start-date-id"
    val secondPeriodEndDate = "#second-period-end-date-id"
    val firstPeriodRentQuestion = "#first-period-has-pay-id"
    val firstRentAmount = "#first-period-rent-value-id"
    val secondRentAmount = "#second-period-rent-value-id"
    val additionPeriodQuestion = "#main-content > div > div.govuk-grid-column-two-thirds > form > div > fieldset > legend > h1"
    val yesRadio = "#main-content > div > div.govuk-grid-column-two-thirds > form > div > fieldset > div > div:nth-child(1) > label"
    val noRadio = "#main-content > div > div.govuk-grid-column-two-thirds > form > div > fieldset > div > div:nth-child(2) > label"
    val saveButton = "#continue"
  }

  "RentPeriodsView" must {
    val rentPeriodsView = view(address, form, firstTable, secondTable, radio, NormalMode)
    lazy implicit val document: Document = Jsoup.parse(rentPeriodsView.body)
    val htmlApply = view.apply(address, form, firstTable, secondTable, radio, NormalMode).body
    val htmlRender = view.render(address, form, firstTable, secondTable, radio, NormalMode, request, messages, mockConfig).body
    lazy val htmlF = view.f(address, form, firstTable, secondTable, radio, NormalMode)

    "htmlF is not empty" in {
      htmlF.toString() must not be empty
    }

    "apply must nit be the same as render" in {
      htmlApply mustBe htmlRender
    }

    "render is not empty" in {
      htmlRender must not be empty
    }

    "show the correct title" in {
      elementText(Selectors.navTitle) mustBe title
    }

    "show the correct heading" in {
      elementText(Selectors.heading) mustBe heading
    }

    "show the correct first period start date" in {
      elementText(Selectors.firsPeriodStartDate) mustBe firsPeriodStartDate
    }

    "show the correct first period end date" in {
      elementText(Selectors.firstPeriodEndDate) mustBe firstPeriodEndDate
    }

    "show the correct second period start date" in {
      elementText(Selectors.secondPeriodStartDate) mustBe secondPeriodStartDate
    }

    "show the correct second period end date" in {
      elementText(Selectors.secondPeriodEndDate) mustBe secondPeriodEndDate
    }

    "show the correct answer for if pay first period rent" in {
      elementText(Selectors.firstPeriodRentQuestion) mustBe firstPeriodRentQuestion
    }

    "show the correct first period rent amount" in {
      elementText(Selectors.firstRentAmount) mustBe firstRentAmount
    }

    "show the correct second period rent amount" in {
      elementText(Selectors.secondRentAmount) mustBe secondRentAmount
    }

    "show the correct radio question" in {
      elementText(Selectors.additionPeriodQuestion) mustBe additionPeriodQuestion
    }

    "show the correct yes radio button label" in {
      elementText(Selectors.yesRadio) mustBe yesRadio
    }

    "show the correct no radio button label" in {
      elementText(Selectors.noRadio) mustBe noRadio
    }

    "show the correct save button" in {
      elementText(Selectors.saveButton) mustBe saveButton
    }
  }
}


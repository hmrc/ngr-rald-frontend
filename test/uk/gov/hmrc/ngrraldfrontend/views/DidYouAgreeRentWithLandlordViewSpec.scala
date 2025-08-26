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
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.Radios
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.DidYouAgreeRentWithLandlordForm
import uk.gov.hmrc.ngrraldfrontend.views.html.DidYouAgreeRentWithLandlordView

class DidYouAgreeRentWithLandlordViewSpec extends ViewBaseSpec {
  lazy val view: DidYouAgreeRentWithLandlordView = inject[DidYouAgreeRentWithLandlordView]

  object Strings {
    val heading = "Did you agree the rent with your landlord or their agent?"
    val radio1 = "Yes"
    val radio2 = "No, a court set the rent"
    val continue = "Continue"
  }

  object Selectors {
    val heading = "#main-content > div > div > form > h1"
    val radio1 = "#main-content > div > div > form > div > div > div:nth-child(1) > label"
    val radio2 = "#main-content > div > div > form > div > div > div:nth-child(2) > label"
    val radio3 = "#main-content > div > div > form > div > div > div:nth-child(3) > label"
    val continue = "#continue"
  }

  val content: NavigationBarContent = NavBarPageContents.CreateNavBar(
    contents = NavBarContents(
      homePage = Some(true),
      messagesPage = Some(false),
      profileAndSettingsPage = Some(false),
      signOutPage = Some(true)
    ),
    currentPage = NavBarCurrentPage(homePage = true),
    notifications = Some(1)
  )

  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"
  private val yesButton: NGRRadioButtons = NGRRadioButtons("Yes", YesTheLandlord)
  private val noButton: NGRRadioButtons = NGRRadioButtons("No, a court set the rent", NoACourtSet)
  private val ngrRadio: NGRRadio = NGRRadio(NGRRadioName("what-type-of-agreement-radio"), Seq(yesButton, noButton))
  val form = DidYouAgreeRentWithLandlordForm.form.fillAndValidate(DidYouAgreeRentWithLandlordForm("YesTheLandlord"))
  val radio: Radios = buildRadios(form, ngrRadio)

  "DidYouAgreeRentWithLandlordView" must {
    val whatTypeOfAgreementView = view(content, address, form, radio)
    lazy implicit val document: Document = Jsoup.parse(whatTypeOfAgreementView.body)
    val htmlApply = view.apply(content, address, form, radio).body
    val htmlRender = view.render(content, address, form, radio, request, messages, mockConfig).body
    lazy val htmlF = view.f(content, address, form, radio)

    "htmlF is not empty" in {
      htmlF.toString() must not be empty
    }

    "apply must be the same as render" in {
      htmlApply mustBe htmlRender
    }

    "render is not empty" in {
      htmlRender must not be empty
    }

    "show correct heading" in {
      elementText(Selectors.heading) mustBe Strings.heading
    }

    "show correct radio 1" in {
      elementText(Selectors.radio1) mustBe Strings.radio1
    }

    "show correct radio 2" in {
      elementText(Selectors.radio2) mustBe Strings.radio2
    }

    "show correct continue button" in {
      elementText(Selectors.continue) mustBe Strings.continue
    }
  }
}

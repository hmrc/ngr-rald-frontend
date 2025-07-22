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
import play.api.data.Form
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.Radios
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.TypeOfLeaseRenewalForm
import uk.gov.hmrc.ngrraldfrontend.models.{NGRRadio, NGRRadioButtons, NGRRadioName}
import uk.gov.hmrc.ngrraldfrontend.views.html.TypeOfLeaseRenewalView

class TypeOfLeaseRenewalViewSpec extends ViewBaseSpec {
  lazy val view: TypeOfLeaseRenewalView = inject[TypeOfLeaseRenewalView]
  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"


  val heading = "What type of lease renewal is it?"
  val title = s"$heading - GOV.UK"
  val radio1 = "A renewed agreement"
  val radio2 = "A surrender and renewal"

  private val firstButton: NGRRadioButtons = NGRRadioButtons("typeOfLeaseRenewal.option1", TypeOfLeaseRenewalForm.RenewedAgreement)
  private val secondButton: NGRRadioButtons = NGRRadioButtons("typeOfLeaseRenewal.option2", TypeOfLeaseRenewalForm.SurrenderAndRenewal)
  private val ngrRadio: NGRRadio = NGRRadio(NGRRadioName("connection-to-property-radio"), Seq(firstButton, secondButton))
  val form: Form[TypeOfLeaseRenewalForm] = TypeOfLeaseRenewalForm.form.fillAndValidate(TypeOfLeaseRenewalForm.RenewedAgreement)

  val radio: Radios = buildRadios(form, ngrRadio)

  object Selectors {
    val navTitle = "head > title"
    val heading = "#main-content > div > div > form > div > div > h1"
    val addressCaption = "#main-content > div > div > form > div > div > span"
    val radio1 = "#main-content > div > div > form > div > div > div > div > div:nth-child(1) > label"
    val radio2 = "#main-content > div > div > form > div > div > div > div > div:nth-child(2) > label"
  }

  "TypeOfLeaseRenewalView" must {
    val leaseRenewalView = view(form, content, radio, address)
    lazy implicit val document: Document = Jsoup.parse(leaseRenewalView.body)
    val htmlApply = view.apply(form, content, radio, address).body
    val htmlRender = view.render(form, content, radio, address, request, messages, mockConfig).body
    lazy val htmlF = view.f(form, content, radio, address)

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

    "show the correct radio button for RenewedAgreement" in {
      elementText(Selectors.radio1) mustBe radio1
    }

    "show the correct radio button for SurrenderAndRenewal" in {
      elementText(Selectors.radio2) mustBe radio2
    }

  }
}


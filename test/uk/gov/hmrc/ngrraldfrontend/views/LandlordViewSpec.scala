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
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{ErrorMessage, Label, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.Radios
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.components.{BusinessPartnerOrSharedDirector, CompanyPensionFund, FamilyMember, LandLordAndTenant, LeaseOrTenancy, NGRCharacterCount, NGRRadio, NGRRadioButtons, NGRRadioName, OtherRelationship}
import uk.gov.hmrc.ngrraldfrontend.models.forms.LandlordForm
import uk.gov.hmrc.ngrraldfrontend.views.html.LandlordView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.NGRCharacterCountComponent

class LandlordViewSpec extends ViewBaseSpec {
  lazy val view: LandlordView = inject[LandlordView]
  lazy val ngrCharacterCountComponent: NGRCharacterCountComponent = inject[NGRCharacterCountComponent]

  object Strings {
    val heading = "Landlord"
    val textInputLabel = "What is the landlords full name?"
    val radioLabel = "What is your relationship with the landlord?"
    val radio1 = "Landlord and tenant relationship only"
    val radio2 = "Family member"
    val radio3 = "Company pension fund"
    val radio4 = "Business partner or shared director"
    val radio5 = "Other relationship"
    val continue = "Continue"


    object Selectors {
      val heading = "#main-content > div > div > form > h1"
      val textInputLabel = "#main-content > div > div > form > div:nth-child(4) > h1 > label"
      val radioLabel = "#main-content > div > div > form > div:nth-child(5) > fieldset > legend > h1"
      val radio1 = "#main-content > div > div > form > div:nth-child(5) > fieldset > div > div:nth-child(1) > label"
      val radio2 = "#main-content > div > div > form > div:nth-child(5) > fieldset > div > div:nth-child(2) > label"
      val radio3 = "#main-content > div > div > form > div:nth-child(5) > fieldset > div > div:nth-child(3) > label"
      val radio4 = "#main-content > div > div > form > div:nth-child(5) > fieldset > div > div:nth-child(4) > label"
      val radio5 = "#main-content > div > div > form > div:nth-child(5) > fieldset > div > div:nth-child(5) > label"
      val continue = "#continue"
    }

    val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"
    private val landLordAndTennant: NGRRadioButtons = NGRRadioButtons("Landlord and tenant relationship only", LandLordAndTenant)
    private val familyMember: NGRRadioButtons = NGRRadioButtons("Family member", FamilyMember)
    private val companyPensionFund: NGRRadioButtons = NGRRadioButtons("Company pension fund", CompanyPensionFund)
    private val businessPartnerOrSharedDirector: NGRRadioButtons = NGRRadioButtons("Company pension fund", BusinessPartnerOrSharedDirector)

    private def otherRelationship(form: Form[LandlordForm])(implicit messages: Messages): NGRRadioButtons = NGRRadioButtons(
      radioContent = "landlord.radio5",
      radioValue = OtherRelationship,
      conditionalHtml = Some(ngrCharacterCountComponent(form,
        NGRCharacterCount(
          id = "landlord-radio-other",
          name = "landlord-radio-other",
          label = Label(
            classes = "govuk-label govuk-label--m",
            content = Text(Messages("landlord.radio5.dropdown"))
          ),
          errorMessage = Some(ErrorMessage(
            id = Some("radio-other-error"),
            content = Text(Messages("landlord.radio.other.empty.error"))
          ))
        ))
      )
    )

    private val ngrRadio: NGRRadio = NGRRadio(NGRRadioName("what-type-of-agreement-radio"), Seq(landLordAndTennant, familyMember, companyPensionFund, businessPartnerOrSharedDirector, otherRelationship(form)))
    val form = LandlordForm.form.fillAndValidate(LandlordForm(landlordName = "Bob", landLordType = "FamilyMember", None))
    val radio: Radios = buildRadios(form, ngrRadio)

    "TellUsAboutYourNewAgreementView" must {
      val landlordView = view(content, address, form, radio)
      lazy implicit val document: Document = Jsoup.parse(landlordView.body)
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

      "show correct text input label" in {
        elementText(Selectors.textInputLabel) mustBe Strings.textInputLabel
      }

      "show correct radio label" in {
        elementText(Selectors.radioLabel) mustBe Strings.radioLabel
      }

      "show correct radio 1" in {
        elementText(Selectors.radio1) mustBe Strings.radio1
      }

      "show correct radio 2" in {
        elementText(Selectors.radio2) mustBe Strings.radio2
      }

      "show correct radio 3" in {
        elementText(Selectors.radio3) mustBe Strings.radio3
      }

      "show correct radio 4" in {
        elementText(Selectors.radio4) mustBe Strings.radio4
      }

      "show correct radio 5" in {
        elementText(Selectors.radio5) mustBe Strings.radio5
      }

      "show correct continue button" in {
        elementText(Selectors.continue) mustBe Strings.continue
      }
    }
  }
}


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
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models._
import uk.gov.hmrc.ngrraldfrontend.views.html.TellUsAboutYourAgreementView

class TellUsAboutYourAgreementViewSpec extends ViewBaseSpec {
  lazy val view: TellUsAboutYourAgreementView = inject[TellUsAboutYourAgreementView]

  object Strings {
    val headingNewAgreement = "Tell us about your new agreement"
    val p1NewAgreement = "You need to tell us within 60 days if you have a new agreement."
    val headingRenewedAgreement = "Tell us about your renewed agreement"
    val p1RenewedAgreement = "You need to tell us within 60 days if you have a renewed your agreement."
    val headingRentAgreement = "Tell us about your rent review"
    val p1RentAgreement = "You need to tell us within 60 days if you have completed a rent review."
    val subheading = "Information you need"
    val p2 = "It will help to have a copy of your agreement to refer to."
    val rentp2 = "You should also have a rent review memorandum that sets out the new terms that you agreed with your landlord or their agent."
    val p3 = "Depending on what you tell us about, you need to know things like:"
    val bulletPoint1 = "who your landlord is"
    val bulletPoint2 = "when you made the agreement"
    val rentBulletPoint2 = "when you made the change to your agreement"
    val bulletPoint3 = "how much your rent is"
    val bulletPoint4 = "what your rent includes"
    val bulletPoint5 = "if you have done any alterations and improvements"
    val rentBulletPoint5 = "if you have a break clause"
    val subheading2 = "Supporting documents"
    val p4 = "You do not need to upload any documents to support what you tell us."
    val continue = "Continue"
  }

  object Selectors {
    val heading = "#main-content > div > div > form > div > div > h1.govuk-heading-l"
    val p1 = "#main-content > div > div > form > div > div > p:nth-child(3)"
    val subheading = "#main-content > div > div > form > div > div > h1:nth-child(4)"
    val p2 = "#main-content > div > div > form > div > div > p:nth-child(5)"
    val rentp2 = "#main-content > div > div > form > div > div > p:nth-child(6)"
    val p3 = "#main-content > div > div > form > div > div > p:nth-child(6)"
    val rentp3 = "#main-content > div > div > form > div > div > p:nth-child(7)"
    val bulletPoint1 = "#main-content > div > div > form > div > div > ul > li:nth-child(1)"
    val bulletPoint2 = "#main-content > div > div > form > div > div > ul > li:nth-child(2)"
    val bulletPoint3 = "#main-content > div > div > form > div > div > ul > li:nth-child(3)"
    val bulletPoint4 = "#main-content > div > div > form > div > div > ul > li:nth-child(4)"
    val bulletPoint5 = "#main-content > div > div > form > div > div > ul > li:nth-child(5)"
    val subheading2 = "#main-content > div > div > form > div > div > h1:nth-child(8)"
    val rentSubheading2 = "#main-content > div > div > form > div > div > h1:nth-child(9)"
    val p4 = "#main-content > div > div > form > div > div > p:nth-child(9)"
    val rentp4 = "#main-content > div > div > form > div > div > p:nth-child(10)"
    val continue = "#continue"
  }

  val address = "5 Brixham Marina, Berry Head Road, Brixham, Devon, TQ5 9BW"

  "TellUsAboutYourNewAgreementView" must {
    val tellUsAboutYourNewAgreementView = view(content, address, AgreementType.NewAgreement)
    lazy implicit val document: Document = Jsoup.parse(tellUsAboutYourNewAgreementView.body)
    val htmlApply = view.apply(content, address, AgreementType.NewAgreement).body
    val htmlRender = view.render(content, address, AgreementType.NewAgreement, request, messages, mockConfig).body
    lazy val htmlF = view.f(content, address, AgreementType.NewAgreement)

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
      elementText(Selectors.heading) mustBe Strings.headingNewAgreement
    }

    "show correct p1" in {
      elementText(Selectors.p1) mustBe Strings.p1NewAgreement
    }

    "show correct subheading" in {
      elementText(Selectors.subheading) mustBe Strings.subheading
    }

    "show correct p2" in {
      elementText(Selectors.p2) mustBe Strings.p2
    }

    "show correct p3" in {
      elementText(Selectors.p3) mustBe Strings.p3
    }

    "show correct bulletPoint1" in {
      elementText(Selectors.bulletPoint1) mustBe Strings.bulletPoint1
    }

    "show correct bulletPoint2" in {
      elementText(Selectors.bulletPoint2) mustBe Strings.bulletPoint2
    }

    "show correct bulletPoint3" in {
      elementText(Selectors.bulletPoint3) mustBe Strings.bulletPoint3
    }

    "show correct bulletPoint4" in {
      elementText(Selectors.bulletPoint4) mustBe Strings.bulletPoint4
    }

    "show correct bulletPoint5" in {
      elementText(Selectors.bulletPoint5) mustBe Strings.bulletPoint5
    }

    "show correct subheading2" in {
      elementText(Selectors.subheading2) mustBe Strings.subheading2
    }

    "show correct p4" in {
      elementText(Selectors.p4) mustBe Strings.p4
    }

    "show correct continue button" in {
      elementText(Selectors.continue) mustBe Strings.continue
    }
  }

  "TellUsAboutYourRenewedAgreementView" must {
    val tellUsAboutYourRenewedAgreementView = view(content, address, AgreementType.RenewedAgreement)
    lazy implicit val document: Document = Jsoup.parse(tellUsAboutYourRenewedAgreementView.body)
    val htmlApply = view.apply(content, address, AgreementType.RenewedAgreement).body
    val htmlRender = view.render(content, address, AgreementType.RenewedAgreement, request, messages, mockConfig).body
    lazy val htmlF = view.f(content, address, AgreementType.RenewedAgreement)

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
      elementText(Selectors.heading) mustBe Strings.headingRenewedAgreement
    }

    "show correct p1" in {
      elementText(Selectors.p1) mustBe Strings.p1RenewedAgreement
    }

    "show correct subheading" in {
      elementText(Selectors.subheading) mustBe Strings.subheading
    }

    "show correct p2" in {
      elementText(Selectors.p2) mustBe Strings.p2
    }

    "show correct p3" in {
      elementText(Selectors.p3) mustBe Strings.p3
    }

    "show correct bulletPoint1" in {
      elementText(Selectors.bulletPoint1) mustBe Strings.bulletPoint1
    }

    "show correct bulletPoint2" in {
      elementText(Selectors.bulletPoint2) mustBe Strings.bulletPoint2
    }

    "show correct bulletPoint3" in {
      elementText(Selectors.bulletPoint3) mustBe Strings.bulletPoint3
    }

    "show correct bulletPoint4" in {
      elementText(Selectors.bulletPoint4) mustBe Strings.bulletPoint4
    }

    "show correct subheading2" in {
      elementText(Selectors.subheading2) mustBe Strings.subheading2
    }

    "show correct p4" in {
      elementText(Selectors.p4) mustBe Strings.p4
    }

    "show correct continue button" in {
      elementText(Selectors.continue) mustBe Strings.continue
    }
  }
  "TellUsAboutYourRentView" must {
    val tellUsAboutYourRenewedAgreementView = view(content, address, AgreementType.RentAgreement)
    lazy implicit val document: Document = Jsoup.parse(tellUsAboutYourRenewedAgreementView.body)
    val htmlApply = view.apply(content, address, AgreementType.RentAgreement).body
    val htmlRender = view.render(content, address, AgreementType.RentAgreement, request, messages, mockConfig).body
    lazy val htmlF = view.f(content, address, AgreementType.RentAgreement)

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
      elementText(Selectors.heading) mustBe Strings.headingRentAgreement
    }

    "show correct p1" in {
      elementText(Selectors.p1) mustBe Strings.p1RentAgreement
    }

    "show correct subheading" in {
      elementText(Selectors.subheading) mustBe Strings.subheading
    }

    "show correct p2" in {
      elementText(Selectors.p2) mustBe Strings.p2
    }

    "show correct p2.5" in {
      elementText(Selectors.rentp2) mustBe Strings.rentp2
    }

    "show correct p3" in {
      elementText(Selectors.rentp3) mustBe Strings.p3
    }

    "show correct bulletPoint1" in {
      elementText(Selectors.bulletPoint1) mustBe Strings.bulletPoint1
    }

    "show correct bulletPoint2" in {
      elementText(Selectors.bulletPoint2) mustBe Strings.rentBulletPoint2
    }

    "show correct bulletPoint3" in {
      elementText(Selectors.bulletPoint3) mustBe Strings.bulletPoint3
    }

    "show correct bulletPoint4" in {
      elementText(Selectors.bulletPoint4) mustBe Strings.bulletPoint4
    }

    "show correct bulletPoint5" in {
      elementText(Selectors.bulletPoint5) mustBe Strings.rentBulletPoint5
    }

    "show correct subheading2" in {
      elementText(Selectors.rentSubheading2) mustBe Strings.subheading2
    }

    "show correct p4" in {
      elementText(Selectors.rentp4) mustBe Strings.p4
    }

    "show correct continue button" in {
      elementText(Selectors.continue) mustBe Strings.continue
    }
  }
}

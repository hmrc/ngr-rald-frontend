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
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.views.html.{Layout, RentReviewDetailsSentView}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Table, TableRow, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class RentReviewDetailSentViewSpec extends ViewBaseSpec {
    lazy val view: RentReviewDetailsSentView = inject[RentReviewDetailsSentView]
    lazy val table: Table = Table(Seq(
      Seq(
        TableRow(
          HtmlContent(messages("Address"))
        ),
        TableRow(
          Text(messages("123 Nice Lane"))
        ),
      ),
      Seq(
        TableRow(
          HtmlContent(messages("Property Reference"))
        ),
        TableRow(
          Text(messages("123"))
        ),
      ),
    )
    )

    object Strings {
      val title: String = "Renewed agreement details sent"
      val yourRef: String = "Your reference is 1234"
      val print: String = "Print this page"
      val emailText: String = "We have sent a confirmation email to test@testUser.com"
      val whatNext: String = "What happens next?"
      val goBack: String = "Go to your account home"
    }

    val reference: String = "1234"
    val email: String = "test@testUser.com"

    object Selectors {
      val title = "#main-content > div > div.govuk-grid-column-two-thirds > form > div > h1"
      val yourRef = "#main-content > div > div.govuk-grid-column-two-thirds > form > div > div"
      val print = "#printPage > a"
      val emailText = "#main-content > div > div.govuk-grid-column-two-thirds > form > p:nth-child(4)"
      val whatNext = "#main-content > div > div.govuk-grid-column-two-thirds > form > h2"
      val goBack = "#main-content > div > div.govuk-grid-column-two-thirds > form > p:nth-child(7)"
    }

    "AddPropertyRequestSent" must {
      val RentReviewDetailSentView = view(Some(reference), table, email)
      lazy implicit val document: Document = Jsoup.parse(RentReviewDetailSentView.body)
      val htmlApply = view.apply(Some(reference), table, email).body
      val htmlRender = view.render(Some(reference), table, email, request, messages, mockConfig).body
      lazy val htmlF = view.f(Some(reference), table, email)

      "htmlF is not empty" in {
        htmlF.toString() must not be empty
      }

      "apply must be the same as render" in {
        htmlApply mustBe htmlRender
      }

      "render is not empty" in {
        htmlRender must not be empty
      }

      "display correct content" in {
        elementText(Selectors.title) mustBe Strings.title
        elementText(Selectors.yourRef) must include(Strings.yourRef)
        elementText(Selectors.print) mustBe Strings.print
        elementText(Selectors.emailText) mustBe Strings.emailText
        elementText(Selectors.whatNext) mustBe Strings.whatNext
        elementText(Selectors.goBack) mustBe Strings.goBack
      }
    }
  }
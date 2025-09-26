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
import uk.gov.hmrc.ngrraldfrontend.pages.Page

class TestPage extends Page {
  override def toString: String = "TestPage"
}

class PageSpec extends AnyFlatSpec with Matchers {
  "Page.toString" should "implicitly convert Page to String using toString" in {
    val page: Page = new TestPage
    val str: String = page // implicit conversion
    str shouldEqual "TestPage"
  }
}

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

package uk.gov.hmrc.ngrraldfrontend.pages

import play.api.libs.json.*

import scala.language.implicitConversions

trait Page

object Page {
  
  implicit val pageReads: Reads[Page] =
    Reads.StringReads.map(str => new Page {
      override def toString = str
    })

  implicit val pageWrites: Writes[Page] =
    Writes(page => JsString(page.toString))

  implicit val pageFormat: Format[Page] = Format(pageReads, pageWrites)


  implicit def toString(page: Page): String =
    page.toString
}

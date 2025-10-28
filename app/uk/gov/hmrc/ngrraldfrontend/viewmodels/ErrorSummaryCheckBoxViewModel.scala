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

package uk.gov.hmrc.ngrraldfrontend.viewmodels

import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.errorsummary.{ErrorLink, ErrorSummary}
import uk.gov.hmrc.ngrraldfrontend.utils.DateKeyFinder

trait ErrorSummaryCheckBoxViewModel {

  object ErrorSummaryCheckBoxViewModel {
    def apply(
               form: Form[_],
               errorLinkOverrides: Map[String, String] = Map.empty
             )(implicit messages: Messages): ErrorSummary = {

      val errors = form.errors.map {
        error =>
          ErrorLink(
            href    = deriveHref(error, form, errorLinkOverrides),
            content = HtmlContent(messages(error.message, error.args: _*))
          )
      }

      ErrorSummary(
        errorList = errors,
        title     = Text(messages("error.summary.title"))
      )
    }

    private def deriveHref(error: FormError, form: Form[_], errorLinkOverrides: Map[String, String]): Option[String] = {
      val messages = error.messages
      val key = error.key
      if(messages.nonEmpty){
        Some(s"#Incentive")
      }else {
        Some(s"#${errorLinkOverrides.getOrElse(key, key)}")
      }
    }
  }
}

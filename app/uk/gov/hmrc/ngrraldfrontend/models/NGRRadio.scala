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

import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.*
import uk.gov.hmrc.govukfrontend.views.html.components.{Hint, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.errormessage.ErrorMessage
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.Radios

case class NGRRadioName(key: String)

case class NGRRadioButtons(radioContent: String, radioValue: RadioEntry, buttonHint: Option[String] = None, conditionalHtml: Option[Html] = None)

case class NGRRadioHeader(title: String, classes: String, isPageHeading: Boolean)

case class NGRRadio(radioGroupName: NGRRadioName, NGRRadioButtons: Seq[NGRRadioButtons], ngrTitle: Option[NGRRadioHeader] = None, hint: Option[String] = None)

object NGRRadio {

  def buildRadios[A](
                      form: Form[A],
                      NGRRadios: NGRRadio
                    )(implicit messages: Messages): Radios = {
    Radios(
      fieldset = NGRRadios.ngrTitle.map(header =>
        Fieldset(
          legend = Some(Legend(
            content = Text(Messages(header.title)),
            classes = header.classes,
            isPageHeading = header.isPageHeading
          ))
        )),
      hint = NGRRadios.hint.map { hint =>
        Hint(content = Text(Messages(hint)))
      },
      idPrefix = Some(NGRRadios.radioGroupName.key),
      name = NGRRadios.radioGroupName.key,
      items = NGRRadios.NGRRadioButtons.map { item =>
        RadioItem(
          content = Text(Messages(item.radioContent)),
          value = Some(item.radioValue.toString),
          hint = Some(Hint(content = Text(Messages(item.buttonHint.getOrElse(""))))),
          checked = form.data.values.toList.contains(item.radioValue.toString),
          conditionalHtml = item.conditionalHtml
        )
      },
      classes = "govuk-radios",
      errorMessage = form(NGRRadios.radioGroupName.key).error.map(err => ErrorMessage(content = Text(messages(err.message)))),

    )
  }
}
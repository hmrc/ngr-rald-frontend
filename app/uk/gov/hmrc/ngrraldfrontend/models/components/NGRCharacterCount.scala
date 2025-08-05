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

package uk.gov.hmrc.ngrraldfrontend.models.components

import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Text, Value}
import uk.gov.hmrc.govukfrontend.views.html.components.GovukCharacterCount
import uk.gov.hmrc.govukfrontend.views.viewmodels.FormGroup
import uk.gov.hmrc.govukfrontend.views.viewmodels.charactercount.CharacterCount
import uk.gov.hmrc.govukfrontend.views.viewmodels.errormessage.ErrorMessage
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.label.Label
import uk.gov.hmrc.ngrraldfrontend.models.forms.LandlordForm
import uk.gov.hmrc.ngrraldfrontend.views.html.components.NGRCharacterCountComponent

import javax.inject.Inject

case class NGRCharacterCount(
                              id: String = "",
                              name: String = "",
                              maxWords: Option[Int] = None,
                              maxLength: Option[Int] = None,
                              label: Label = Label(),
                              hint: Option[Hint] = None,
                              errorMessage: Option[ErrorMessage] = None,
                         )
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
import uk.gov.hmrc.govukfrontend.views.Aliases.*
import uk.gov.hmrc.govukfrontend.views.html.components.{Hint, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.errormessage.ErrorMessage
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.Radios


trait RadioEntry

sealed trait YesNoItem extends RadioEntry
case object `true` extends YesNoItem
case object `false` extends YesNoItem

sealed trait TypeOfAgreement extends RadioEntry
case object LeaseOrTenancy extends TypeOfAgreement
case object Written extends TypeOfAgreement
case object Verbal extends TypeOfAgreement

sealed trait RentBasedOn extends RadioEntry
case object OpenMarket extends RentBasedOn
case object PercentageOpenMarket extends RentBasedOn
case object Turnover extends RentBasedOn
case object PercentageTurnover extends RentBasedOn
case object TotalOccupancyCost extends RentBasedOn
case object Indexation extends RentBasedOn
case object Other extends RentBasedOn

sealed trait RepairsAndInsurance extends RadioEntry
case object You extends RepairsAndInsurance
case object Landlord extends RepairsAndInsurance
case object YouAndLandlord extends RepairsAndInsurance

case class NGRRadioName(key: String)

case class NGRRadioButtons(radioContent: String,
                           radioValue: RadioEntry,
                           buttonHint: Option[String] = None,
                           conditionalCharacterCount: Option[CharacterCount] = None,
                           conditionalHtml: Option[Html] = None) {

}

case class NGRRadio(radioGroupName: NGRRadioName,
                    NGRRadioButtons: Seq[NGRRadioButtons],
                    ngrTitle: Option[Legend] = None,
                    hint: Option[String] = None)

object NGRRadio {

  def yesButton(radioContent: String = "service.yes", conditionalHtml: Option[Html] = None): NGRRadioButtons =
    NGRRadioButtons(radioContent = radioContent, radioValue = `true`, conditionalHtml = conditionalHtml)

  def noButton(radioContent: String = "service.no", conditionalHtml: Option[Html] = None): NGRRadioButtons =
    NGRRadioButtons(radioContent = radioContent, radioValue = `false`, conditionalHtml = conditionalHtml)

  def simpleNgrRadio(radioName: String, hint: Option[String] = None)(implicit messages: Messages): NGRRadio =
    NGRRadio(
      radioGroupName = NGRRadioName(radioName),
      NGRRadioButtons = Seq(NGRRadio.yesButton(), NGRRadio.noButton()),
      hint = hint
    )  

  def ngrRadio(radioName: String, radioButtons: Seq[NGRRadioButtons], ngrTitle: String,
               ngrTitleClass: String = "govuk-fieldset__legend--m", hint: Option[String] = None)(implicit messages: Messages): NGRRadio =
    NGRRadio(
      radioGroupName = NGRRadioName(radioName),
      NGRRadioButtons = radioButtons,
      ngrTitle = Some(Legend(content = Text(messages(ngrTitle)), classes = ngrTitleClass, isPageHeading = true)),
      hint = hint
    )  

  def buildRadios[A](
                      form: Form[A],
                      NGRRadios: NGRRadio
                    )(implicit messages: Messages): Radios = {
    Radios(
      fieldset = NGRRadios.ngrTitle.map(header => Fieldset(legend = NGRRadios.ngrTitle)),
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
          checked = form.data.get(NGRRadios.radioGroupName.key).exists(value => value.equals(item.radioValue.toString)),
          conditionalHtml = item.conditionalHtml
        )
      },
      classes = "govuk-radios",
      errorMessage = form(NGRRadios.radioGroupName.key).error.map(err => ErrorMessage(content = Text(messages(err.message)))),
    )
  }
}
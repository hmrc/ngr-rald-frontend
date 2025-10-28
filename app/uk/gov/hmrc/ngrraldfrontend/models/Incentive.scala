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


import models.Enumerable
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.{CheckboxItem, ExclusiveCheckbox}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.ngrraldfrontend.viewmodels.checkbox.CheckboxItemViewModel

sealed trait Incentive

object Incentive extends Enumerable.Implicits {

  case object YesLumpSum extends  WithName("yesLumpSum") with  Incentive
  case object YesRentFreePeriod extends WithName("yesRentFreePeriod") with Incentive
  case object No extends WithName("no") with Incentive
  case object Divider extends Incentive

  val values: Seq[Incentive] = Seq(
    YesLumpSum,
    YesRentFreePeriod,
    Divider,
    No
  )

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] = {
    values.zipWithIndex.map {
      case (value, index) =>
        value match {
          case YesLumpSum => CheckboxItemViewModel(
            content = Text(messages(s"didYouGetIncentiveForNotTriggeringBreakClause.checkbox")),
            fieldId = s"incentive",
            index = index,
            value = value.toString,
          )
          case YesRentFreePeriod => CheckboxItemViewModel(
            content = Text(messages(s"didYouGetIncentiveForNotTriggeringBreakClause.checkbox1")),
            fieldId = s"incentive",
            index = index,
            value = value.toString,
          )
          case Divider => CheckboxItemViewModel(
            fieldId = s"incentive",
            index = index,
            value = value.toString,
            divider = messages("service.or")
          )
          case No => CheckboxItemViewModel(
            content = Text(messages(s"didYouGetIncentiveForNotTriggeringBreakClause.checkbox2")),
            fieldId = s"incentive",
            index = index,
            value = value.toString,
            behaviour = Some(ExclusiveCheckbox)
          )
        }
    }
  }
  implicit val enumerable: Enumerable[Incentive] =
    Enumerable(values.map(v => v.toString -> v) *)
}

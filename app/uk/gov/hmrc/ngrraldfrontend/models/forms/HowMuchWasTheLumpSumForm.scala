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

package uk.gov.hmrc.ngrraldfrontend.models.forms

import play.api.data.*
import play.api.data.Forms.*
import play.api.data.format.Formatter
import play.api.libs.json.{Json, OFormat}

import scala.math.BigDecimal.RoundingMode
import scala.util.Try


final case class HowMuchWasTheLumpSumForm(lumpSum: BigDecimal)

object HowMuchWasTheLumpSumForm extends CommonFormValidators {
  implicit val format: OFormat[HowMuchWasTheLumpSumForm] = Json.format[HowMuchWasTheLumpSumForm]

  private lazy val lumpSum = "how–much–was–the–lump–sum-value"
  private lazy val lumpSumEmptyError  = "howMuchWasTheLumpSum.empty.error"
  private lazy val lumpSumMaxError    = "howMuchWasTheLumpSum.tooLarge.error"
  private lazy val lumpSumFormatError = "howMuchWasTheLumpSum.format.error"


  def unapply(howMuchWasTheLumpSumForm: HowMuchWasTheLumpSumForm): Option[BigDecimal] = Some(howMuchWasTheLumpSumForm.lumpSum)

  val form: Form[HowMuchWasTheLumpSumForm] = Form(
    mapping(
      lumpSum -> text()
        .transform[String](_.strip().replaceAll("[£|,|\\s]", ""), identity)
        .verifying(
          firstError(
            isNotEmpty(lumpSum, lumpSumEmptyError),
            regexp(amountRegex.pattern(),lumpSumFormatError)
          )
        )
        .transform[BigDecimal](BigDecimal(_).setScale(2, RoundingMode.HALF_UP), _.toString)
        .verifying(
          maximumValue[BigDecimal](BigDecimal("9999999.99"), lumpSumMaxError)
        )
    )(HowMuchWasTheLumpSumForm.apply)(HowMuchWasTheLumpSumForm.unapply)
  )
  
}


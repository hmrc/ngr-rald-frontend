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

import play.api.data.Form
import play.api.data.Forms.{mapping, optional, text}
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.ngrraldfrontend.models.forms.AgreementVerbalForm.firstError
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings

final case class HowManyParkingSpacesOrGaragesIncludedInRentForm(
                                                                  uncoveredSpaces: Int,
                                                                  coveredSpaces: Int,
                                                                  garages: Int,
                                                                )

object HowManyParkingSpacesOrGaragesIncludedInRentForm extends CommonFormValidators with Mappings {
  implicit val format: OFormat[HowManyParkingSpacesOrGaragesIncludedInRentForm] = Json.format[HowManyParkingSpacesOrGaragesIncludedInRentForm]

  private lazy val fieldRequired = "howManyParkingSpacesOrGaragesIncludedInRent.error.required"
  private lazy val uncoveredSpacesWholeNumError  = "howManyParkingSpacesOrGaragesIncludedInRent.uncoveredSpaces.wholeNum.error"
  private lazy val coveredSpacesWholeNumError  = "howManyParkingSpacesOrGaragesIncludedInRent.coveredSpaces.wholeNum.error"
  private lazy val garagesWholeNumError  = "howManyParkingSpacesOrGaragesIncludedInRent.garages.wholeNum.error"
  private lazy val uncoveredSpacesTooHighError  = "howManyParkingSpacesOrGaragesIncludedInRent.uncoveredSpaces.tooHigh.error"
  private lazy val coveredSpacesTooHighError  = "howManyParkingSpacesOrGaragesIncludedInRent.coveredSpaces.tooHigh.error"
  private lazy val garagesTooHighError  = "howManyParkingSpacesOrGaragesIncludedInRent.garages.tooHigh.error"
  private lazy val allFieldsRequiredError  = "howManyParkingSpacesOrGaragesIncludedInRent.allFields.error.required"
  private val maxValue = 9999

  def unapply(howManyParkingSpacesOrGaragesIncludedInRentForm: HowManyParkingSpacesOrGaragesIncludedInRentForm): Option[(Int, Int, Int)] =
    Some(
      howManyParkingSpacesOrGaragesIncludedInRentForm.uncoveredSpaces,
      howManyParkingSpacesOrGaragesIncludedInRentForm.coveredSpaces,
      howManyParkingSpacesOrGaragesIncludedInRentForm.garages,
    )

  private def isParkingSpacesEmpty[A]:
  Constraint[A] =
    Constraint((input: A) => {
      val formData = input.asInstanceOf[HowManyParkingSpacesOrGaragesIncludedInRentForm]
      val totalSpaces = Seq(formData.uncoveredSpaces, formData.coveredSpaces, formData.garages).sum
      if (totalSpaces > 0)
        Valid
      else
        Invalid(fieldRequired)
    })

  val form: Form[HowManyParkingSpacesOrGaragesIncludedInRentForm] = {
    Form(
      mapping(
        "uncoveredSpaces" -> optional(
          text()
            .transform[String](_.strip().replaceAll(",", ""), identity)
            .verifying(regexp(wholePositiveNumberRegexp.pattern(), uncoveredSpacesWholeNumError))
        ).transform[Int](_.map(_.toInt).getOrElse(0), value => Some(value.toString))
          .verifying(maximumValue[Int](maxValue, uncoveredSpacesTooHighError)),
        "coveredSpaces" -> optional(
          text()
            .transform[String](_.strip().replaceAll(",", ""), identity)
            .verifying(regexp(wholePositiveNumberRegexp.pattern(), coveredSpacesWholeNumError))
        ).transform[Int](_.map(_.toInt).getOrElse(0), value => Some(value.toString))
          .verifying(maximumValue[Int](maxValue, coveredSpacesTooHighError)),
        "garages" -> optional(
          text()
            .transform[String](_.strip().replaceAll(",", ""), identity)
            .verifying(regexp(wholePositiveNumberRegexp.pattern(), garagesWholeNumError))
        ).transform[Int](_.map(_.toInt).getOrElse(0), value => Some(value.toString))
          .verifying(maximumValue[Int](maxValue, garagesTooHighError)),
      )
      (HowManyParkingSpacesOrGaragesIncludedInRentForm.apply)(HowManyParkingSpacesOrGaragesIncludedInRentForm.unapply)
        .verifying(
          firstError(
            isParkingSpacesEmpty
          )
        )
    )
  }
}
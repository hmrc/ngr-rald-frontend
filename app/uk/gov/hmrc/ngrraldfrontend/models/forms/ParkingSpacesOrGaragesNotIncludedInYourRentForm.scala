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
import uk.gov.hmrc.ngrraldfrontend.models.{DateMappings, NGRDate}
import uk.gov.hmrc.ngrraldfrontend.models.forms.HowManyParkingSpacesOrGaragesIncludedInRentForm.{coveredSpacesTooHighError, coveredSpacesWholeNumError, firstError, garagesTooHighError, garagesWholeNumError, isLargerThanInt, isParkingSpacesEmpty, maxValue, regexp, uncoveredSpacesTooHighError, uncoveredSpacesWholeNumError, wholePositiveNumberRegexp}
import uk.gov.hmrc.ngrraldfrontend.models.forms.ProvideDetailsOfFirstSecondRentPeriodForm.{SecondRentPeriodAmount, amountRegex, annualRentEmptyError, dateMapping, firstError, isDateAfter1900, isDateEmpty, isDateValid, isNotEmpty, maxAmount, maximumValue, regexp}
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings
import play.api.libs.json.{Json, OFormat}

import scala.math.BigDecimal.RoundingMode

case class ParkingSpacesOrGaragesNotIncludedInYourRentForm(
                                                            uncoveredSpaces: Int,
                                                            coveredSpaces: Int,
                                                            garages: Int,
                                                            totalCost: BigDecimal,
                                                            agreementDate: NGRDate
                                                          )

object ParkingSpacesOrGaragesNotIncludedInYourRentForm extends CommonFormValidators with Mappings with DateMappings{
  implicit val format: OFormat[ParkingSpacesOrGaragesNotIncludedInYourRentForm] = Json.format[ParkingSpacesOrGaragesNotIncludedInYourRentForm]


  private lazy val fieldRequired = "parkingSpacesOrGaragesNotIncludedInYourRent.error.required"
  private lazy val uncoveredSpacesWholeNumError = "parkingSpacesOrGaragesNotIncludedInYourRent.uncoveredSpaces.wholeNum.error"
  private lazy val coveredSpacesWholeNumError = "parkingSpacesOrGaragesNotIncludedInYourRent.coveredSpaces.wholeNum.error"
  private lazy val garagesWholeNumError = "parkingSpacesOrGaragesNotIncludedInYourRent.garages.wholeNum.error"
  private lazy val uncoveredSpacesTooHighError = "parkingSpacesOrGaragesNotIncludedInYourRent.uncoveredSpaces.tooHigh.error"
  private lazy val coveredSpacesTooHighError = "parkingSpacesOrGaragesNotIncludedInYourRent.coveredSpaces.tooHigh.error"
  private lazy val garagesTooHighError = "parkingSpacesOrGaragesNotIncludedInYourRent.garages.tooHigh.error"
  private val maxValue = 9999

  private val maxTotalCostAmount: BigDecimal = BigDecimal("9999999.99")

  def unapply(parkingSpacesOrGaragesNotIncludedInYourRentForm: ParkingSpacesOrGaragesNotIncludedInYourRentForm): Option[(Int, Int, Int, BigDecimal, NGRDate)] =
    Some(
      parkingSpacesOrGaragesNotIncludedInYourRentForm.uncoveredSpaces,
      parkingSpacesOrGaragesNotIncludedInYourRentForm.coveredSpaces,
      parkingSpacesOrGaragesNotIncludedInYourRentForm.garages,
      parkingSpacesOrGaragesNotIncludedInYourRentForm.totalCost,
      parkingSpacesOrGaragesNotIncludedInYourRentForm.agreementDate,
    )

  private def isParkingSpacesEmpty[A]:
  Constraint[A] =
    Constraint((input: A) => {
      val formData = input.asInstanceOf[ParkingSpacesOrGaragesNotIncludedInYourRentForm]
      (formData.uncoveredSpaces, formData.coveredSpaces, formData.garages) match {
        case (uncoveredSpaces, coveredSpaces, garages) if uncoveredSpaces + coveredSpaces + garages <= 0 => Invalid(fieldRequired)
        case (_, _, _) => Valid
      }
    })

  def form: Form[ParkingSpacesOrGaragesNotIncludedInYourRentForm] = {
      Form(
        mapping(
          "uncoveredSpaces" -> optional(
            text()
              .transform[String](_.strip().replaceAll(",", ""), identity)
              .verifying(
                firstError(
                  regexp(wholePositiveNumberRegexp.pattern(), uncoveredSpacesWholeNumError),
                  isLargerThanInt(maxValue, uncoveredSpacesTooHighError)
                )
              )
          ).transform[Int](_.map(_.toInt).getOrElse(0), value => Some(value.toString)),
          "coveredSpaces" -> optional(
            text()
              .transform[String](_.strip().replaceAll(",", ""), identity)
              .verifying(
                firstError(
                  regexp(wholePositiveNumberRegexp.pattern(), coveredSpacesWholeNumError),
                  isLargerThanInt(maxValue, coveredSpacesTooHighError)
                )
              )
          ).transform[Int](_.map(_.toInt).getOrElse(0), value => Some(value.toString)),
          "garages" -> optional(
            text()
              .transform[String](_.strip().replaceAll(",", ""), identity)
              .verifying(
                firstError(
                  regexp(wholePositiveNumberRegexp.pattern(), garagesWholeNumError),
                  isLargerThanInt(maxValue, garagesTooHighError)
                )
              )
          ).transform[Int](_.map(_.toInt).getOrElse(0), value => Some(value.toString)),
          "totalCost" -> text()
            .transform[String](_.strip().replaceAll("[£|,|\\s]", ""), identity)
            .verifying(
              firstError(
                isNotEmpty("totalCost", "parkingSpacesOrGaragesNotIncludedInYourRent.totalCost.required.error"),
                regexp(amountRegex.pattern(), "parkingSpacesOrGaragesNotIncludedInYourRent.totalCost.invalid.error")
              )
            ).transform[BigDecimal](BigDecimal(_).setScale(2, RoundingMode.HALF_UP), _.toString)
            .verifying(
              minimumValue[BigDecimal](1, "parkingSpacesOrGaragesNotIncludedInYourRent.totalCost.minimum.error"),
              maximumValue[BigDecimal](maxTotalCostAmount, "parkingSpacesOrGaragesNotIncludedInYourRent.totalCost.max.error")
            ),
          "agreementDate" -> dateMapping
          .verifying(
            firstError(
              isDateEmpty(errorKeys("parkingSpacesOrGaragesNotIncludedInYourRent", "agreementDate")),
              isDateValid("parkingSpacesOrGaragesNotIncludedInYourRent.agreementDate.invalid.error"),
              isDateAfter1900("parkingSpacesOrGaragesNotIncludedInYourRent.agreementDate.before.1900.error")
            )
          ),
        )(ParkingSpacesOrGaragesNotIncludedInYourRentForm.apply)(ParkingSpacesOrGaragesNotIncludedInYourRentForm.unapply)      .verifying(
          firstError(
            isParkingSpacesEmpty
          )
        )
      )
  }
}

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

import play.api.data.Forms.{mapping, of, text}
import play.api.data.format.Formatter
import play.api.data.validation.Constraint
import play.api.data.{Form, FormError, Forms}
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.forms.HowManyParkingSpacesOrGaragesIncludedInRentForm.wholePositiveNumberRegexp
import uk.gov.hmrc.ngrraldfrontend.models.forms.ProvideDetailsOfFirstSecondRentPeriodForm.{amountRegex, dateMapping, firstError, isDateAfter1900, isDateEmpty, isDateValid, isNotEmpty, maximumValue, regexp}
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings

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

  private def parkingFormatter(args: Seq[String] = Seq.empty): Formatter[Int] = new Formatter[Int] {
  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Int] = {
    (data.get("uncoveredSpaces").getOrElse("0"), data.get("coveredSpaces").getOrElse("0"), data.get("garages").getOrElse("0")) match {
      case (uncoveredSpaces,coveredSpaces,garages) if((uncoveredSpaces.isEmpty || uncoveredSpaces == "0") && (coveredSpaces.isEmpty || coveredSpaces == "0")  && (garages.isEmpty || garages == "0")) =>
        Left(Seq(FormError(key, fieldRequired, args)))
      case (uncoveredSpaces, coveredSpaces, garages) =>
        data.get(key) match {
          case Some(value) if value.toDoubleOption.getOrElse(0d) > maxValue.toDouble =>
            Left(Seq(FormError(key, s"parkingSpacesOrGaragesNotIncludedInYourRent.${key}.tooHigh.error", args)))

          case Some(value) if value.nonEmpty && !value.replaceAll(",", "").matches(wholePositiveNumberRegexp.pattern()) =>
            Left(Seq(FormError(key, s"parkingSpacesOrGaragesNotIncludedInYourRent.${key}.wholeNum.error", args)))

          case value =>  Right(value.flatMap(value => value.trim.replaceAll(",", "").toIntOption).getOrElse(0))
        }
      case _ =>
        Left(Seq(FormError(key, fieldRequired, args)))
    }
  }

  override def unbind(key: String, value: Int): Map[String, String] = Map(key -> value.toString)
}

  def form: Form[ParkingSpacesOrGaragesNotIncludedInYourRentForm] = {
      Form(
        mapping(
          "uncoveredSpaces" -> of(parkingFormatter()),
          "coveredSpaces" -> of(parkingFormatter()),
          "garages" -> of(parkingFormatter()),
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
        )(ParkingSpacesOrGaragesNotIncludedInYourRentForm.apply)(ParkingSpacesOrGaragesNotIncludedInYourRentForm.unapply)
      )
  }
}

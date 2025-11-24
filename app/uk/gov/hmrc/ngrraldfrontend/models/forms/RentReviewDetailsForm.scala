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
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.govukfrontend.views.html.components.GovukRadios
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.{buildRadios, ngrRadio, noButton, yesButton}
import uk.gov.hmrc.ngrraldfrontend.models.forms.mappings.Mappings
import uk.gov.hmrc.ngrraldfrontend.models.{DateMappings, NGRDate, RentReviewDetails, errorKeys}

import scala.math.BigDecimal.RoundingMode

case class RentReviewDetailsForm(annualRentAmount: BigDecimal, whatHappensAtRentReview: String, startDate: NGRDate,
                                 hasAgreedNewRent: String, whoAgreed: Option[String])

object RentReviewDetailsForm extends CommonFormValidators with Mappings with DateMappings {

  implicit val format: OFormat[RentReviewDetailsForm] = Json.format[RentReviewDetailsForm]

  val annualAmount = "annualAmount"
  val whatHappensAtRentReviewRadio = "what-happens-at-rent-review-radio"
  val hasAgreedNewRentRadio = "has-agreed-new-rent-radio"
  val whoAgreedRadio = "who-agreed-radio"
  private val whoAgreedRequiredError = "rentReviewDetails.whoAgreed.required.error"

  def unapply(rentReviewDetailsForm: RentReviewDetailsForm): Option[(BigDecimal, String, NGRDate, String, Option[String])] =
    Some(rentReviewDetailsForm.annualRentAmount, rentReviewDetailsForm.whatHappensAtRentReview, rentReviewDetailsForm.startDate,
      rentReviewDetailsForm.hasAgreedNewRent, rentReviewDetailsForm.whoAgreed)

  def answerToForm(rentReviewDetails: RentReviewDetails): Form[RentReviewDetailsForm] =
    form.fill(
      RentReviewDetailsForm(
        annualRentAmount = rentReviewDetails.annualRentAmount,
        whatHappensAtRentReview = rentReviewDetails.whatHappensAtRentReview,
        startDate = NGRDate.fromString(rentReviewDetails.startDate),
        hasAgreedNewRent = rentReviewDetails.hasAgreedNewRent.toString,
        whoAgreed = rentReviewDetails.whoAgreed
      )
    )

  def formToAnswers(rentReviewDetailsForm: RentReviewDetailsForm): RentReviewDetails =
    val hasNewRent = rentReviewDetailsForm.hasAgreedNewRent.toBoolean
    RentReviewDetails(
      annualRentAmount = rentReviewDetailsForm.annualRentAmount,
      whatHappensAtRentReview = rentReviewDetailsForm.whatHappensAtRentReview,
      startDate = rentReviewDetailsForm.startDate.makeString,
      hasAgreedNewRent = hasNewRent,
      whoAgreed = if (!hasNewRent) rentReviewDetailsForm.whoAgreed else None
    )

  private def whoAgreedFormatter(args: Seq[String] = Seq.empty): Formatter[Option[String]] = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] =
      val hasAgreed = data.get(hasAgreedNewRentRadio).exists(_ == "false")
      data.get(key) match {
        case None if hasAgreed => Left(Seq(FormError(key, whoAgreedRequiredError, args)))
        case Some(s) if hasAgreed && s.isEmpty => Left(Seq(FormError(key, whoAgreedRequiredError, args)))
        case Some(s) => Right(Some(s))
        case None => Right(None)
      }

    override def unbind(key: String, value: Option[String]): Map[String, String] =
      Map(key -> value.getOrElse(""))
  }

  def form: Form[RentReviewDetailsForm] = {
    Form(
      mapping(
        annualAmount -> text()
          .transform[String](_.strip().replaceAll("[£|,|\\s]", ""), identity)
          .verifying(
            firstError(
              isNotEmpty(annualAmount, "rentReviewDetails.annualAmount.required.error"),
              regexp(amountRegex.pattern(), "rentReviewDetails.annualAmount.invalid.error")
            )
          )
          .transform[BigDecimal](BigDecimal(_).setScale(2, RoundingMode.HALF_UP), _.toString)
          .verifying(
            maximumValue[BigDecimal](BigDecimal("9999999.99"), "rentReviewDetails.annualAmount.maximum.error")
          ),
        whatHappensAtRentReviewRadio -> radioText("rentReviewDetails.whatHappensAtRentReview.required.error"),
        "startDate" -> dateMapping
          .verifying(
            firstError(
              isDateEmpty(errorKeys("rentReviewDetails", "startDate")),
              isDateValid("rentReviewDetails.startDate.invalid.error"),
              isDateAfter1900("rentReviewDetails.startDate.before.1900.error")
            )
          ),
        hasAgreedNewRentRadio -> radioText("rentReviewDetails.hasAgreedNewRent.required.error"),
        whoAgreedRadio -> of(whoAgreedFormatter())
      )(RentReviewDetailsForm.apply)(RentReviewDetailsForm.unapply)
    )
  }

  def createWhatHappensAtRentReviewRadio(implicit messages: Messages): NGRRadio =
    ngrRadio(radioName = whatHappensAtRentReviewRadio,
      radioButtons = Seq(
        NGRRadioButtons(radioContent = "rentReviewDetails.whatHappensAtRentReview.radio1.text", radioValue = GoUpOrDown),
        NGRRadioButtons(radioContent = "rentReviewDetails.whatHappensAtRentReview.radio2.text", radioValue = OnlyGoUp),
      ),
      ngrTitle = "rentReviewDetails.whatHappensAtRentReview.label",
      isPageHeading = false
    )

  def createHasAgreedNewRentRadio(form: Form[RentReviewDetailsForm], govukRadios: GovukRadios)(implicit messages: Messages): NGRRadio =
    ngrRadio(radioName = hasAgreedNewRentRadio,
      radioButtons = Seq(yesButton(), noButton(conditionalHtml = Some(govukRadios(buildRadios(form, createWhoAgreedRadio))))),
      ngrTitle = "rentReviewDetails.hasAgreedNewRent.label",
      isPageHeading = false
    )

  private def createWhoAgreedRadio(implicit messages: Messages): NGRRadio =
    ngrRadio(radioName = whoAgreedRadio,
      radioButtons = Seq(
        NGRRadioButtons(radioContent = "rentReviewDetails.whoAgreed.radio1.text", radioValue = Arbitrator),
        NGRRadioButtons(radioContent = "rentReviewDetails.whoAgreed.radio2.text", radioValue = IndependentExpert),
      ),
      ngrTitle = "rentReviewDetails.whoAgreed.label",
      ngrTitleClass = "govuk-fieldset__legend--s",
      isPageHeading = false
    )
}

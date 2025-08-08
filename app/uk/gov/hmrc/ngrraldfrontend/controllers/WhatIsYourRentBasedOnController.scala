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

package uk.gov.hmrc.ngrraldfrontend.controllers

import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Label, Text}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, PropertyLinkingAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.components.NavBarPageContents.createDefaultNavBar
import uk.gov.hmrc.ngrraldfrontend.models.forms.WhatIsYourRentBasedOnForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.WhatIsYourRentBasedOnForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.repo.RaldRepo
import uk.gov.hmrc.ngrraldfrontend.views.html.WhatIsYourRentBasedOnView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.NGRCharacterCountComponent
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatIsYourRentBasedOnController @Inject()(view: WhatIsYourRentBasedOnView,
                                                authenticate: AuthRetrievals,
                                                hasLinkedProperties: PropertyLinkingAction,
                                                raldRepo: RaldRepo,
                                                ngrCharacterCountComponent: NGRCharacterCountComponent,
                                                mcc: MessagesControllerComponents
                                               )(implicit appConfig: AppConfig, ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  private def otherRadioButton(form: Form[WhatIsYourRentBasedOnForm])(implicit messages: Messages): NGRRadioButtons = NGRRadioButtons(
    radioContent = "whatIsYourRentBasedOn.other",
    radioValue = Other,
    buttonHint = Some("whatIsYourRentBasedOn.other.hint"),
    conditionalHtml = Some(ngrCharacterCountComponent(form,
      NGRCharacterCount(
        id = "rent-based-on-other-desc",
        name = "rent-based-on-other-desc",
        maxLength = Some(250),
        label = Label(
          classes = "govuk-label govuk-label--s",
          content = Text(Messages("whatIsYourRentBasedOn.other.desc.label"))
        )
      )))
  )

  def ngrRadio(form: Form[WhatIsYourRentBasedOnForm])(implicit messages: Messages): NGRRadio =
    val ngrRadioButtons: Seq[NGRRadioButtons] = Seq(
      NGRRadioButtons(radioContent = "whatIsYourRentBasedOn.openMarket", radioValue = OpenMarket, buttonHint = Some("whatIsYourRentBasedOn.openMarket.hint")),
      NGRRadioButtons(radioContent = "whatIsYourRentBasedOn.percentageOpenMarket", radioValue = PercentageOpenMarket, buttonHint = Some("whatIsYourRentBasedOn.percentageOpenMarket.hint")),
      NGRRadioButtons(radioContent = "whatIsYourRentBasedOn.turnover", radioValue = Turnover, buttonHint = Some("whatIsYourRentBasedOn.turnover.hint")),
      NGRRadioButtons(radioContent = "whatIsYourRentBasedOn.percentageTurnover", radioValue = PercentageTurnover, buttonHint = Some("whatIsYourRentBasedOn.percentageTurnover.hint")),
      NGRRadioButtons(radioContent = "whatIsYourRentBasedOn.totalOccupancyCost", radioValue = TotalOccupancyCost, buttonHint = Some("whatIsYourRentBasedOn.totalOccupancyCost.hint")),
      NGRRadioButtons(radioContent = "whatIsYourRentBasedOn.indexation", radioValue = Indexation, buttonHint = Some("whatIsYourRentBasedOn.indexation.hint"))
    )
    NGRRadio(
      NGRRadioName("rent-based-on-radio"),
      NGRRadioButtons = ngrRadioButtons :+ otherRadioButton(form)
    )

  def show: Action[AnyContent] = {
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      request.propertyLinking.map(property =>
        Future.successful(Ok(view(createDefaultNavBar, form, buildRadios(form, ngrRadio(form)), property.addressFull)))
      ).getOrElse(throw new NotFoundException("Couldn't find property in mongo"))
    }
  }

  def submit: Action[AnyContent] = {
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            val correctedFormErrors = formWithErrors.errors.map { formError =>
              (formError.key, formError.messages) match
                case ("", messages) if messages.contains("whatIsYourRentBasedOn.otherText.error.required") =>
                  formError.copy(key = "rent-based-on-other-desc")
                case ("", messages) if messages.contains("whatIsYourRentBasedOn.otherText.error.maxLength") =>
                  formError.copy(key = "rent-based-on-other-desc")
                case _ =>
                  formError
            }
            val formWithCorrectedErrors = formWithErrors.copy(errors = correctedFormErrors)

            request.propertyLinking.map(property =>
                Future.successful(BadRequest(view(createDefaultNavBar, formWithCorrectedErrors,
                  buildRadios(formWithErrors, ngrRadio(formWithCorrectedErrors)), property.addressFull))))
              .getOrElse(throw new NotFoundException("Couldn't find property in mongo")),
          rentBasedOnForm =>
            raldRepo.insertRentBased(
              CredId(request.credId.getOrElse("")),
              rentBasedOnForm.radioValue,
              if (rentBasedOnForm.radioValue.equals("Other")) rentBasedOnForm.rentBasedOnOther else None
            )
            rentBasedOnForm.radioValue match
              case "PercentageTurnover" =>
                Future.successful(Redirect(routes.HowMuchIsTotalAnnualRentController.show.url))
              case _ =>
                Future.successful(Redirect(routes.WhatTypeOfAgreementController.show.url))
        )
    }
  }
}

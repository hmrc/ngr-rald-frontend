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
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.WhatIsYourRentBasedOnForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.WhatIsYourRentBasedOnForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{Mode, RentBasedOn, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.WhatIsYourRentBasedOnPage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.views.html.WhatIsYourRentBasedOnView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.NGRCharacterCountComponent
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WhatIsYourRentBasedOnController @Inject()(view: WhatIsYourRentBasedOnView,
                                                authenticate: AuthRetrievals,
                                                ngrCharacterCountComponent: NGRCharacterCountComponent,
                                                mcc: MessagesControllerComponents,
                                                getData: DataRetrievalAction,
                                                navigator: Navigator,
                                                sessionRepository: SessionRepository,
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

  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(CredId(request.credId))).get(WhatIsYourRentBasedOnPage) match {
        case None => form
        case Some(value) => form.fill(WhatIsYourRentBasedOnForm(value.rentBased,value.otherDesc))
      }
      Future.successful(Ok(view(preparedForm, buildRadios(preparedForm, ngrRadio(preparedForm)), request.property.addressFull, mode)))
    }
  }

  def submit(mode: Mode): Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors,
              buildRadios(formWithErrors, ngrRadio(formWithErrors)), request.property.addressFull, mode))),
          rentBasedOnForm =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(CredId(request.credId)))
                .set(WhatIsYourRentBasedOnPage, 
                  RentBasedOn(rentBasedOnForm.radioValue, 
                    if (rentBasedOnForm.radioValue == "Other")
                      rentBasedOnForm.rentBasedOnOther
                    else
                      None
                  )))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(WhatIsYourRentBasedOnPage, mode, updatedAnswers))
        )
    }
  }
}

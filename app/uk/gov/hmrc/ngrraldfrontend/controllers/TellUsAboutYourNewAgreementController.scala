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

import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, PropertyLinkingAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.connectors.NGRConnector
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.{NewAgreement, RenewedAgreement}
import uk.gov.hmrc.ngrraldfrontend.models.{RaldUserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.components.NavBarPageContents.createDefaultNavBar
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.repo.RaldRepo
import uk.gov.hmrc.ngrraldfrontend.views.html.TellUsAboutYourAgreementView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TellUsAboutYourNewAgreementController @Inject()(view: TellUsAboutYourAgreementView,
                                                      authenticate: AuthRetrievals,
                                                      hasLinkedProperties: PropertyLinkingAction,
                                                      ngrConnector: NGRConnector,
                                                      raldRepo: RaldRepo,
                                                      mcc: MessagesControllerComponents
                                                     )(implicit appConfig: AppConfig, ec:ExecutionContext)  extends FrontendController(mcc) with I18nSupport {

  def show: Action[AnyContent] = {
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      request.propertyLinking.map(property =>
          Future.successful(Ok(view(navigationBarContent = createDefaultNavBar, selectedPropertyAddress = property.addressFull, agreement = NewAgreement))))
        .getOrElse(throw new NotFoundException("Couldn't find property in mongo"))
    }
  }

    def submit: Action[AnyContent] = {
      (authenticate andThen hasLinkedProperties).async { implicit request =>
        request.propertyLinking.map(property =>
          raldRepo.upsertRaldUserAnswers(
            raldUserAnswers = RaldUserAnswers(
              credId = CredId(request.credId.getOrElse("")),
              agreementType = NewAgreement,
              selectedProperty = request.propertyLinking.getOrElse(throw new NotFoundException("failed to find property")),
              whatTypeOfAgreement = None
            )
          )
        )
        Future.successful(Redirect(routes.TellUsAboutYourNewAgreementController.show.url))
      }
  }
}

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
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, CheckRequestSentReferenceAction, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.RepairsAndInsuranceForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.RepairsAndInsuranceForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{Mode, RepairsAndInsurance, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.RepairsAndInsurancePage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.views.html.RepairsAndInsuranceView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RepairsAndInsuranceController @Inject()(repairsAndInsuranceView: RepairsAndInsuranceView,
                                              authenticate: AuthRetrievals,
                                              navigator: Navigator,
                                              getData: DataRetrievalAction,
                                              checkRequestSentReference: CheckRequestSentReferenceAction,
                                              sessionRepository: SessionRepository,
                                              mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen checkRequestSentReference andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(CredId(request.credId))).get(RepairsAndInsurancePage) match {
        case None => form
        case Some(value) => form.fill(RepairsAndInsuranceForm(value.internalRepairs, value.externalRepairs, value.buildingInsurance))
      }
        Future.successful(Ok(repairsAndInsuranceView(
          form = preparedForm,
          internalRepairs = buildRadios(preparedForm, RepairsAndInsuranceForm.createRadio("internalRepairs")),
          externalRepairs = buildRadios(preparedForm, RepairsAndInsuranceForm.createRadio("externalRepairs")),
          buildingInsurance = buildRadios(preparedForm, RepairsAndInsuranceForm.createRadio("buildingInsurance")),
          propertyAddress = request.property.addressFull,
          mode
        )))
    }
  }

  def submit(mode: Mode): Action[AnyContent] =
    (authenticate andThen checkRequestSentReference andThen getData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
            Future.successful(BadRequest(repairsAndInsuranceView(
              form = formWithErrors,
              internalRepairs = buildRadios(formWithErrors, RepairsAndInsuranceForm.createRadio("internalRepairs")),
              externalRepairs = buildRadios(formWithErrors, RepairsAndInsuranceForm.createRadio("externalRepairs")),
              buildingInsurance = buildRadios(formWithErrors, RepairsAndInsuranceForm.createRadio("buildingInsurance")),
              propertyAddress = request.property.addressFull,
              mode
            )))
        },
        repairsAndInsurance =>
          val answers = RepairsAndInsurance(repairsAndInsurance.internalRepairs, repairsAndInsurance.externalRepairs, repairsAndInsurance.buildingInsurance)
          for{
            updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(CredId(request.credId))).set(RepairsAndInsurancePage, answers))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(RepairsAndInsurancePage, mode, updatedAnswers))
      )
    }
}



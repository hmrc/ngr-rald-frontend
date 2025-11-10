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
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.*
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.services.CheckAnswers.*
import uk.gov.hmrc.ngrraldfrontend.views.html.CheckAnswersView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckAnswersController @Inject()(view: CheckAnswersView,
                                       authenticate: AuthRetrievals,
                                       mcc: MessagesControllerComponents,
                                       getData: DataRetrievalAction,
                                       sessionRepository: SessionRepository,
                                       navigator: Navigator
                                      )(implicit appConfig: AppConfig, ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      Future.successful(Ok(view(
        selectedPropertyAddress = request.property.addressFull,
        leaseRenewalsSummary = createLeaseRenewalsSummaryRows(credId = request.credId, userAnswers = request.userAnswers),
        landlordSummary = createLandlordSummaryRows(credId = request.credId, userAnswers = request.userAnswers),
        agreementDetailsSummary = createAgreementDetailsRows(credId = request.credId, userAnswers = request.userAnswers),
        rentSummary = createRentRows(credId = request.credId, userAnswers = request.userAnswers),
        firstRentPeriod = createRentPeriodRow(credId = request.credId, userAnswers = request.userAnswers),
        whatYourRentIncludesSummary = createWhatYourRentIncludesRows(credId = request.credId, userAnswers = request.userAnswers),
        repairsAndInsurance = createRepairsAndInsurance(credId = request.credId, userAnswers = request.userAnswers),
        rentReview = createRentReviewRows(credId = request.credId, userAnswers = request.userAnswers),
        payments = createPaymentRows(credId = request.credId, userAnswers = request.userAnswers)
      )))
    }
  }
}

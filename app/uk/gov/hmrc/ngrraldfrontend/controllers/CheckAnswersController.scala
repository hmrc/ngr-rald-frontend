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
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, CheckRequestSentReferenceAction, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.{CheckAnswersPage, TellUsAboutRentPage}
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.services.CheckAnswers.*
import uk.gov.hmrc.ngrraldfrontend.views.html.CheckAnswersView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckAnswersController @Inject()(view: CheckAnswersView,
                                       authenticate: AuthRetrievals,
                                       getData: DataRetrievalAction,
                                       checkRequestSentReference: CheckRequestSentReferenceAction,
                                       sessionRepository: SessionRepository,
                                       navigator: Navigator,
                                       mcc: MessagesControllerComponents,
                                      )(implicit appConfig: AppConfig, ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  def show: Action[AnyContent] = {
    (authenticate andThen checkRequestSentReference andThen getData).async { implicit request =>
      val userAnswers = request.userAnswers
      userAnswers match
        case Some(answers) =>
          Future.successful(Ok(view(
            selectedPropertyAddress = request.property.addressFull,
            leaseRenewalsSummary = createLeaseRenewalsSummaryRows(answers = answers),
            landlordSummary = createLandlordSummaryRows(answers = answers),
            agreementDetailsSummary = createAgreementDetailsRows(answers = answers),
            rentSummary = createRentRows(answers = answers),
            firstRentPeriod = createFirstRentPeriodRow(answers = answers),
            rentPeriods = createRentPeriodsSummaryLists(answers = answers),
            whatYourRentIncludesSummary = createWhatYourRentIncludesRows(answers = answers),
            repairsAndInsurance = createRepairsAndInsurance(answers = answers),
            rentReview = createRentReviewRows(answers = answers),
            repairsAndFittingOutSummary = createRepairsAndFittingOut(answers = answers),
            payments = createPaymentRows(answers = answers),
            breakClause = createBreakClauseRows(answers = answers),
            otherDetailsSummary = createOtherDetailsRow(answers = answers),
            isRentReviewed =  answers.get(TellUsAboutRentPage).nonEmpty
          )))
        case None => Future.successful(Redirect(appConfig.ngrDashboardUrl))
    }
  }

  def submit: Action[AnyContent] = {
    (authenticate andThen checkRequestSentReference andThen getData).async { implicit request =>
      Future.successful(Redirect(navigator.nextPage(CheckAnswersPage, NormalMode,
        request.userAnswers.getOrElse(throw new NotFoundException(s"Failed to find answers for credId: ${request.credId}")))))
    }
  }
}

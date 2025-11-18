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
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.forms.MoneyToTakeOnTheLeaseForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.MoneyToTakeOnTheLeaseForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{Mode, MoneyToTakeOnTheLease, NGRDate, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.MoneyToTakeOnTheLeasePage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.utils.DateKeyFinder
import uk.gov.hmrc.ngrraldfrontend.views.html.MoneyToTakeOnTheLeaseView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MoneyToTakeOnTheLeaseController @Inject()(MoneyToTakeOnTheLeaseView: MoneyToTakeOnTheLeaseView,
                                                authenticate: AuthRetrievals,
                                                getData: DataRetrievalAction,
                                                sessionRepository: SessionRepository,
                                                navigator: Navigator,
                                                mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with DateKeyFinder {

  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers
        .getOrElse(UserAnswers(CredId(request.credId)))
        .get(MoneyToTakeOnTheLeasePage) match {
        case None => form
        case Some(value) =>
          form.fill(MoneyToTakeOnTheLeaseForm(value.amount, NGRDate.fromString(value.date)))
      }

      Future.successful(Ok(MoneyToTakeOnTheLeaseView(
        form = preparedForm,
        propertyAddress = request.property.addressFull,
        mode = mode
      )))
    }
  }

  def submit(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val correctedFormErrors = formWithErrors.errors.map { formError =>
            (formError.key, formError.messages) match {
              case (key, messages) if messages.head.contains("moneyToTakeOnTheLease.date") =>
                setCorrectKey(formError, "moneyToTakeOnTheLease", "date")
              case _ => formError
            }
          }
          val formWithCorrectedErrors = formWithErrors.copy(errors = correctedFormErrors)

          Future.successful(BadRequest(MoneyToTakeOnTheLeaseView(
            form = formWithCorrectedErrors,
            propertyAddress = request.property.addressFull,
            mode = mode
          )))
        },
        advanceMoney => {
          val answers = MoneyToTakeOnTheLease(advanceMoney.amount, advanceMoney.date.makeString)
          for {
            updatedAnswers <- Future.fromTry(
              request.userAnswers.getOrElse(UserAnswers(CredId(request.credId)))
                .set(MoneyToTakeOnTheLeasePage, answers)
            )
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(MoneyToTakeOnTheLeasePage, mode, updatedAnswers))
        }
      )
    }
}
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
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.forms.DidYouGetIncentiveForNotTriggeringBreakClauseForm
import uk.gov.hmrc.ngrraldfrontend.models.{DidYouGetIncentiveForNotTriggeringBreakClause, Mode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.DidYouGetIncentiveForNotTriggeringBreakClausePage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.views.html.DidYouGetIncentiveForNotTriggeringBreakClauseView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DidYouGetIncentiveForNotTriggeringBreakClauseController @Inject()(
                                                                         didYouGetIncentiveForNotTriggeringBreakClauseView: DidYouGetIncentiveForNotTriggeringBreakClauseView,
                                                                         authenticate: AuthRetrievals,
                                                                         getData: DataRetrievalAction,
                                                                         formProvider: DidYouGetIncentiveForNotTriggeringBreakClauseForm,
                                                                         sessionRepository: SessionRepository,
                                                                         navigator: Navigator,
                                                                         mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  val form: Form[DidYouGetIncentiveForNotTriggeringBreakClause] = formProvider()

    def show(mode: Mode): Action[AnyContent] = {
      (authenticate andThen getData).async { implicit request =>
        val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.credId)).get(DidYouGetIncentiveForNotTriggeringBreakClausePage) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        Future.successful(Ok(didYouGetIncentiveForNotTriggeringBreakClauseView(
          selectedPropertyAddress = request.property.addressFull,
          form = preparedForm,
          mode = mode
        )))
      }
    }

  def submit(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(didYouGetIncentiveForNotTriggeringBreakClauseView(
            form = formWithErrors,
            selectedPropertyAddress = request.property.addressFull,
            mode = mode
          )))
        },
        radioValue =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.credId)).set(DidYouGetIncentiveForNotTriggeringBreakClausePage, radioValue))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(DidYouGetIncentiveForNotTriggeringBreakClausePage, mode, updatedAnswers))
      )
    }
  }

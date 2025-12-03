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
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.{Hint, PrefixOrSuffix, Text}
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.forms.InterimRentSetByTheCourtForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.InterimRentSetByTheCourtForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{InterimRentSetByTheCourt, Mode, NGRMonthYear, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.InterimSetByTheCourtPage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.utils.DateKeyFinder
import uk.gov.hmrc.ngrraldfrontend.views.html.InterimRentSetByTheCourtView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class InterimRentSetByTheCourtController @Inject()(interimRentSetByTheCourtView: InterimRentSetByTheCourtView,
                                                   authenticate: AuthRetrievals,
                                                   inputText: InputText,
                                                   getData: DataRetrievalAction,
                                                   sessionRepository: SessionRepository,
                                                   navigator: Navigator,
                                                   mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with DateKeyFinder {

  def generateInputText(form: Form[InterimRentSetByTheCourtForm], inputFieldName: String)(implicit messages: Messages): HtmlFormat.Appendable = {
    inputText(
      form = form,
      id = inputFieldName,
      name = inputFieldName,
      label = messages(s"interimRentSetByTheCourt.label.1"),
      labelClasses = Some("govuk-fieldset__legend govuk-fieldset__legend--s"),
      isVisible = true,
      classes = Some("govuk-input govuk-input--width-10"),
      prefix = Some(PrefixOrSuffix(content = Text("£")))
    )
  }

  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(CredId(request.credId))).get(InterimSetByTheCourtPage) match {
        case None => form
        case Some(value) => form.fill(InterimRentSetByTheCourtForm(value.amount, NGRMonthYear.fromString(value.date)))
      }
        Future.successful(Ok(interimRentSetByTheCourtView(
          form = preparedForm,
          propertyAddress = request.property.addressFull,
          interimAmount = generateInputText(preparedForm, "interimAmount"),
          mode = mode
        )))
    }
  }

  def submit(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val correctedFormErrors = formWithErrors.errors.map { formError =>
            (formError.key, formError.messages) match
              case (key, messages) if messages.head.contains("interimRentSetByTheCourt.date") =>
                setCorrectKey(formError, "interimRentSetByTheCourt", "date")
              case _ =>
                formError
          }
          val formWithCorrectedErrors = formWithErrors.copy(errors = correctedFormErrors)
            Future.successful(BadRequest(interimRentSetByTheCourtView(
              form = formWithCorrectedErrors,
              propertyAddress = request.property.addressFull,
              interimAmount = generateInputText(formWithCorrectedErrors, "interimAmount"),
              mode = mode
            )))
        },
        interimRent =>
          val answers = InterimRentSetByTheCourt(interimRent.amount,interimRent.date.makeString)
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(CredId(request.credId))).set(InterimSetByTheCourtPage, answers))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(InterimSetByTheCourtPage, mode, updatedAnswers))
      )
    }
  }

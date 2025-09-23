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
import uk.gov.hmrc.govukfrontend.views.Aliases.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.dateinput.DateInput
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentDatesAgreeForm
import uk.gov.hmrc.ngrraldfrontend.models.{Mode, NGRDate, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentDatesAgreeForm.form
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.{AgreedRentChangePage, RentDatesAgreePage}
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.utils.DateKeyFinder
import uk.gov.hmrc.ngrraldfrontend.views.html.RentDatesAgreeView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class RentDatesAgreeController @Inject()(rentDatesAgreeView: RentDatesAgreeView,
                                         authenticate: AuthRetrievals,
                                         mcc: MessagesControllerComponents,
                                         getData: DataRetrievalAction,
                                         navigator: Navigator,
                                         sessionRepository: SessionRepository
                                        )(implicit appConfig: AppConfig, ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport with DateKeyFinder{

  def dateInput()(implicit messages: Messages): DateInput = DateInput(
    id = "date",
    namePrefix = Some("rentDatesAgreeInput"),
    fieldset = Some(Fieldset(
      legend = Some(Legend(
        content = Text(messages("rentDatesAgree.subheading")),
        classes = "govuk-fieldset__legend--s",
        isPageHeading = true
      ))
    )),
    hint = Some(Hint(
      id = Some("rentDatesAgree.hint"),
      content = Text(messages("rentDatesAgree.hint"))
    ))
  )
  //TODO Add in preparedForm
  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.credId)).get(RentDatesAgreePage) match {
      case None => form
      case Some(value) => form.fill(RentDatesAgreeForm(NGRDate.fromString(value)))
    }
        Future.successful(Ok(rentDatesAgreeView(
          form = preparedForm,
          dateInput = dateInput(),
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
            (formError.key, formError.messages) match
              case (key, messages) if messages.contains("rentDatesAgree.date.month.required.error") =>
                formError.copy(key = "rentDatesAgreeInput.month")
              case (key, messages) if messages.contains("rentDatesAgree.date.month.year.required.error") =>
                formError.copy(key = "rentDatesAgreeInput.month")
              case (key, messages) if messages.contains("rentDatesAgree.date.year.required.error") =>
                formError.copy(key = "rentDatesAgreeInput.year")
              case _ =>
                formError.copy(key = "rentDatesAgreeInput.day")
          }
          val formWithCorrectedErrors = formWithErrors.copy(errors = correctedFormErrors)
            Future.successful(BadRequest(rentDatesAgreeView(
              form = formWithCorrectedErrors,
              dateInput = dateInput(),
              propertyAddress = request.property.addressFull,
              mode = mode
            )))
        },
        dateValue =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.credId)).set(RentDatesAgreePage, dateValue.dateInput.makeString))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(RentDatesAgreePage, mode, updatedAnswers))
      )
    }
}
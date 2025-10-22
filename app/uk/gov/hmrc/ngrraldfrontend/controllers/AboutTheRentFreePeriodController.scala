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
import uk.gov.hmrc.govukfrontend.views.Aliases.{DateInput, Fieldset, Hint, Legend, PrefixOrSuffix, Text}
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.forms.AboutTheRentFreePeriodForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.AboutTheRentFreePeriodForm.form
import uk.gov.hmrc.ngrraldfrontend.models.{Mode, AboutTheRentFreePeriod, NGRDate, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.AboutTheRentFreePeriodPage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.utils.DateKeyFinder
import uk.gov.hmrc.ngrraldfrontend.views.html.AboutTheRentFreePeriodView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AboutTheRentFreePeriodController @Inject()(aboutTheRentFreePeriodView: AboutTheRentFreePeriodView,
                                                          authenticate: AuthRetrievals,
                                                          inputText: InputText,
                                                          getData: DataRetrievalAction,
                                                          sessionRepository: SessionRepository,
                                                          navigator: Navigator,
                                                          mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with DateKeyFinder {

  def generateInputText(form: Form[AboutTheRentFreePeriodForm], inputFieldName: String)(implicit messages: Messages): HtmlFormat.Appendable = {
    inputText(
      form = form,
      id = inputFieldName,
      name = inputFieldName,
      label = messages(s"aboutTheRentFreePeriod.label"),
      isVisible = true,
      classes = Some("govuk-input govuk-input--width-3")
    )
  }

  def dateInput()(implicit messages: Messages): DateInput = DateInput(
    id = "date",
    namePrefix = Some("aboutTheRentFreePeriod"),
    fieldset = Some(Fieldset(
      legend = Some(Legend(
        content = Text(messages("aboutTheRentFreePeriod.date.label.2")),
        classes = "govuk-fieldset__legend--s",
        isPageHeading = false
      ))
    )),
    hint = Some(Hint(
      id = Some("aboutTheRentFreePeriod.date.hint.2"),
      content = Text(messages("aboutTheRentFreePeriod.date.hint.2"))
    ))
  )

  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.credId)).get(AboutTheRentFreePeriodPage) match {
        case None => form
        case Some(value) => form.fill(AboutTheRentFreePeriodForm(value.months, NGRDate.fromString(value.date)))
      }
      Future.successful(Ok(aboutTheRentFreePeriodView(
        form = preparedForm,
        propertyAddress = request.property.addressFull,
        howManyMonths = generateInputText(preparedForm, "howManyMonths"),
        dateInput = dateInput(),
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
              case (key, messages) if messages.head.contains("aboutTheRentFreePeriod.date") =>
                setCorrectKey(formError, "aboutTheRentFreePeriod", "date")
              case _ =>
                formError
          }
          val formWithCorrectedErrors = formWithErrors.copy(errors = correctedFormErrors)
          Future.successful(BadRequest(aboutTheRentFreePeriodView(
            form = formWithCorrectedErrors,
            propertyAddress = request.property.addressFull,
            howManyMonths = generateInputText(formWithCorrectedErrors, "howManyMonths"),
            dateInput = dateInput(),
            mode = mode
          )))
        },
        howManyMonths =>
          val answers = AboutTheRentFreePeriod(howManyMonths.howManyMonths, howManyMonths.date.makeString)
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.credId)).set(AboutTheRentFreePeriodPage, answers))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(AboutTheRentFreePeriodPage, mode, updatedAnswers))
      )
    }
}

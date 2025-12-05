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
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, CheckRequestSentReferenceAction, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.forms.MoneyYouPaidInAdvanceToLandlordForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.MoneyYouPaidInAdvanceToLandlordForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{Mode, MoneyYouPaidInAdvanceToLandlord, NGRDate, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.MoneyYouPaidInAdvanceToLandlordPage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.utils.DateKeyFinder
import uk.gov.hmrc.ngrraldfrontend.views.html.MoneyYouPaidInAdvanceToLandlordView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MoneyYouPaidInAdvanceToLandlordController @Inject()(moneyYouPaidInAdvanceToLandlordView: MoneyYouPaidInAdvanceToLandlordView,
                                                          authenticate: AuthRetrievals,
                                                          inputText: InputText,
                                                          getData: DataRetrievalAction,
                                                          checkRequestSentReference: CheckRequestSentReferenceAction,
                                                          sessionRepository: SessionRepository,
                                                          navigator: Navigator,
                                                          mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with DateKeyFinder {

  def generateInputText(form: Form[MoneyYouPaidInAdvanceToLandlordForm], inputFieldName: String)(implicit messages: Messages): HtmlFormat.Appendable = {
    inputText(
      form = form,
      id = inputFieldName,
      name = inputFieldName,
      label = messages("moneyYouPaidInAdvanceToLandlord.label.1"),
      labelClasses = Some("govuk-label govuk-label--s"),
      isVisible = true,
      classes = Some("govuk-input govuk-input--width-10"),
      prefix = Some(PrefixOrSuffix(content = Text("£")))
    )
  }

  def dateInput()(implicit messages: Messages): DateInput = DateInput(
    id = "date",
    namePrefix = Some("moneyYouPaidInAdvanceToLandlord"),
    fieldset = Some(Fieldset(
      legend = Some(Legend(
        content = Text(messages("moneyYouPaidInAdvanceToLandlord.date.label.2")),
        classes = "govuk-fieldset__legend--s"
      ))
    )),
    hint = Some(Hint(
      id = Some("moneyYouPaidInAdvanceToLandlord.date.hint.2"),
      content = Text(messages("moneyYouPaidInAdvanceToLandlord.date.hint.2"))
    ))
  )

  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen checkRequestSentReference andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(CredId(request.credId))).get(MoneyYouPaidInAdvanceToLandlordPage) match {
        case None => form
        case Some(value) => form.fill(MoneyYouPaidInAdvanceToLandlordForm(value.amount, NGRDate.fromString(value.date)))
      }
      Future.successful(Ok(moneyYouPaidInAdvanceToLandlordView(
        form = preparedForm,
        propertyAddress = request.property.addressFull,
        advanceMoney = generateInputText(preparedForm, "advanceMoney"),
        dateInput = dateInput(),
        mode = mode
      )))
    }
  }

  def submit(mode: Mode): Action[AnyContent] =
    (authenticate andThen checkRequestSentReference andThen getData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val correctedFormErrors = formWithErrors.errors.map { formError =>
            (formError.key, formError.messages) match
              case (key, messages) if messages.head.contains("moneyYouPaidInAdvanceToLandlord.date") =>
                setCorrectKey(formError, "moneyYouPaidInAdvanceToLandlord", "date")
              case _ =>
                formError
          }
          val formWithCorrectedErrors = formWithErrors.copy(errors = correctedFormErrors)
          Future.successful(BadRequest(moneyYouPaidInAdvanceToLandlordView(
            form = formWithCorrectedErrors,
            propertyAddress = request.property.addressFull,
            advanceMoney = generateInputText(formWithCorrectedErrors, "advanceMoney"),
            dateInput = dateInput(),
            mode = mode
          )))
        },
        advanceMoney =>
          val answers = MoneyYouPaidInAdvanceToLandlord(advanceMoney.amount,advanceMoney.date.makeString)
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(CredId(request.credId))).set(MoneyYouPaidInAdvanceToLandlordPage, answers))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(MoneyYouPaidInAdvanceToLandlordPage, mode, updatedAnswers))
      )
    }
}
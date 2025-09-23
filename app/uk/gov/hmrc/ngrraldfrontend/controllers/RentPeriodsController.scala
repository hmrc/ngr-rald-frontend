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

import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{Table, TableRow}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.{Mode, ProvideDetailsOfFirstSecondRentPeriod}
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentPeriodsForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentPeriodsForm.form
import uk.gov.hmrc.ngrraldfrontend.models.UserAnswers
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.{ProvideDetailsOfFirstSecondRentPeriodPage, RentPeriodsPage}
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.utils.CurrencyHelper
import uk.gov.hmrc.ngrraldfrontend.views.html.RentPeriodView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RentPeriodsController @Inject()(view: RentPeriodView,
                                      authenticate: AuthRetrievals,
                                      getData: DataRetrievalAction,
                                      mcc: MessagesControllerComponents,
                                      sessionRepository: SessionRepository,
                                      navigator: Navigator,
                                     )(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with CurrencyHelper {

  def firstTable(userAnswers: ProvideDetailsOfFirstSecondRentPeriod)(implicit messages: Messages): Table =
    Table(
      rows = Seq(
        Seq(
          TableRow(
            content = Text(messages("rentPeriods.first.startDate"))
          ),
          TableRow(
            content = Text(userAnswers.firstDateStart),
            attributes = Map(
              "id" -> "first-period-start-date-id"
            )
          )
        ),
        Seq(
          TableRow(
            content = Text(messages("rentPeriods.first.endDate"))
          ),
          TableRow(
            content = Text(userAnswers.firstDateEnd),
            attributes = Map(
              "id" -> "first-period-end-date-id"
            )
          )
        ),
        userAnswers.firstRentPeriodAmount match
            case Some(answer) => Seq(
              TableRow(
                content = Text(messages("rentPeriods.first.rentValue"))
              ),
              TableRow(
                content = Text(formatRentValue(answer.toDouble)),
                attributes = Map(
                  "id" -> "first-period-rent-value-id"
                )
              )
            )
            case None => Seq(),
        Seq(
          TableRow(
            content = Text(messages("rentPeriods.first.doYouPay"))
          ),
          TableRow(
            content = Text(
              if (userAnswers.firstRentPeriodRadio) {
                "Yes"
              } else {
                "No"
              }
            ),
            attributes = Map(
              "id" -> "first-period-has-pay-id"
            )
          )
        )
      ),
      head = None,
      caption = Some(Messages("rentPeriods.first.subheading")),
      captionClasses = "govuk-table__caption--m",
      firstCellIsHeader = true
    )

  def secondTable(userAnswers: ProvideDetailsOfFirstSecondRentPeriod)(implicit messages: Messages): Table = Table(
    rows = Seq(
      Seq(
        TableRow(
          content = Text(messages("rentPeriods.second.startDate"))
        ),
        TableRow(
          content = Text(userAnswers.secondDateStart),
          attributes = Map(
            "id" -> "second-period-start-date-id"
          )
        )
      ),
      Seq(
        TableRow(
          content = Text(messages("rentPeriods.second.endDate"))
        ),
        TableRow(
          content = Text(userAnswers.secondDateEnd),
          attributes = Map(
            "id" -> "second-period-end-date-id"
          )
        )
      ),
      Seq(
        TableRow(
          content = Text(messages("rentPeriods.second.rentValue"))
        ),
        TableRow(
          content = Text(userAnswers.secondHowMuchIsRent),
          attributes = Map(
            "id" -> "second-period-rent-value-id"
          )
        )
      )
    ),
    head = None,
    caption = Some(Messages("rentPeriods.second.subheading")),
    captionClasses = "govuk-table__caption--m",
    firstCellIsHeader = true
  )

  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      request.userAnswers.getOrElse(UserAnswers(request.credId)).get(ProvideDetailsOfFirstSecondRentPeriodPage) match {
        case Some(value) =>
          val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.credId)).get(RentPeriodsPage) match {
            case Some(value) => form.fill(RentPeriodsForm(value))
            case None => form
          }
          Future.successful(Ok(view(
            selectedPropertyAddress = request.property.addressFull,
            preparedForm,
            firstTable = firstTable(value),
            secondTable = secondTable(value),
            ngrRadio = buildRadios(preparedForm, RentPeriodsForm.ngrRadio(preparedForm)),
            mode = mode)))
        case None => throw new Exception("Not found answers")
      }
    }
  }

  def submit(mode: Mode): Action[AnyContent]   = {
    (authenticate andThen getData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            request.userAnswers.getOrElse(UserAnswers(request.credId)).get(ProvideDetailsOfFirstSecondRentPeriodPage) match {
              case Some(value) => Future.successful(BadRequest(view(
                selectedPropertyAddress = request.property.addressFull,
                formWithErrors,
                firstTable = firstTable(value),
                secondTable = secondTable(value),
                buildRadios(formWithErrors, RentPeriodsForm.ngrRadio(formWithErrors)), mode = mode)))
              case None => throw new NotFoundException("Couldn't find user Answers")
            },
          rentPeriodsForm =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.credId)).set(RentPeriodsPage, rentPeriodsForm.radioValue))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(RentPeriodsPage, mode, updatedAnswers))
        )
    }
  }
}    
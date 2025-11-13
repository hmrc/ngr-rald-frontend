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
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentPeriodsForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentPeriodsForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{Mode, NGRDate, NormalMode, ProvideDetailsOfFirstRentPeriod, DetailsOfRentPeriod, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.{ProvideDetailsOfFirstRentPeriodPage, ProvideDetailsOfSecondRentPeriodPage, RentPeriodsPage}
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.utils.CurrencyHelper
import uk.gov.hmrc.ngrraldfrontend.views.html.RentPeriodView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.time.LocalDate
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

  def firstTable(firstRentPeriods: ProvideDetailsOfFirstRentPeriod)(implicit messages: Messages): Table =
    Table(
      rows = Seq(
        Seq(
          TableRow(
            content = Text(messages("rentPeriods.first.startDate"))
          ),
          TableRow(
            content = Text(NGRDate.formatDate(firstRentPeriods.startDate.toString)),
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
            content = Text(NGRDate.formatDate(firstRentPeriods.endDate.toString)),
            attributes = Map(
              "id" -> "first-period-end-date-id"
            )
          )
        ),
        firstRentPeriods.rentPeriodAmount match
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
              if (firstRentPeriods.isRentPayablePeriod) {
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

  def rentPeriodTable(startDate: String, rentPeriod: DetailsOfRentPeriod, index: Int)(implicit messages: Messages): Table = {
    val periodSequence = messages(s"rentPeriod.${index + 2}.sequence")
    val periodSequenceLowerCase = periodSequence.toLowerCase
    Table(
      rows = Seq(
        Seq(
          TableRow(
            content = Text(messages("rentPeriods.second.startDate"))
          ),
          TableRow(
            content = Text(NGRDate.formatDate(startDate)),
            attributes = Map(
              "id" -> s"$periodSequenceLowerCase-period-start-date-id"
            )
          )
        ),
        Seq(
          TableRow(
            content = Text(messages("rentPeriods.second.endDate"))
          ),
          TableRow(
            content = Text(NGRDate.formatDate(rentPeriod.endDate)),
            attributes = Map(
              "id" -> s"$periodSequenceLowerCase-period-end-date-id"
            )
          )
        ),
        Seq(
          TableRow(
            content = Text(messages("rentPeriods.second.rentValue"))
          ),
          TableRow(
            content = Text(formatRentValue(rentPeriod.rentPeriodAmount.toDouble)),
            attributes = Map(
              "id" -> s"$periodSequenceLowerCase-period-rent-value-id"
            )
          )
        )
      ),
      head = None,
      caption = Some(messages("rentPeriods.second.subheading", periodSequence)),
      captionClasses = "govuk-table__caption--m",
      firstCellIsHeader = true
    )
  }

  def createRentPeriodsDetailsTables(firstRentPeriod: ProvideDetailsOfFirstRentPeriod, rentPeriods: Seq[DetailsOfRentPeriod])(implicit messages: Messages): Seq[Table] = {
    val secondRentPeriodStartDate: String = firstRentPeriod.endDate.plusDays(1).toString
    val rentPeriodsStartDates: Seq[String] = rentPeriods.map(_.endDate).map(LocalDate.parse(_).plusDays(1).toString).dropRight(1)
    val rentPeriodsWithStartDates: Seq[((DetailsOfRentPeriod, String), Int)] = rentPeriods.zip(secondRentPeriodStartDate +: rentPeriodsStartDates).zipWithIndex
    rentPeriodsWithStartDates.map(details => rentPeriodTable(details._1._2, details._1._1, details._2))
  }

  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      val userAnswers = request.userAnswers.getOrElse(UserAnswers(CredId(request.credId)))
      (userAnswers.get(ProvideDetailsOfFirstRentPeriodPage), userAnswers.get(ProvideDetailsOfSecondRentPeriodPage)) match {
        case (Some(firstRentPeriod), Some(rentPeriods)) if rentPeriods.nonEmpty=>
          val preparedForm = userAnswers.get(RentPeriodsPage) match {
            case Some(value) => form.fill(RentPeriodsForm(value.toString))
            case None => form
          }
          Future.successful(Ok(view(
            selectedPropertyAddress = request.property.addressFull,
            preparedForm,
            firstTable = firstTable(firstRentPeriod),
            rentPeriodsTables = createRentPeriodsDetailsTables(firstRentPeriod, rentPeriods),
            ngrRadio = buildRadios(preparedForm, RentPeriodsForm.rentPeriodsRadio(preparedForm)),
            mode = mode)))
        case (_, _) => Future.successful(Redirect(routes.ProvideDetailsOfFirstRentPeriodController.show(mode)))
      }
    }
  }

  def submit(mode: Mode): Action[AnyContent]   = {
    (authenticate andThen getData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            val userAnswers = request.userAnswers.getOrElse(UserAnswers(CredId(request.credId)))
            (userAnswers.get(ProvideDetailsOfFirstRentPeriodPage), userAnswers.get(ProvideDetailsOfSecondRentPeriodPage)) match {
              case (Some(firstRentPeriod), Some(rentPeriods)) if rentPeriods.nonEmpty => Future.successful(BadRequest(view(
                selectedPropertyAddress = request.property.addressFull,
                formWithErrors,
                firstTable = firstTable(firstRentPeriod),
                rentPeriodsTables = createRentPeriodsDetailsTables(firstRentPeriod, rentPeriods),
                buildRadios(formWithErrors, RentPeriodsForm.rentPeriodsRadio(formWithErrors)), mode = mode)))
              case (_, _) => throw new NotFoundException("Couldn't find user Answers")
            },
          rentPeriodsForm =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(CredId(request.credId)))
                .set(RentPeriodsPage, rentPeriodsForm.radioValue.toBoolean))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(RentPeriodsPage, mode, updatedAnswers))
        )
    }
  }
}    
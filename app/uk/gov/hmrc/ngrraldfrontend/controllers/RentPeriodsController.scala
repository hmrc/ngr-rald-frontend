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
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, CheckRequestSentReferenceAction, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentPeriodsForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentPeriodsForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{DetailsOfRentPeriod, Mode, NGRDate, ProvideDetailsOfFirstRentPeriod, UserAnswers}
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
                                      checkRequestSentReference: CheckRequestSentReferenceAction,
                                      mcc: MessagesControllerComponents,
                                      sessionRepository: SessionRepository,
                                      navigator: Navigator,
                                     )(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with CurrencyHelper {

  private def summaryListRow(key: String, valueId: String, value: String)(implicit messages: Messages) =
    SummaryListRow(key = Key(content = Text(messages(key)), classes = "govuk-summary-list__key_width"),
      value = Value(HtmlContent(s"""<span id="$valueId">$value</span>""")))

  def firstPeriodSummaryList(firstRentPeriods: ProvideDetailsOfFirstRentPeriod)(implicit messages: Messages): SummaryList = {
    val isRentPayable = firstRentPeriods.isRentPayablePeriod
    val rows = Seq(
      summaryListRow(key = "rentPeriods.first.startDate", valueId = "first-period-start-date-id", value = NGRDate.formatDate(firstRentPeriods.startDate.toString)),
      summaryListRow(key = "rentPeriods.first.endDate", valueId = "first-period-end-date-id", value = NGRDate.formatDate(firstRentPeriods.endDate.toString)),
      summaryListRow(key = "rentPeriods.first.doYouPay", valueId = "first-period-has-pay-id", value = if (isRentPayable) "Yes" else "No")
    )
    SummaryList(
      card = Some(Card(title = Some(CardTitle(content = Text(Messages("rentPeriods.first.subheading")))))),
      rows = if (!isRentPayable)
        rows
      else
        rows :+ summaryListRow(key = "rentPeriods.first.rentValue", valueId = "first-period-rent-value-id", value = firstRentPeriods.rentPeriodAmount.map(amount => formatRentValue(amount.toDouble)).getOrElse("£0"))
    )
  }

  def rentPeriodSummaryList(startDate: String, rentPeriod: DetailsOfRentPeriod, index: Int)(implicit messages: Messages): SummaryList = {
    val periodSequence = messages(s"rentPeriod.${index + 2}.sequence")
    val periodSequenceLowerCase = periodSequence.toLowerCase
    SummaryList(
      card = Some(Card(title = Some(CardTitle(content = Text(messages("rentPeriods.second.subheading", periodSequence)))))),
      rows = Seq(
        summaryListRow(key = "rentPeriods.second.startDate", valueId = s"$periodSequenceLowerCase-period-start-date-id", value = NGRDate.formatDate(startDate)),
        summaryListRow(key = "rentPeriods.second.endDate", valueId = s"$periodSequenceLowerCase-period-end-date-id", value = NGRDate.formatDate(rentPeriod.endDate)),
        summaryListRow(key = "rentPeriods.second.rentValue", valueId = s"$periodSequenceLowerCase-period-rent-value-id", value = formatRentValue(rentPeriod.rentPeriodAmount.toDouble))
      )
    )
  }

  def createRentPeriodsDetailsSummaryLists(firstRentPeriod: ProvideDetailsOfFirstRentPeriod, rentPeriods: Seq[DetailsOfRentPeriod])(implicit messages: Messages): Seq[SummaryList] = {
    val secondRentPeriodStartDate: String = firstRentPeriod.endDate.plusDays(1).toString
    val rentPeriodsStartDates: Seq[String] = rentPeriods.map(_.endDate).map(LocalDate.parse(_).plusDays(1).toString).dropRight(1)
    val rentPeriodsWithStartDates: Seq[((DetailsOfRentPeriod, String), Int)] = rentPeriods.zip(secondRentPeriodStartDate +: rentPeriodsStartDates).zipWithIndex
    rentPeriodsWithStartDates.map(details => rentPeriodSummaryList(details._1._2, details._1._1, details._2))
  }

  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen checkRequestSentReference andThen getData).async { implicit request =>
      val userAnswers = request.userAnswers.getOrElse(UserAnswers(CredId(request.credId)))
      (userAnswers.get(ProvideDetailsOfFirstRentPeriodPage), userAnswers.get(ProvideDetailsOfSecondRentPeriodPage)) match {
        case (Some(firstRentPeriod), Some(rentPeriods)) if rentPeriods.nonEmpty =>
          val preparedForm = userAnswers.get(RentPeriodsPage) match {
            case Some(value) => form.fill(RentPeriodsForm(value.toString))
            case None => form
          }
          Future.successful(Ok(view(
            selectedPropertyAddress = request.property.addressFull,
            preparedForm,
            firstRentPeriodSummaryList = firstPeriodSummaryList(firstRentPeriod),
            rentPeriodsSummaryLists = createRentPeriodsDetailsSummaryLists(firstRentPeriod, rentPeriods),
            ngrRadio = buildRadios(preparedForm, RentPeriodsForm.rentPeriodsRadio(preparedForm, rentPeriods.size)),
            mode = mode)))
        case (_, _) => Future.successful(Redirect(routes.ProvideDetailsOfFirstRentPeriodController.show(mode)))
      }
    }
  }

  def submit(mode: Mode): Action[AnyContent] = {
    (authenticate andThen checkRequestSentReference andThen getData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            val userAnswers = request.userAnswers.getOrElse(UserAnswers(CredId(request.credId)))
            (userAnswers.get(ProvideDetailsOfFirstRentPeriodPage), userAnswers.get(ProvideDetailsOfSecondRentPeriodPage)) match {
              case (Some(firstRentPeriod), Some(rentPeriods)) if rentPeriods.nonEmpty => Future.successful(BadRequest(view(
                selectedPropertyAddress = request.property.addressFull,
                formWithErrors,
                firstRentPeriodSummaryList = firstPeriodSummaryList(firstRentPeriod),
                rentPeriodsSummaryLists = createRentPeriodsDetailsSummaryLists(firstRentPeriod, rentPeriods),
                buildRadios(formWithErrors, RentPeriodsForm.rentPeriodsRadio(formWithErrors, rentPeriods.size)), mode = mode)))
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
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
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, PropertyLinkingAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.RaldUserAnswers
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.components.NavBarPageContents.createDefaultNavBar
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentPeriodsForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentPeriodsForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.repo.RaldRepo
import uk.gov.hmrc.ngrraldfrontend.views.html.RentPeriodView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RentPeriodsController @Inject()(view: RentPeriodView,
                                      authenticate: AuthRetrievals,
                                      hasLinkedProperties: PropertyLinkingAction,
                                      raldRepo: RaldRepo,
                                      mcc: MessagesControllerComponents
                                     )(implicit appConfig: AppConfig, ec:ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  def firstTable(userAnswers: RaldUserAnswers)(implicit messages:Messages): Table =
    Table(
    rows = Seq(
      Seq(
        TableRow(
          content = Text(messages("rentPeriods.first.startDate"))
        ),
        TableRow(
          content = Text(userAnswers.provideDetailsOfFirstSecondRentPeriod.map{ dates =>
            dates.firstDateStart
          }.getOrElse(""))
        )
      ),
      Seq(
        TableRow(
          content = Text(messages("rentPeriods.first.endDate"))
        ),
        TableRow(
          content = Text(userAnswers.provideDetailsOfFirstSecondRentPeriod.map{ dates =>
            dates.firstDateEnd
          }.getOrElse(""))
        )
      ),
      if(userAnswers.provideDetailsOfFirstSecondRentPeriod.nonEmpty){
        Seq(
          TableRow(
            content = Text(messages("rentPeriods.first.rentValue"))
          ),
          TableRow(
            content = Text(userAnswers.provideDetailsOfFirstSecondRentPeriod.map { dates =>
              dates.firstRentPeriodAmount.get
            }.getOrElse(""))
          )
        )
      }else(Seq()),
      Seq(
        TableRow(
          content = Text(messages("rentPeriods.first.doYouPay"))
        ),
        TableRow(
          content = Text(userAnswers.provideDetailsOfFirstSecondRentPeriod.map{ dates =>
            if(dates.firstRentPeriodRadio == true){
              "Yes"
            }else{"False"}
          }.getOrElse(""))
        )
      )
    ),
    head = None,
    caption = Some(Messages("rentPeriods.first.subheading")),
    captionClasses = "govuk-table__caption--m",
    firstCellIsHeader = true
  )

  def secondTable(userAnswers: RaldUserAnswers)(implicit messages: Messages): Table = Table(
    rows = Seq(
      Seq(
        TableRow(
          content = Text(messages("rentPeriods.second.startDate"))
        ),
        TableRow(
          content = Text(userAnswers.provideDetailsOfFirstSecondRentPeriod.map{ dates =>
            dates.secondDateStart
          }.getOrElse(""))
        )
      ),
      Seq(
        TableRow(
          content = Text(messages("rentPeriods.second.endDate"))
        ),
        TableRow(
          content = Text(userAnswers.provideDetailsOfFirstSecondRentPeriod.map { dates =>
            dates.secondDateEnd
          }.getOrElse(""))
        )
      ),
        Seq(
          TableRow(
            content = Text(messages("rentPeriods.second.rentValue"))
          ),
          TableRow(
            content = Text(userAnswers.provideDetailsOfFirstSecondRentPeriod.map { dates =>
              dates.firstRentPeriodAmount.get
            }.getOrElse(""))
          )
        )
    ),
    head = None,
    caption = Some(Messages("rentPeriods.first.subheading")),
    captionClasses = "govuk-table__caption--m",
    firstCellIsHeader = true
  )

  def show: Action[AnyContent] = {
    (authenticate andThen hasLinkedProperties).async { implicit request =>
     raldRepo.findByCredId(CredId(request.credId.getOrElse(""))).flatMap {
        case Some(answers: RaldUserAnswers) =>
          Future.successful(Ok(view(
            navigationBarContent = createDefaultNavBar,
            selectedPropertyAddress = answers.selectedProperty.addressFull,
            form,
            firstTable = firstTable(answers),
            secondTable = secondTable(answers),
            ngrRadio = buildRadios(form, RentPeriodsForm.ngrRadio(form)))))
        case None =>
          throw new NotFoundException("Couldn't find user Answers")
      }
    }
  }

  def submit: Action[AnyContent]   = {
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            raldRepo.findByCredId(CredId(request.credId.getOrElse(""))).flatMap {
              case Some(answers: RaldUserAnswers) =>
                Future.successful(BadRequest(view(
                  navigationBarContent = createDefaultNavBar,
                  selectedPropertyAddress = answers.selectedProperty.addressFull,
                  formWithErrors,
                  firstTable = firstTable(answers),
                  secondTable = secondTable(answers),
                  buildRadios(formWithErrors, RentPeriodsForm.ngrRadio(formWithErrors)))))
              case None => throw new NotFoundException("Couldn't find user Answers")
            },
          rentPeriodsForm =>
            raldRepo.insertRentPeriod(
              CredId(request.credId.getOrElse("")),
              rentPeriodsForm.radioValue
            )
            Future.successful(Redirect(routes.WhatTypeOfAgreementController.show.url))
        )
    }
  }
}    

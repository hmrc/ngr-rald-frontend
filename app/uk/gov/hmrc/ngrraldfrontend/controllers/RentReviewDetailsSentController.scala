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
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{Table, TableRow}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.connectors.NGRConnector
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.{NewAgreement, RenewedAgreement, RentAgreement}
import uk.gov.hmrc.ngrraldfrontend.models.{AgreementType, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.vmvProperty.VMVProperty
import uk.gov.hmrc.ngrraldfrontend.pages.*
import uk.gov.hmrc.ngrraldfrontend.views.html.RentReviewDetailsSentView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RentReviewDetailsSentController @Inject()(view: RentReviewDetailsSentView,
                                                authenticate: AuthRetrievals,
                                                mcc: MessagesControllerComponents,
                                                getData: DataRetrievalAction,
                                                ngrConnector: NGRConnector
                                               )(implicit appConfig: AppConfig, executionContext: ExecutionContext) extends FrontendController(mcc) with I18nSupport {


  def firstTable(property: VMVProperty)(implicit messages: Messages): Table =
    Table(
      rows = Seq(
        Seq(
          TableRow(
            content = Text(messages("rentReviewDetailsSent.p3")),
            classes = "govuk-!-font-weight-bold"
          ),
          TableRow(
            content = Text(property.addressFull),
            classes = "govuk-table__caption--s, govuk-!-width-two-thirds",
            attributes = Map(
              "id" -> "full-address-id"
            )
          ),
        ),
        Seq(
          TableRow(
            content = Text(messages("rentReviewDetailsSent.p4")),
            classes = "govuk-!-font-weight-bold"
          ),
          TableRow(
            content = Text(property.localAuthorityReference),
            classes = "govuk-table__caption--s, govuk-!-width-two-thirds",
            attributes = Map(
              "id" -> "local-authority-reference-id"
            )
          ))))


  def confirmation: Action[AnyContent] =
    (authenticate andThen getData).async { implicit request =>
//      val answers = request.userAnswers.getOrElse(CredId(request.credId))
      
      val newAgreement = request.userAnswers.map(answers => answers.getCurrentJourneyUserAnswers(TellUsAboutYourNewAgreementPage, answers, request.credId))
      val renewed = request.userAnswers.map(answers => answers.getCurrentJourneyUserAnswers(TellUsAboutYourRenewedAgreementPage, answers, request.credId))
      val rent = request.userAnswers.map(answers => answers.getCurrentJourneyUserAnswers(TellUsAboutRentPage, answers, request.credId))

      ngrConnector.getRaldUserAnswers(CredId(request.credId)).flatMap {
        case Some(raldUserAnswers) => Future.successful(Ok(view(
          raldUserAnswers.get(DeclarationPage),
          firstTable(request.property),
          request.email.getOrElse(""),
          if (newAgreement == NewAgreement) {
            NewAgreement
          } else if (renewed == RenewedAgreement) {
            RenewedAgreement
          } else {
            RentAgreement
          }
        )))
        case None => Future.failed(throw new NotFoundException("Unable to find rald user answers"))
      }
    }
}



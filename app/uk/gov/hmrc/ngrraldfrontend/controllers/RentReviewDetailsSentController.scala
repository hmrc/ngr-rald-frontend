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
import uk.gov.hmrc.govukfrontend.views.Aliases.{SummaryListRow, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{Table, TableRow}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.connectors.NGRConnector
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.NewAgreement
import uk.gov.hmrc.ngrraldfrontend.models.NGRSummaryListRow.summarise
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.{buildRadios, simpleNgrRadio}
import uk.gov.hmrc.ngrraldfrontend.models.forms.ConfirmBreakClauseForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.ConfirmBreakClauseForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.{CredId, RatepayerRegistration}
import uk.gov.hmrc.ngrraldfrontend.models.vmvProperty.VMVProperty
import uk.gov.hmrc.ngrraldfrontend.models.{AuthenticatedUserRequest, Mode, NGRSummaryListRow, PropertyLinkingUserAnswers, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.ConfirmBreakClausePage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.views.html.{ConfirmBreakClauseView, RentReviewDetailsSentView}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.ngrraldfrontend.models.components.{TableData, TableHeader, TableRowIsActive, TableRowLink, TableRowText}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RentReviewDetailsSentController @Inject()(view: RentReviewDetailsSentView,
                                                authenticate: AuthRetrievals,
                                                mcc: MessagesControllerComponents,
                                                getData: DataRetrievalAction,
                                                ngrConnector: NGRConnector,
                                                sessionRepository: SessionRepository,
                                                navigator: Navigator
                                               )(implicit appConfig: AppConfig, executionContext: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  private def generateTable(propertyList: List[VMVProperty])(implicit messages: Messages): Table = {
    TableData(
      headers = Seq(
        TableHeader("Address", "govuk-table__caption--s govuk-!-width-half"),
        TableHeader("Property reference", "govuk-table__caption--s, govuk-!-width-one-quarter")),
      rows = propertyList.map(property => Seq(
        TableRowText(property.addressFull),
        TableRowText(property.localAuthorityReference))),
      caption = Some(messages(""))
    ).toTable
  }

  def firstTable(property: VMVProperty)(implicit messages: Messages): Table =
    Table(
      rows = Seq(
        Seq(
          TableRow(
            content = HtmlContent(messages("rentReviewDetailsSent.p3"))
          ),
          TableRow(
            content = Text(property.addressFull),
            classes = "govuk-table__caption--s, govuk-!-width-two-thirds"
          ),
        ),
        Seq(
          TableRow(
            content = HtmlContent(messages("rentReviewDetailsSent.p4"))
          ),
          TableRow(
            content = Text(property.localAuthorityReference),
            classes = "govuk-table__caption--s, govuk-!-width-two-thirds"
          ))))


  def confirmation(): Action[AnyContent] =
    (authenticate).async { implicit request: AuthenticatedUserRequest[AnyContent] =>
      ngrConnector.getLinkedProperty(CredId(request.credId.getOrElse(""))).flatMap {
        case Some(vmvProperty) => Future.successful(Ok(view(firstTable(vmvProperty))))
        case None => Future.failed(throw new NotFoundException("Unable to find match Linked Properties"))
      }
    }

//  def confirmation: Action[AnyContent] = {
//    (authenticate andThen getData).async { implicit AuthenticatedUserRequest =>
//      val propertyAddress = AuthenticatedUserRequest.property.addressFull
//      val propertyReference = AuthenticatedUserRequest.property.localAuthorityReference
//      val email = AuthenticatedUserRequest.property
//
//      Future.successful(Ok(view(propertyAddress, propertyReference)))
//    }
//  }
}


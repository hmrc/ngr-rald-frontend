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

package uk.gov.hmrc.ngrraldfrontend.actions

import com.google.inject.ImplementedBy
import play.api.mvc.Results.Redirect
import play.api.mvc.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.connectors.NGRConnector
import uk.gov.hmrc.ngrraldfrontend.controllers.routes
import uk.gov.hmrc.ngrraldfrontend.models.AuthenticatedUserRequest
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.repo.RaldRepo
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PropertyLinkingActionImpl @Inject()(
                                    ngrConnector: NGRConnector,
                                    authenticate: AuthRetrievals,
                                    raldRepo: RaldRepo,
                                    appConfig: AppConfig,
                                    mcc: MessagesControllerComponents
                                  )(implicit ec: ExecutionContext) extends PropertyLinkingAction with RegistrationAction {

  override def invokeBlock[A](request: Request[A], block: AuthenticatedUserRequest[A] => Future[Result]): Future[Result] = {

    authenticate.invokeBlock(request, { implicit authRequest: AuthenticatedUserRequest[A] =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(authRequest, authRequest.session)

      val credId = CredId(authRequest.credId.getOrElse(""))

      def checkPropertyLinking(): Future[Result] =
        raldRepo.findByCredId(credId).flatMap{
          case Some(property) =>  block(authRequest.copy(propertyLinking = Some(property.selectedProperty)))
          case _ => ngrConnector.getPropertyLinkingUserAnswers(credId).flatMap { maybePropertyLinkingUserAnswers =>
            if (maybePropertyLinkingUserAnswers.isDefined) {
              block(authRequest.copy(propertyLinking = maybePropertyLinkingUserAnswers.map{selectedProperty => selectedProperty.vmvProperty}))
            } else {
              redirectToDashboard()
            }
          }
        }

      ngrConnector.getRatepayer(credId).flatMap { maybeRatepayer =>
        val isRegistered = maybeRatepayer
          .flatMap(_.ratepayerRegistration)
          .flatMap(_.isRegistered)
          .getOrElse(false)

        if (isRegistered)
          checkPropertyLinking()
        else
          redirectToRegister()
      }
    })
  }

  private def redirectToRegister(): Future[Result] = {
    Future.successful(Redirect(s"${appConfig.ngrLoginRegistrationHost}/ngr-login-register-frontend/register"))
  }

  private def redirectToDashboard(): Future[Result] = {
    Future.successful(Redirect(s"${appConfig.ngrDashboardUrl}/dashboard"))
  }
  // $COVERAGE-OFF$
  override def parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser

  override protected def executionContext: ExecutionContext = ec
  // $COVERAGE-ON$

}

@ImplementedBy(classOf[PropertyLinkingActionImpl])
trait PropertyLinkingAction extends ActionBuilder[AuthenticatedUserRequest, AnyContent] with ActionFunction[Request, AuthenticatedUserRequest]

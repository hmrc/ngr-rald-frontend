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

package uk.gov.hmrc.ngrraldfrontend.connectors

import play.api.http.Status.CREATED
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.*
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.{PropertyLinkingUserAnswers, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.vmvProperty.VMVProperty

import java.net.URL
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NGRConnector @Inject()(http: HttpClientV2,
                             appConfig: AppConfig
                            )
                            (implicit ec: ExecutionContext) {

  private def url(path: String): URL = url"${appConfig.nextGenerationRatesHost}/next-generation-rates/$path"

  def getPropertyLinkingUserAnswers()(implicit hc: HeaderCarrier): Future[Option[PropertyLinkingUserAnswers]] = {
    http.get(url("get-property-linking-user-answers"))
      .execute[Option[PropertyLinkingUserAnswers]]
  }

  def getLinkedProperty(credId: CredId)(implicit hc: HeaderCarrier): Future[Option[VMVProperty]] = {
    getPropertyLinkingUserAnswers()
      .map {
        case Some(propertyLinkingUserAnswers) => Some(propertyLinkingUserAnswers.vmvProperty)
        case None => None
      }
  }

  def upsertRaldUserAnswers(model: UserAnswers)(implicit hc: HeaderCarrier):  Future[HttpResponse] = {
    http.post(url("upsert-rald-user-answers"))
      .withBody(Json.toJson(model))
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case CREATED => response
          case _ => throw new Exception(s"${response.status}: ${response.body}")
        }
      }
  }

  def getRaldUserAnswers(credId: CredId)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]] = {
    implicit val rds: HttpReads[UserAnswers] = readFromJson
    http.get(url("get-rald-user-answers"))
      .withBody(Json.toJson(credId))
      .execute[Option[UserAnswers]]
  }
}

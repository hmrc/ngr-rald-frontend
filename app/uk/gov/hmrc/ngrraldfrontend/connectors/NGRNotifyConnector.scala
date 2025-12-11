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

import play.api.Logging
import play.api.http.Status.ACCEPTED
import play.api.libs.json.Json
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.UserAnswers
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import java.net.URL
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NGRNotifyConnector @Inject()(http: HttpClientV2,
                                   appConfig: AppConfig,
                                  )(implicit ec: ExecutionContext) extends Logging {
  private val headers = Map(
    HeaderNames.CONTENT_TYPE -> "application/json"
  )

  private def url(path: String, assessmentId: String): URL = url"${appConfig.nextGenerationRatesNotifyUrl}/ngr-notify/$path/$assessmentId"

  def postRaldChanges(answers: UserAnswers, assessmentId: String)(implicit hc: HeaderCarrier): Future[Int] = {
    if (appConfig.features.bridgeEndpointEnabled()) {
      http.post(url("rald", assessmentId))
        .withBody(Json.toJson(answers.data))
        .setHeader(headers.toSeq*)
        .execute[HttpResponse]
        .map(_.status)
    } else {
      Future.successful(ACCEPTED)
    }
  }

}

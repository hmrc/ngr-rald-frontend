/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.ngrraldfrontend.services

import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.audit.ExtendedAuditModel
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuditingService@Inject()(
                                appConfig: AppConfig,
                                auditConnector: AuditConnector
                              ) extends Logging {

  def extendedAudit(auditModel: ExtendedAuditModel, path: String = "-")
                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Unit = {
    val event = toExtendedDataEvent(appConfig.appName, auditModel, path)
    logger.debug(s"Splunk Audit Event:\n\n$event")
    handleAuditResult(auditConnector.sendExtendedEvent(event))
  }

  def toExtendedDataEvent(appName: String, auditModel: ExtendedAuditModel, path: String)
                         (implicit hc: HeaderCarrier): ExtendedDataEvent = {
    ExtendedDataEvent(
      auditSource = appName,
      auditType   = auditModel.auditType,
      tags        = AuditExtensions.auditHeaderCarrier(hc).toAuditTags(path),
      detail      = Json.toJson(auditModel.detail)
    )
  }

  //$COVERAGE-OFF$ Disabling scoverage as returns Unit, only used for Debug messages

  private def handleAuditResult(auditResult: Future[AuditResult])(implicit ec: ExecutionContext): Unit =
    auditResult.map {
      case AuditResult.Success =>
        logger.debug("Splunk Audit Successful")
      case AuditResult.Failure(err, _) =>
        logger.debug(s"Splunk Audit Error, message: $err")
      case AuditResult.Disabled =>
        logger.debug("Auditing Disabled")
    }

  //$COVERAGE-ON$
}

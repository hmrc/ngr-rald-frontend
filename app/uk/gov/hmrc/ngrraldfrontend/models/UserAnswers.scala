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

package uk.gov.hmrc.ngrraldfrontend.models

import play.api.libs.json.*
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.pages.DeclarationPage
import uk.gov.hmrc.ngrraldfrontend.queries.{Gettable, Settable}

import java.time.Instant
import scala.util.{Failure, Success, Try}

final case class UserAnswers(
                              credId: CredId,
                              data: JsObject = Json.obj(),
                              lastUpdated: Instant = Instant.now
                            ) {

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path)).reads(data).getOrElse(None)

  def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {

    val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors) =>
        Failure(JsResultException(errors))
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy (data = d)
        page.cleanup(Some(value), updatedAnswers)
    }
  }

  def remove[A](page: Settable[A])(implicit writes: Writes[A]): Try[UserAnswers] = {
    val updatedData = if data.keys.contains(page.toString) then
      data.removeObject(page.path) match {
        case JsSuccess(jsValue, _) =>
          Success(jsValue)
        case JsError(errors) =>
          Failure(JsResultException(errors))
      }
    else
      Success(data)

    updatedData.flatMap {
      d =>
        println(Console.MAGENTA + d + Console.RESET)
        val updatedAnswers = copy(data = d, lastUpdated = Instant.now)
        page.cleanup(None, updatedAnswers)
    }
  }

  def getCurrentJourneyUserAnswers[A](page: Gettable[A], userAnswers: UserAnswers, credId: String)(implicit rds: Reads[A]): UserAnswers =
    userAnswers.get(page) match
      case Some(_) =>
        if (userAnswers.get(DeclarationPage).isDefined) UserAnswers(CredId(credId)) else userAnswers
      case _ => UserAnswers(CredId(credId))
}

object UserAnswers {

  val reads: Reads[UserAnswers] = {

    import play.api.libs.functional.syntax.*

    (
      (__ \ "credId").read[CredId] and
      (__ \ "data").read[JsObject] and
      (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
    ) (UserAnswers.apply)
  }

  val writes: OWrites[UserAnswers] = {

    import play.api.libs.functional.syntax.*

    (
      (__ \ "credId").write[CredId] and
      (__ \ "data").write[JsObject] and
      (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
    ) (ua => (ua.credId, ua.data, ua.lastUpdated))
  }

  implicit val format: OFormat[UserAnswers] = OFormat(reads, writes)
}

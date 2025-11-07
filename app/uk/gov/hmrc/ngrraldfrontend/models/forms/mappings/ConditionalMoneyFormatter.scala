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

package uk.gov.hmrc.ngrraldfrontend.models.forms.mappings

import play.api.data.FormError
import play.api.data.format.Formatter

import scala.math.BigDecimal.RoundingMode.HALF_UP
import scala.util.Try
import scala.util.matching.Regex

class ConditionalMoneyFormatter(errorKeyPrefix: String, requiredOnCondition: Map[String, String] => Boolean)
  extends Formatter[Option[BigDecimal]]:

  private val amountRegex: Regex = """^\d+\.?\d{0,4}$""".r
  private val maxAmount: BigDecimal = BigDecimal("9999999.99")

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[BigDecimal]] =
    val isRequired = requiredOnCondition(data)
    data.get(key) match {
      case None if isRequired => oneError(key, "required.error")
      case Some(s) if isRequired => validateMoney(key, s.trim.replaceAll("[£,\\s]", ""))
      case _ => Right(None)
    }

  override def unbind(key: String, value: Option[BigDecimal]): Map[String, String] =
    Map(key -> value.fold("")(_.toString))

  private def oneError(key: String, errorTypeKey: String): Left[Seq[FormError], Option[BigDecimal]] =
    Left(Seq(FormError(key, s"$errorKeyPrefix.$errorTypeKey")))

  private def validateMoney(key: String, amount: String): Either[Seq[FormError], Option[BigDecimal]] =
    if amount.isEmpty then
      oneError(key, "required.error")
    else if !amountRegex.matches(amount) then
      oneError(key, "invalid.error")
    else
      Try(BigDecimal(amount).setScale(2, HALF_UP)).map { bigDecimal =>
        if bigDecimal > maxAmount then oneError(key, "max.error")
        else Right(Some(bigDecimal))
      }.getOrElse(oneError(key, "invalid.error"))

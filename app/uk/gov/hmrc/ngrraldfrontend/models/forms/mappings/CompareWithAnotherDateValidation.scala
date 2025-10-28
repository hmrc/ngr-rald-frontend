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

import java.time.LocalDate

/**
 * @author Yuriy Tumakha
 */
case class CompareWithAnotherDateValidation(errorTypeKey: String, anotherDateField: String, validate: (LocalDate, LocalDate) => Boolean) extends DateValidation:

  private val localDateFormatter: LocalDateFormatter = LocalDateFormatter(anotherDateField)

  private def bindLocalDate(fieldKey: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] =
    localDateFormatter.bind(fieldKey, data)

  def validateDateAndData(date: LocalDate, data: Map[String, String]): Boolean =
    bindLocalDate(anotherDateField, data).map { anotherDate =>
      validate.apply(date, anotherDate)
    }.getOrElse(true)

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

import models.Enumerable
import play.api.data.FieldMapping
import play.api.data.Forms.of
import play.api.data.validation.{Constraint, Valid, ValidationResult}
import uk.gov.hmrc.ngrraldfrontend.models.forms.CommonFormValidators

trait Mappings extends CommonFormValidators with Formatters{
  protected def radioText(errorKey: String = "error.required", args: Seq[String] = Seq.empty): FieldMapping[String] =
    of(stringFormatter(errorKey, args))

  protected def optionalRadioText(errorKey: String = "error.required", isOptional: Boolean, args: Seq[String] = Seq.empty): FieldMapping[String] =
    if (!isOptional)
      of(stringFormatter(errorKey, args))
    else
      of(optionalStringFormatter(args))

  def constraint[A](f: A => ValidationResult): Constraint[A] = Constraint[A]("")(f)

  implicit class ConstraintUtil[A](cons: Constraint[A]) {

    def andThen(newCons: Constraint[A]): Constraint[A] =
      constraint((data: A) =>
        cons.apply(data) match {
          case Valid => newCons.apply(data)
          case r => r
        }
      )
  }

  protected def enumerable[A](requiredKey: String = "error.required",
                              invalidKey: String = "error.invalid",
                              args: Seq[String] = Seq.empty)(implicit ev: Enumerable[A]): FieldMapping[A] =
    of(enumerableFormatter[A](requiredKey, invalidKey, args))
}

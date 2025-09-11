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

package uk.gov.hmrc.ngrraldfrontend.viewmodels

import org.mockito.Mockito.when
import play.api.data.{Form, FormError}
import uk.gov.hmrc.ngrraldfrontend.helpers.TestSupport
import uk.gov.hmrc.ngrraldfrontend.viewmodels.govuk.all.ErrorSummaryViewModel


class ErrorSummaryViewModelSpec extends TestSupport {
  val fieldName1 = "agreedDate"
  val fieldName2 = "startPayingDate"
  val pageName = "rentDatesAgreeStart"
  val form = mock[Form[_]]

  "ErrorSummaryViewModel" must {
    "when date is missing, day should be the key" in {
      when(form.data).thenReturn(Map(s"$fieldName1.day" -> "",
        s"$fieldName1.month" -> "",
        s"$fieldName1.year" -> "",
        s"$fieldName2.day" -> "",
        s"$fieldName2.month" -> "",
        s"$fieldName2.year" -> ""))
      when(form.errors).thenReturn(Seq(FormError(fieldName1, s"$pageName.$fieldName1.required.error"), FormError(fieldName2, s"$pageName.$fieldName2.required.error")))
      val actual = ErrorSummaryViewModel.apply(form, Some(pageName), Some(Seq(fieldName1, fieldName2)))
      actual.errorList.size mustBe 2
      actual.errorList.map(_.href).toList mustBe List(Some(s"#$fieldName1.day"), Some(s"#$fieldName2.day"))
    }
    "when day is invalid, day should be the key" in {
      when(form.data).thenReturn(Map(s"$fieldName1.day" -> "30",
        s"$fieldName1.month" -> "2",
        s"$fieldName1.year" -> "2025",
        s"$fieldName2.day" -> "AS",
        s"$fieldName2.month" -> "12",
        s"$fieldName2.year" -> "2025"))
      when(form.errors).thenReturn(Seq(FormError(fieldName1, s"$pageName.$fieldName1.invalid.error"), FormError(fieldName2, s"$pageName.$fieldName2.invalid.error")))
      val actual = ErrorSummaryViewModel.apply(form, Some(pageName), Some(Seq(fieldName1, fieldName2)))
      actual.errorList.size mustBe 2
      actual.errorList.map(_.href).toList mustBe List(Some(s"#$fieldName1.day"), Some(s"#$fieldName2.day"))
    }
    "when month is invalid, month should be the key" in {
      when(form.data).thenReturn(Map(s"$fieldName1.day" -> "50",
        s"$fieldName1.month" -> "AS",
        s"$fieldName1.year" -> "2025",
        s"$fieldName2.day" -> "12",
        s"$fieldName2.month" -> "30",
        s"$fieldName2.year" -> "2025"))
      when(form.errors).thenReturn(Seq(FormError(fieldName1, s"$pageName.$fieldName1.invalid.error"), FormError(fieldName2, s"$pageName.$fieldName2.invalid.error")))
      val actual = ErrorSummaryViewModel.apply(form, Some(pageName), Some(Seq(fieldName1, fieldName2)))
      actual.errorList.size mustBe 2
      actual.errorList.map(_.href).toList mustBe List(Some(s"#$fieldName1.month"), Some(s"#$fieldName2.month"))
    }
    "when year is invalid, year should be the key" in {
      when(form.data).thenReturn(Map(s"$fieldName1.day" -> "28",
        s"$fieldName1.month" -> "2",
        s"$fieldName1.year" -> "20250",
        s"$fieldName2.day" -> "12",
        s"$fieldName2.month" -> "12",
        s"$fieldName2.year" -> "12025"))
      when(form.errors).thenReturn(Seq(FormError(fieldName1, s"$pageName.$fieldName1.invalid.error"), FormError(fieldName2, s"$pageName.$fieldName2.invalid.error")))
      val actual = ErrorSummaryViewModel.apply(form, Some(pageName), Some(Seq(fieldName1, fieldName2)))
      actual.errorList.size mustBe 2
      actual.errorList.map(_.href).toList mustBe List(Some(s"#$fieldName1.year"), Some(s"#$fieldName2.year"))
    }
    "when day and month are missing, day should be the key" in {
      when(form.data).thenReturn(Map(s"$fieldName1.day" -> "",
        s"$fieldName1.month" -> "",
        s"$fieldName1.year" -> "2025",
        s"$fieldName2.day" -> "",
        s"$fieldName2.month" -> "",
        s"$fieldName2.year" -> "2025"))
      when(form.errors).thenReturn(Seq(FormError(fieldName1, s"$pageName.$fieldName1.dayAndMonth.required.error"), FormError(fieldName2, s"$pageName.$fieldName2.dayAndMonth.required.error")))
      val actual = ErrorSummaryViewModel.apply(form, Some(pageName), Some(Seq(fieldName1, fieldName2)))
      actual.errorList.size mustBe 2
      actual.errorList.map(_.href).toList mustBe List(Some(s"#$fieldName1.day"), Some(s"#$fieldName2.day"))
    }
    "when day and year are missing, day should be the key" in {
      when(form.data).thenReturn(Map(s"$fieldName1.day" -> "",
        s"$fieldName1.month" -> "1",
        s"$fieldName1.year" -> "",
        s"$fieldName2.day" -> "",
        s"$fieldName2.month" -> "12",
        s"$fieldName2.year" -> ""))
      when(form.errors).thenReturn(Seq(FormError(fieldName1, s"$pageName.$fieldName1.dayAndYear.required.error"), FormError(fieldName2, s"$pageName.$fieldName2.dayAndYear.required.error")))
      val actual = ErrorSummaryViewModel.apply(form, Some(pageName), Some(Seq(fieldName1, fieldName2)))
      actual.errorList.size mustBe 2
      actual.errorList.map(_.href).toList mustBe List(Some(s"#$fieldName1.day"), Some(s"#$fieldName2.day"))
    }
    "when month and year are missing, month should be the key" in {
      when(form.data).thenReturn(Map(s"$fieldName1.day" -> "1",
        s"$fieldName1.month" -> "",
        s"$fieldName1.year" -> "",
        s"$fieldName2.day" -> "1",
        s"$fieldName2.month" -> "",
        s"$fieldName2.year" -> ""))
      when(form.errors).thenReturn(Seq(FormError(fieldName1, s"$pageName.$fieldName1.monthAndYear.required.error"), FormError(fieldName2, s"$pageName.$fieldName2.monthAndYear.required.error")))
      val actual = ErrorSummaryViewModel.apply(form, Some(pageName), Some(Seq(fieldName1, fieldName2)))
      actual.errorList.size mustBe 2
      actual.errorList.map(_.href).toList mustBe List(Some(s"#$fieldName1.month"), Some(s"#$fieldName2.month"))
    }
    "when no errors matches, the key shouldn't be changed" in {
      when(form.data).thenReturn(Map(s"$fieldName1.day" -> "1",
        s"$fieldName1.month" -> "3",
        s"$fieldName1.year" -> "",
        s"$fieldName2.day" -> "1",
        s"$fieldName2.month" -> "12",
        s"$fieldName2.year" -> ""))
      when(form.errors).thenReturn(Seq(FormError(fieldName1, s"$pageName.$fieldName1.day.required.error"), FormError(fieldName2, s"$pageName.$fieldName2.day.required.error")))
      val actual = ErrorSummaryViewModel.apply(form, Some(pageName), Some(Seq(fieldName1, fieldName2)))
      actual.errorList.size mustBe 2
      actual.errorList.map(_.href).toList mustBe List(Some(s"#$fieldName1"), Some(s"#$fieldName2"))
    }
  }
}
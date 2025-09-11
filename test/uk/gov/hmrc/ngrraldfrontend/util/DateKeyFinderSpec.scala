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

package uk.gov.hmrc.ngrraldfrontend.util

import play.api.data.FormError
import uk.gov.hmrc.ngrraldfrontend.helpers.TestSupport
import uk.gov.hmrc.ngrraldfrontend.utils.DateKeyFinder


class DateKeyFinderSpec extends TestSupport with DateKeyFinder {

  "method findKey" must {
    val fieldName = "agreedDate"
    "February max day is 28 in 2025 but user enter 29 and it should return day as the key" in {
      val actual = findKey(
        Map(s"$fieldName.day" -> "29",
          s"$fieldName.month" -> "2",
          s"$fieldName.year" -> "2025"),
        fieldName
      )
      actual mustBe "agreedDate.day"
    }

    "February max day is 29 in 2000 but user enter 30 and it should return day as the key" in {
      val actual = findKey(
        Map(s"$fieldName.day" -> "30",
          s"$fieldName.month" -> "2",
          s"$fieldName.year" -> "2000"),
        fieldName
      )
      actual mustBe "agreedDate.day"
    }

    "April max day is 30 but user enter 31 and it should return day as the key" in {
      val actual = findKey(
        Map(s"$fieldName.day" -> "31",
          s"$fieldName.month" -> "4",
          s"$fieldName.year" -> "2025"),
        fieldName
      )
      actual mustBe "agreedDate.day"
    }

    "September max day is 30 but user enter 31 and it should return day as the key" in {
      val actual = findKey(
        Map(s"$fieldName.day" -> "31",
          s"$fieldName.month" -> "9",
          s"$fieldName.year" -> "2025"),
        fieldName
      )
      actual mustBe "agreedDate.day"
    }

    "User enter anything other than digits in day and it should return day as the key" in {
      val actual = findKey(
        Map(s"$fieldName.day" -> "@'{}",
          s"$fieldName.month" -> "12",
          s"$fieldName.year" -> "2025"),
        fieldName
      )
      actual mustBe "agreedDate.day"
    }

    "User enter 13 in month and it should return month as the key" in {
      val actual = findKey(
        Map(s"$fieldName.day" -> "31",
          s"$fieldName.month" -> "13",
          s"$fieldName.year" -> "2025"),
        fieldName
      )
      actual mustBe "agreedDate.month"
    }

    "User enter anything other than digits in month and it should return month as the key" in {
      val actual = findKey(
        Map(s"$fieldName.day" -> "50",
          s"$fieldName.month" -> "Dec",
          s"$fieldName.year" -> "2025"),
        fieldName
      )
      actual mustBe "agreedDate.month"
    }

    "User enter 5 digits in year and it should return year as the key" in {
      val actual = findKey(
        Map(s"$fieldName.day" -> "31",
          s"$fieldName.month" -> "12",
          s"$fieldName.year" -> "20250"),
        fieldName
      )
      actual mustBe "agreedDate.year"
    }

    "User enter day and month start with 0 and 5 digits for the year it should return year as the key" in {
      val actual = findKey(
        Map(s"$fieldName.day" -> "05",
          s"$fieldName.month" -> "03",
          s"$fieldName.year" -> "20250"),
        fieldName
      )
      actual mustBe "agreedDate.year"
    }
  }
  
  "method setCorrectKey" must {
    val pageName = "RentDatesAgreeStart"
    val fieldName = "agreedDate"
    "When day is missing the key should set to day" in {
      val formError = FormError.apply(fieldName, s"$pageName.$fieldName.day.required.error")
      val actual = setCorrectKey(formError, pageName, fieldName)
      actual mustBe formError.copy(key = s"$fieldName.day")
    }

    "When month is missing the key should set to month" in {
      val formError = FormError.apply(fieldName, s"$pageName.$fieldName.month.required.error")
      val actual = setCorrectKey(formError, pageName, fieldName)
      actual mustBe formError.copy(key = s"$fieldName.month")
    }

    "When year is missing the key should set to year" in {
      val formError = FormError.apply(fieldName, s"$pageName.$fieldName.year.required.error")
      val actual = setCorrectKey(formError, pageName, fieldName)
      actual mustBe formError.copy(key = s"$fieldName.year")
    }

    "When date is before 1900 the key should set to year" in {
      val formError = FormError.apply(fieldName, s"$pageName.$fieldName.before.1900.error")
      val actual = setCorrectKey(formError, pageName, fieldName)
      actual mustBe formError.copy(key = s"$fieldName.year")
    }
  }
}
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

package uk.gov.hmrc.ngrraldfrontend.models.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.govukfrontend.views.Aliases.{ErrorMessage, Hint, Label, Text}

class NGRCharacterCountSpec extends AnyFlatSpec with Matchers {

  "NGRCharacterCount" should "create a default instance with empty and None values" in {
    val defaultCharCount = NGRCharacterCount()

    defaultCharCount.id shouldBe ""
    defaultCharCount.name shouldBe ""
    defaultCharCount.maxWords shouldBe None
    defaultCharCount.label shouldBe Label()
    defaultCharCount.hint shouldBe None
    defaultCharCount.errorMessage shouldBe None
  }

  it should "store provided values correctly" in {
    val label = Label(content = Text("Test Label"))
    val hint = Some(Hint(content = Text("This is a hint")))
    val errorMessage = Some(ErrorMessage( content = Text("Error occurred")))

    val charCount = NGRCharacterCount(
      id = "123",
      name = "Character Count",
      maxWords = Some(100),
      label = label,
      hint = hint,
      errorMessage = errorMessage
    )

    charCount.id shouldBe "123"
    charCount.name shouldBe "Character Count"
    charCount.maxWords shouldBe Some(100)
    charCount.label shouldBe label
    charCount.hint shouldBe hint
    charCount.errorMessage shouldBe errorMessage
  }

  it should "handle None values gracefully" in {
    val charCount = NGRCharacterCount(
      id = "456",
      name = "Test",
      maxWords = None,
      label = Label(content = Text("Label")),
      hint = None,
      errorMessage = None
    )

    charCount.maxWords shouldBe None
    charCount.hint shouldBe None
    charCount.errorMessage shouldBe None
  }
}


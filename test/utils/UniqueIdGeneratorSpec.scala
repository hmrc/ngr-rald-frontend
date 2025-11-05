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

package utils

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import utils.UniqueIdGenerator

class UniqueIdGeneratorSpec extends AnyFreeSpec with Matchers {

  private val allowedChars = UniqueIdGenerator.allowedChars

  "UniqueIdGenerator" - {

    "generate a 12-char ID with 2 hyphens in correct format" in {
      val id = UniqueIdGenerator.generateId
      id.length mustBe 14
      id.count(_ == '-') mustBe 2

      val compactId = UniqueIdGenerator.parse(id)
      compactId.length mustBe 12
      compactId.forall(allowedChars.contains(_)) mustBe true
      val formatted = UniqueIdGenerator.format(compactId)
      formatted mustBe id
    }

    "validate good IDs" in {
      val validIds = List(
        "fdfd-fdfd-dfdf",
        "VDJ4-5NSG-8RHW",
        "BDJ6867MLMNE",
        "nvjf5245bsmv"
      )

      validIds.foreach { id =>
        withClue(s"Expected '$id' to be valid: ") {
          UniqueIdGenerator.validateId(id).isRight mustBe true
        }
      }
    }

    "invalidate bad IDs" in {
      val invalidIds = List(
        "0FDE-DFD1-DGJ1",
        "0efkdkfvncma",
        "hello",
        "&fdh-9adf-4jnf",
        "ABCD-EFGH-IJKLM",
        "ABCD-EFGH-IJ1M",
        "ABCD-EFGH-IJOM"
      )

      invalidIds.foreach { id =>
        withClue(s"Expected '$id' to be invalid: ") {
          UniqueIdGenerator.validateId(id).isLeft mustBe true
        }
      }
    }

    "format raw reference correctly" in {
      val raw = "7GQX2MZKJH9B"
      val formatted = UniqueIdGenerator.format(raw)
      formatted mustBe "7GQX-2MZK-JH9B"
    }

    "parse formatted reference back to raw" in {
      val formatted = "7GQX-2MZK-JH9B"
      val raw = UniqueIdGenerator.parse(formatted)
      raw mustBe "7GQX2MZKJH9B"
    }

    "round-trip format and parse should preserve original raw reference" in {
      val raw = "N8V3W5Y2X4ZT"
      val formatted = UniqueIdGenerator.format(raw)
      val parsed = UniqueIdGenerator.parse(formatted)
      parsed mustBe raw
    }
  }
}

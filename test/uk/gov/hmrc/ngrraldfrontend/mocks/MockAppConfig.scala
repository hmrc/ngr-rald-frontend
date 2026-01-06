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

package uk.gov.hmrc.ngrraldfrontend.mocks

import play.api.Configuration
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.config.features.Features

class MockAppConfig(val runModeConfiguration: Configuration) extends AppConfig {
  override val appName: String = "ngr-rald-frontend"
  override val features: Features = new Features()(runModeConfiguration)
  override val nextGenerationRatesHost: String = "https://localhost:1500"
  override val ngrLoginRegistrationHost: String = "https://localhost:1502"
  override val ngrDashboardUrl: String = "http://localhost:1503/ngr-dashboard-frontend/dashboard"
  override val ngrCheckYourDetailsUrl: String = "http://localhost:1503/ngr-dashboard-frontend/check-details"
  override val ngrLogoutUrl: String = "http://localhost:1503/ngr-dashboard-frontend/signout"
  override def getString(key: String): String = ""
  override val cacheTtl: Long = 8
  override val nextGenerationRatesNotifyUrl: String = "https://localhost:1515"
}


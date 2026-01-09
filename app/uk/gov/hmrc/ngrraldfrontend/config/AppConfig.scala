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

package uk.gov.hmrc.ngrraldfrontend.config

import play.api.Configuration
import uk.gov.hmrc.ngrraldfrontend.config.features.Features
import uk.gov.hmrc.ngrraldfrontend.controllers.routes
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

trait AppConfig {
  val features: Features
  val nextGenerationRatesHost: String
  val ngrLoginRegistrationHost: String
  val ngrDashboardUrl: String
  val ngrCheckYourDetailsUrl: String
  val ngrLogoutUrl: String
  def getString(key: String): String
  val cacheTtl: Long
  val nextGenerationRatesNotifyUrl: String
  val logoutUrl: String
  val feedbackFrontendUrl: String
  val timeout: Int
  val countdown: Int
}

@Singleton
class FrontendAppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) extends AppConfig {
  override val features = new Features()(config)
  override val nextGenerationRatesHost: String = servicesConfig.baseUrl("next-generation-rates")
  override val ngrLoginRegistrationHost: String = servicesConfig.baseUrl("ngr-login-register-frontend")
  override val ngrDashboardUrl: String = s"$dashboardHost/ngr-dashboard-frontend/dashboard"
  override val ngrCheckYourDetailsUrl: String = s"$dashboardHost/ngr-dashboard-frontend/check-your-details"
  override val ngrLogoutUrl: String = s"$dashboardHost/ngr-dashboard-frontend/signout"
  override val cacheTtl: Long = config.get[Int]("mongodb.timeToLiveInSeconds")
  override val nextGenerationRatesNotifyUrl: String = servicesConfig.baseUrl("ngr-notify")
  override val timeout: Int = config.get[Int]("timeout-dialog.timeout")
  override val countdown: Int = config.get[Int]("timeout-dialog.countdown")

  def getString(key: String): String =
    config.getOptional[String](key).filter(!_.isBlank).getOrElse(throwConfigNotFoundError(key))

  private def throwConfigNotFoundError(key: String): String =
    throw new RuntimeException(s"Could not find config key '$key'")

  lazy val dashboardHost: String = getString("microservice.services.ngr-dashboard-frontend.host")

  private lazy val basGatewayHost = getString("microservice.services.bas-gateway-frontend.host")
  private lazy val envHost = getString("environment.host")
  private lazy val registrationBeforeYouGoUrl: String = s"$envHost/ngr-rald-frontend${routes.BeforeYouGoController.show.url}"
  private lazy val feedbackFrontendHost = getString("microservice.services.feedback-survey-frontend.host")

  override val logoutUrl: String = s"$basGatewayHost/bas-gateway/sign-out-without-state?continue=$registrationBeforeYouGoUrl"
  override val feedbackFrontendUrl: String = s"$feedbackFrontendHost/feedback/NGR-Rald"
}

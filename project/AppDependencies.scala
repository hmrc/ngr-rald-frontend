import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.19.0"
  private val hmrcMongoVersion = "2.7.0"
  private val enumeratumVersion = "1.9.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-30"                        % bootstrapVersion,
    "uk.gov.hmrc"             %% "play-frontend-hmrc-play-30"                        % "12.17.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-30"                                % hmrcMongoVersion,
    "uk.gov.hmrc"             %% "centralised-authorisation-resource-client-play-30" % "1.12.0",
    "com.beachape"            %% "enumeratum-play"                                   % enumeratumVersion,
    "uk.gov.hmrc"             %% "domain-play-30"                                    % "11.0.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapVersion            % Test,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"    % hmrcMongoVersion            % Test,
    "org.jsoup"               %  "jsoup"                      % "1.21.1"                    % Test
  )


  val it = Seq.empty
}

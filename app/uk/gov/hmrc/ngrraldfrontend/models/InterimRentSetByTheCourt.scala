package uk.gov.hmrc.ngrraldfrontend.models

import play.api.libs.json.{Json, OFormat}

case class InterimRentSetByTheCourt(amount: String,
                                    date: String
                                   )

object InterimRentSetByTheCourt {
  implicit val format: OFormat[InterimRentSetByTheCourt] = Json.format[InterimRentSetByTheCourt]
}
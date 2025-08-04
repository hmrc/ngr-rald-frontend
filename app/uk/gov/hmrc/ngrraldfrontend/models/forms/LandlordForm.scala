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

package uk.gov.hmrc.ngrraldfrontend.models.forms

import play.api.data.Form
import play.api.data.Forms.{mapping, optional, text}
import play.api.i18n.{DefaultMessagesApi, Lang, Messages, MessagesApi, MessagesImpl}
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ErrorMessage, Label, Text}
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.components.{BusinessPartnerOrSharedDirector, CompanyPensionFund, FamilyMember, LandLordAndTennant, NGRRadioButtons, OtherRelationship}

final case class LandlordForm(landlordName: String, landLordType: String, landlordOther: Option[String]) {
  override def toString: String = Seq(landlordName, landLordType ,landlordOther).mkString(",")
}

object LandlordForm extends CommonFormValidators  {
  implicit val format: OFormat[LandlordForm] = Json.format[LandlordForm]

  private lazy val landlordNameEmptyError = "landlord.name.empty.error"
  private lazy val radioUnselectedError = "whatTypeOfAgreement.error.required"

  private val landlord = "landlord-name-value"
  private val landlordRadio = "landlord-radio"
  private val landlordOther = "landlord-radio-other"


  val messagesApi: MessagesApi = new DefaultMessagesApi()
  val lang: Lang = Lang.defaultLang
  val messages: Messages = MessagesImpl(lang, messagesApi)

  def unapply(landlordForm: LandlordForm): Option[(String, String, Option[String])] = Some((landlordForm.landlordName, landlordForm.landLordType, landlordForm.landlordOther))
  def form: Form[LandlordForm] = {
    Form(
      mapping(
        landlord -> text()
          .verifying(
            firstError(
              isNotEmpty(landlord, landlordNameEmptyError)
            )
          ),
        landlordRadio -> text(),
        landlordOther -> optional(text)
      )(LandlordForm.apply)(LandlordForm.unapply)
    )
  }



  private val landLordAndTennant: NGRRadioButtons = NGRRadioButtons(radioContent = "landlord.radio1", radioValue = LandLordAndTennant)
  private val familyMember: NGRRadioButtons = NGRRadioButtons(radioContent = "landlord.radio2", radioValue = FamilyMember)
  private val companyPensionFund: NGRRadioButtons = NGRRadioButtons(radioContent = "landlord.radio3", radioValue = CompanyPensionFund)
  private val businessPartnerOrSharedDirector: NGRRadioButtons = NGRRadioButtons(radioContent = "landlord.radio4", radioValue = BusinessPartnerOrSharedDirector)
  def otherRelationship()(implicit messages: Messages): NGRRadioButtons = NGRRadioButtons(
    radioContent = "landlord.radio5",
    radioValue = OtherRelationship,
    conditionalHtml = Some(NGRCharacterCount.buildCharacterCount(
      form,
      ngrCharacterCount = NGRCharacterCount(
        id = "landlord-otherRelationship",
        name ="landlord-otherRelationship",
        label = Label(content = Text("landlord.radio5.dropdown"), classes = "govuk-label--m" , isPageHeading = true),
        errorMessage = Some(ErrorMessage(content = Text("")))))
    )
  )

  def ngrRadio(form: Form[LandlordForm])(implicit messages: Messages): NGRRadio =
    NGRRadio(
      NGRRadioName("landlord-radio"),
      ngrTitle = Some(NGRRadioHeader(title = "landlord.p2", classes = "govuk-label govuk-label--m",isPageHeading = true)),
      NGRRadioButtons = Seq(landLordAndTennant, familyMember, companyPensionFund, businessPartnerOrSharedDirector, otherRelationship())
    )

}

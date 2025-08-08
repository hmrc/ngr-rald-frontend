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

package uk.gov.hmrc.ngrraldfrontend.controllers

import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Label, Text}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, PropertyLinkingAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.components.NavBarPageContents.createDefaultNavBar
import uk.gov.hmrc.ngrraldfrontend.models.forms.LandlordForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.LandlordForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.repo.RaldRepo
import uk.gov.hmrc.ngrraldfrontend.views.html.LandlordView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.NGRCharacterCountComponent
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LandlordController @Inject()(view: LandlordView,
                                   authenticate: AuthRetrievals,
                                   hasLinkedProperties: PropertyLinkingAction,
                                   raldRepo: RaldRepo,
                                   ngrCharacterCountComponent: NGRCharacterCountComponent,
                                   mcc: MessagesControllerComponents
                                  )(implicit appConfig: AppConfig, ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  def otherRelationship(form: Form[LandlordForm])(implicit messages: Messages): NGRRadioButtons = NGRRadioButtons(
    radioContent = "landlord.radio5",
    radioValue = OtherRelationship,
    conditionalHtml = Some(ngrCharacterCountComponent(form,
      NGRCharacterCount(
        id = "landlord-radio-other",
        name = "landlord-radio-other",
        maxLength = Some(250),
        label = Label(
          classes = "govuk-label govuk-label--m",
          content = Text(Messages("landlord.radio5.dropdown"))
        )
      )))
  )

  def ngrRadio(form: Form[LandlordForm])(implicit messages: Messages): NGRRadio =
    val landLordAndTenant: NGRRadioButtons = NGRRadioButtons(radioContent = "landlord.radio1", radioValue = LandLordAndTenant)
    val familyMember: NGRRadioButtons = NGRRadioButtons(radioContent = "landlord.radio2", radioValue = FamilyMember)
    val companyPensionFund: NGRRadioButtons = NGRRadioButtons(radioContent = "landlord.radio3", radioValue = CompanyPensionFund)
    val businessPartnerOrSharedDirector: NGRRadioButtons = NGRRadioButtons(radioContent = "landlord.radio4", radioValue = BusinessPartnerOrSharedDirector)
    NGRRadio(
      NGRRadioName("landlord-radio"),
      ngrTitle = Some(NGRRadioHeader(title = "landlord.p2", classes = "govuk-label govuk-label--m", isPageHeading = true)),
      NGRRadioButtons = Seq(landLordAndTenant, familyMember, companyPensionFund, businessPartnerOrSharedDirector, otherRelationship(form))
    )

  def show: Action[AnyContent] = {
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      request.propertyLinking.map(property =>
        Future.successful(Ok(view(
          navigationBarContent = createDefaultNavBar,
          selectedPropertyAddress = property.addressFull,
          form,
          buildRadios(form, ngrRadio(form))
        )))
      ).getOrElse(throw new NotFoundException("Couldn't find property in mongo"))
    }
  }

  def submit: Action[AnyContent] = {
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>

            val correctedFormErrors = formWithErrors.errors.map { formError =>
              (formError.key, formError.messages) match
                case ("", messages) if messages.contains("landlord.radio.other.empty.error") =>
                  formError.copy(key = "landlord-radio-other")
                case _ =>
                  formError
            }
            val formWithCorrectedErrors = formWithErrors.copy(errors = correctedFormErrors)

            request.propertyLinking.map(property =>
              Future.successful(BadRequest(view(
                createDefaultNavBar,
                selectedPropertyAddress = property.addressFull,
                formWithCorrectedErrors,
                buildRadios(formWithErrors, ngrRadio(formWithCorrectedErrors))
              )))).getOrElse(throw new NotFoundException("Couldn't find property in mongo")),
          landlordForm =>
            raldRepo.insertLandlord(
              CredId(request.credId.getOrElse("")),
              landlordForm.landlordName,
              landlordForm.landLordType,
              landlordForm.landlordOther
            )
            Future.successful(Redirect(routes.WhatTypeOfAgreementController.show.url))
        )

    }
  }
}

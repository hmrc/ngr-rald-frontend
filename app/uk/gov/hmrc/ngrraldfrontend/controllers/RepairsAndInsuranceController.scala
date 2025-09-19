package uk.gov.hmrc.ngrraldfrontend.controllers

import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, PropertyLinkingAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.RepairsAndInsuranceForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.RepairsAndInsuranceForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.repo.RaldRepo
import uk.gov.hmrc.ngrraldfrontend.views.html.RepairsAndInsuranceView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RepairsAndInsuranceController @Inject()(repairsAndInsuranceView: RepairsAndInsuranceView,
                                              authenticate: AuthRetrievals,
                                              hasLinkedProperties: PropertyLinkingAction,
                                              raldRepo: RaldRepo,
                                              mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {


  def show: Action[AnyContent] = {
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      request.propertyLinking.map(property =>
        Future.successful(Ok(repairsAndInsuranceView(
          form = form,
          radios = buildRadios(form, RepairsAndInsuranceForm.ngrRadio(form)),
          propertyAddress = property.addressFull,
        )))).getOrElse(throw new NotFoundException("Couldn't find property in mongo"))
    }
  }

  def submit: Action[AnyContent] =
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          request.propertyLinking.map(property =>
            Future.successful(BadRequest(repairsAndInsuranceView(
              form = formWithErrors,
              radios = buildRadios(formWithErrors, RepairsAndInsuranceForm.ngrRadio(formWithErrors)),
              propertyAddress = property.addressFull
            )))).getOrElse(throw new NotFoundException("Couldn't find property in mongo"))
        },
        radioValue =>
          raldRepo.insertAgreedRentChange(
            credId = CredId(request.credId.getOrElse("")),
            agreedRentChange = radioValue.radioValue
          )
          if (radioValue.radioValue == "Yes") {
            Future.successful(Redirect(routes.InterimRentSetByTheCourtController.show.url))
          } else {
            Future.successful(Redirect(routes.CheckRentFreePeriodController.show.url))
          }
      )
    }
}



package uk.gov.hmrc.ngrraldfrontend.controllers

import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, contentAsString, redirectLocation, status}
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.Incentive.YesLumpSum
import uk.gov.hmrc.ngrraldfrontend.models.forms.DidYouGetIncentiveForNotTriggeringBreakClauseForm
import uk.gov.hmrc.ngrraldfrontend.models.{AuthenticatedUserRequest, DidYouGetIncentiveForNotTriggeringBreakClause, NormalMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.pages.{DidYouAgreeRentWithLandlordPage, DidYouGetIncentiveForNotTriggeringBreakClausePage}
import uk.gov.hmrc.ngrraldfrontend.views.html.DidYouGetIncentiveForNotTriggeringBreakClauseView

import scala.concurrent.Future

class DidYouGetIncentiveForNotTriggeringBreakClauseControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Did you get incentive for not triggering the break clause?"
  val formProvider = new DidYouGetIncentiveForNotTriggeringBreakClauseForm()
  val form: Form[DidYouGetIncentiveForNotTriggeringBreakClause] = formProvider()
  lazy val changeToUseOfSpaceRoute: String = routes.DidYouGetIncentiveForNotTriggeringBreakClauseController.show(NormalMode).url
  val changeToUseOfSpace: DidYouGetIncentiveForNotTriggeringBreakClause = DidYouGetIncentiveForNotTriggeringBreakClause(Set(YesLumpSum))
  val userAnswers: Option[UserAnswers] = UserAnswers.set(DidYouGetIncentiveForNotTriggeringBreakClausePage, Set(Yes))

  "Did you agree rent with landlord controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        val result = controllerProperty(None).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return OK and the correct view with prepopulated answers" in {
        val result = controllerProperty(didYouGetIncentiveForNotTriggeringBreakClauseAnswers).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("input[type=radio][name=did-you-agree-rent-with-landlord-radio][value=YesTheLandlord]").hasAttr("checked") mustBe true
        document.select("input[type=radio][name=did-you-agree-rent-with-landlord-radio][value=NoACourtSet]").hasAttr("checked") mustBe false
      }
      "Return NotFoundException when property is not found in the mongo" in {
        when(mockNGRConnector.getLinkedProperty(any[CredId])(any())).thenReturn(Future.successful(None))
        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.show(NormalMode)(authenticatedFakeRequest))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }

    "method submit" must {
      "Return OK and the correct view after submitting with YesTheLandlord radio button" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.DidYouAgreeRentWithLandlordController.submit(NormalMode))
          .withFormUrlEncodedBody(("did-you-agree-rent-with-landlord-radio", "YesTheLandlord"))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") shouldBe Some("/ngr-rald-frontend/do-you-have-a-rent-free-period")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CheckRentFreePeriodController.show(NormalMode).url)
      }
      "Return OK and the correct view after submitting with NoACourtSet radio button" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.DidYouAgreeRentWithLandlordController.submit(NormalMode))
          .withFormUrlEncodedBody(("did-you-agree-rent-with-landlord-radio", "NoACourtSet"))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") shouldBe Some("/ngr-rald-frontend/did-the-court-set-an-interim-rent")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.RentInterimController.show(NormalMode).url)
      }
      "Return Form with Errors when no radio button is selected" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.DidYouAgreeRentWithLandlordController.submit(NormalMode))
          .withFormUrlEncodedBody(("did-you-agree-rent-with-landlord-radio", ""))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") shouldBe Some("/ngr-rald-frontend/what-type-of-agreement-do-you-have ")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return Exception if no address is in the mongo" in {

        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.DidYouAgreeRentWithLandlordController.submit(NormalMode))
            .withFormUrlEncodedBody(("did-you-agree-rent-with-landlord-radio", ""))
            .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some("")))))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }
  }
}


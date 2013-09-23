package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import services.VirtualCss

object VirtualCssController extends Controller {

  def virtualCssJs(cssPath: String) = Action {
    Async {
      val jsFuture = VirtualCss.jsForCss(cssPath)
      jsFuture.map(jsStatementDefinitions => Ok(views.html.jsTemplate(jsStatementDefinitions._2, jsStatementDefinitions._1)))
    }
  }

  /*
    def virtualCss(cssPath: String) = Action {
      Async {
        Ok(VirtualCss.cssForCss(cssPath))
      }
    }*/
}

package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import services.VirtualCss

object VirtualCssController extends Controller {

  def virtualCssJs(cssPath: String) = Action {
    Async {
      val jsFuture = VirtualCss.jsForCss(cssPath)
      jsFuture.map(jsTextAndStatements => Ok(views.html.jsTemplate(jsTextAndStatements._2, jsTextAndStatements._1)).as("text/javascript"))
    }
  }

  def virtualCss(cssPath: String) = Action {
    Async {
      VirtualCss.cssForCss(cssPath).map(cssText => Ok(cssText).as("text/css"))
    }
  }
}

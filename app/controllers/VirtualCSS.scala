package controllers

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WS
import play.api.libs.ws.WS.WSRequestHolder
import scala.concurrent.Future
import scala.collection.mutable

object VirtualCSS extends Controller {

  def virtualCssJs(cssPath: String) = Action {
    Async {
      val jsFuture = getCssJs(cssPath)
      jsFuture.map(jsText => Ok(jsText))
    }
  }

  def getCssJs(cssPath: String) = {
    val cssTextFuture: Future[String] = getCssText(cssPath)
    cssTextFuture.map(processCssToJs)
  }

  def processCssToJs(css: String) = {
    val cssText: String = removeComments(css)
    val sb: StringBuilder = new mutable.StringBuilder()
    val rules = """(?m)(^[^\{]*)\{([^\}]*)\}""".r

    for (rules(selector, rule) <- rules findAllIn cssText) {
      for (line <- rule.split(";")) {
        sb.append(s"selector:${selector.trim} property:${line.trim}\n")
      }
    }
    sb.toString()
  }


  def removeComments(css: String): String = {
    val cssText: String = css.replaceAll( """(?s)/\*.*?\*/""", "")
    cssText
  }

  def getCssText(cssPath: String) = {
    val url: WSRequestHolder = WS.url(s"http://localhost:9000${routes.Assets.at(s"$cssPath").url}")
    url.get().map(response => response.body)
  }
}

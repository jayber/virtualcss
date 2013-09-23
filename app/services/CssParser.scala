package services

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WS.WSRequestHolder
import play.api.libs.ws.WS
import scala.concurrent.Future

object CssParser extends Parser {

  def loadVirtualCssDefinitions = {
    val virtualCssText: String = getFileText("virtualCss.css")
    loadCssPropertiesFromString(virtualCssText)
  }

  def loadCssPropertiesFromUrl(cssPath: String) = {
    val url: WSRequestHolder = WS.url(cssPath)
    val future: Future[String] = url.get().map(response => response.body)
    loadCssProperties(future)
  }

  def loadCssPropertiesFromString(cssText: String) = {
    loadCssProperties(Future(cssText))
  }

  def loadCssProperties(future: Future[String]) = {
    var selectorStylePairs: Map[String, List[(String, String)]] = Map()
    future.map(cssText => CssParser.parse(cssText) {
      (selector: String, property: String, value: String) =>
        selectorStylePairs += (property.trim -> ((selector.trim, value.trim) :: selectorStylePairs.getOrElse(property.trim, Nil)))
    }).map((Unit) => selectorStylePairs)
  }

  def parse(css: String)(onParsedFunction: (String, String, String) => Unit) = {
    val cssText: String = removeComments(css)
    val rules = """(?m)(^[^\{]*)\{([^\}]*)\}""".r
    val line = """([^:]*):(.*)""".r

    for (rules(selector, rule) <- rules findAllIn cssText) {
      for (line(property, value) <- rule.split(";")) {
        onParsedFunction(selector, property, value)
      }
    }
  }

  def removeComments(css: String): String = {
    css.replaceAll( """(?s)/\*.*?\*/""", "")
  }
}

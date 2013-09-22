package services

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WS.WSRequestHolder
import play.api.libs.ws.WS

object CssParser extends Parser {

  def loadVirtualCssDefinitions = {

    val csText: String = getFileText("virtualCss.css")
  }

  def loadCssProperties(cssPath: String) = {
    val url: WSRequestHolder = WS.url(cssPath)
    url.get().map(response => CssParser.parse(response.body))
  }

  def parse(css: String): Map[String, List[(String, String)]] = {
    val cssText: String = removeComments(css)
    val rules = """(?m)(^[^\{]*)\{([^\}]*)\}""".r
    val line = """([^:]*):(.*)""".r

    var selectorStylePairs: Map[String, List[(String, String)]] = Map()
    for (rules(selector, rule) <- rules findAllIn cssText) {
      for (line(property, value) <- rule.split(";")) {
        selectorStylePairs += (property.trim -> ((selector.trim, value.trim) :: selectorStylePairs.getOrElse(property.trim, Nil)))
      }
    }
    selectorStylePairs
  }

  def removeComments(css: String): String = {
    css.replaceAll( """(?s)/\*.*?\*/""", "")
  }
}

package services

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WS.WSRequestHolder
import play.api.libs.ws.WS
import scala.concurrent.Future
import play.api.Play.current
import play.api.Play

object CssParser extends Parser {

  val rules = """(?m)(^[^\{]*)\{([^\}]*)\}""".r
  val line = """([^:]*):(.*)""".r
  val commentRegex = """(?s)/\*.*?\*/"""

  val definitionsPath = Play.configuration.getString("virtualCssDefinitionsFileName")

  def loadVirtualCssDefinitions = {
    Future {
      val virtualCssText: String = getFileText(definitionsPath.get)
      val cssText: String = removeComments(virtualCssText)

      var definitions: Map[String, String] = Map()
      for (rules(selector, rule) <- rules findAllIn cssText) {
        definitions += (selector.trim -> rule)
      }
      definitions
    }
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
    future.map(cssText =>
      CssParser.parseByProperties(cssText) {
        (selector: String, property: String, value: String) =>
          selectorStylePairs += (property -> ((selector, value) :: selectorStylePairs.getOrElse(property.trim, Nil)))
      }).map((Unit) => selectorStylePairs)
  }

  def parseByProperties(css: String)(onParsedFunction: (String, String, String) => Unit) = {
    val cssText: String = removeComments(css)
    for (rules(selector, rule) <- rules findAllIn cssText) {
      for (line(property, value) <- rule.split(";")) {
        onParsedFunction(selector.trim, property.trim, value.trim)
      }
    }
  }

  def removeComments(css: String): String = {
    css.replaceAll(commentRegex, "")
  }
}

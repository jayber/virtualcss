package services

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import play.api.Play
import play.api.Play.current

object JsParser extends Parser {

  val definitionsPath = Play.configuration.getString("virtualCssJsDefinitionsFileName")

  def loadVirtualCssJsImplementations = {
    Future {
      val jsText: String = getFileText(definitionsPath.get)
      (jsText, JsParser.parse(jsText))
    }
  }

  def parse(css: String): Map[String, String] = {
    val cssText: String = removeComments(css)
    val block = """(?s).*?\{(.*)\}""".r
    val definition = """(?s)([^:]*?):([^\{]*?\{.*?\})(,|$)""".r

    var propertyFunctionPairs: Map[String, String] = Map()
    for (block(innerBlock) <- block findAllIn cssText) {
      for (definition(property, value, _) <- definition findAllIn innerBlock) {
        propertyFunctionPairs += property.trim -> value.trim
      }
    }
    propertyFunctionPairs
  }

  def removeComments(css: String): String = {
    css.replaceAll( """(?s)/\*.*?\*/""", "")
    css.replaceAll( """(?s)//.*?\$""", "")
  }
}

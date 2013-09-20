package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import java.io.InputStream
import scala.io.Source
import scala.concurrent.Future

object JsParser {

  def loadVirtualCssDefinitions = {
    val inputStream: InputStream = this.getClass.getClassLoader.getResourceAsStream("public/javascripts/virtualProperties.js")
    val jsonText: String = try {
      Source.fromInputStream(inputStream, "utf-8").mkString("")
    }
    finally inputStream.close()

    (jsonText, Future(JsParser.parse(jsonText)))
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

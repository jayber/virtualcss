package controllers

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WS
import scala.concurrent.Future
import java.io.InputStream
import scala._
import play.api.libs.ws.WS.WSRequestHolder
import scala.io.Source
import play.api.libs.json.Json

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
    val virtualPropsMap = parseCSS(css)


    val inputStream: InputStream = this.getClass.getClassLoader.getResourceAsStream("virtualProperties.JSON")
    val jsonText: String = try {
      Source.fromInputStream(inputStream, "utf-8").mkString("")
    }
    finally inputStream.close()

    val json = Json.parse(jsonText)

    json for
      virtualPropsMap.filterKeys(.as).map(that => map(that._1))

  }

  def parseCSS(css: String): Map[String, List[(String, String)]] = {
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

  def getCssText(cssPath: String) = {
    val url: WSRequestHolder = WS.url(cssPath)
    url.get().map(response => response.body)
  }
}

package controllers

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WS
import scala.concurrent.Future
import java.io.InputStream
import scala._
import play.api.libs.ws.WS.WSRequestHolder
import scala.io.Source

object VirtualCSS extends Controller {

  def virtualCssJs(cssPath: String) = Action {
    Async {
      val jsFuture = getCssJs(cssPath)
      jsFuture.map(jsText => Ok(views.html.jsTemplate(jsText)))
    }
  }

  def getCssJs(cssPath: String) = {
    val cssTextFuture: Future[String] = getCssText(cssPath)
    cssTextFuture.map(processCssToJs)
  }

  def processCssToJs(cssText: String): Iterable[(String, String, String)] = {
    val css = parseCSS(cssText)

    val virtualCSSDefinitions = loadVirtualCSSDefinitions

    getJSForCSS(virtualCSSDefinitions, css)
  }

  def getJSForCSS(virtualCSSDefinitions: Option[Any], css: Map[String, List[(String, String)]]): Iterable[(String, String, String)] = {
    virtualCSSDefinitions.map(_ match {
      case jsonMap: Map[_, _] => {
        jsonMap.filter(entry => css.contains(entry._1.asInstanceOf[String])).
          map(extractDefinedJS(css)).flatten
      }
      case _ => throw new RuntimeException("json does not contain Map")
    }).get
  }

  def extractDefinedJS(css: Map[String, List[(String, String)]]): ((Any, Any)) => List[(String, String, String)] = {
    entry => {
      val property: String = entry._1.asInstanceOf[String]
      val implementations = entry._2.asInstanceOf[List[String]]
      css(property).map(selectorAndValue => {
        (selectorAndValue._1, selectorAndValue._2, implementations(1))
      })
    }
  }

  def loadVirtualCSSDefinitions = {
    val inputStream: InputStream = this.getClass.getClassLoader.getResourceAsStream("virtualProperties.JSON")
    val jsonText: String = try {
      Source.fromInputStream(inputStream, "utf-8").mkString("")
    }
    finally inputStream.close()

    scala.util.parsing.json.JSON.parseFull(jsonText)
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

package services

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WS.WSRequestHolder
import play.api.libs.ws.WS
import scala.concurrent.Future

object VirtualCss {

  val argumentRef = """\*(\d+?)""".r

  def cssForCss(cssPath: String): Future[String] = {
    combineVirtualAndRealCss(cssPath)
  }

  def combineVirtualAndRealCss(cssPath: String) = {
    CssParser.loadVirtualCssDefinitions.flatMap { definitions =>
      val url: WSRequestHolder = WS.url(cssPath)
      url.get().map {
        response => parseSourceAndMixInVirtualCss(response.body, definitions)
      }
    }
  }

  def parseSourceAndMixInVirtualCss(sourceText: String, definitions: Map[String, String]) = {
    val output = new StringBuilder()
    var currentSelector: String = ""
    CssParser.parseByProperties(sourceText) { (selector: String, property: String, value: String) =>
      if ((currentSelector != "") && (currentSelector != selector)) {
        output append "\n}\n"
      }
      if (currentSelector != selector) output append s"""$selector {"""
      currentSelector = selector
      output append s"""\n\t$property :$value;"""
      output append virtualProperties(definitions, property, value)
    }
    output append "\n}\n"
    output.toString()
  }

  def virtualProperties(virtualCssProperties: Map[String, String], property: String, values: String) = {
    virtualCssProperties get property match {
      case Some(body) => substArguments(body, values)
      case _ => ""
    }
  }

  def substArguments(body: String, argumentsBody: String) = {
    val arguments: Array[String] = argumentsBody.split( """\s""")
    argumentRef.replaceAllIn(body, { matched =>
      val argumentIndex = Integer.parseInt(matched group 1)
      arguments(argumentIndex)
    })
  }

  def jsForCss(cssPath: String) = {
    val cssFuture = CssParser.loadCssPropertiesFromUrl(cssPath)
    val implementationsFuture = JsParser.loadVirtualCssJsImplementations
    implementationsFuture.flatMap(jsTextAndImplementations => {
      cssFuture.map(css => {
        (jsTextAndImplementations._1, combinePropertiesAndJS(jsTextAndImplementations._2, css))
      })
    })
  }

  def combinePropertiesAndJS(jsDefinitions: Map[String, String], styleProperties: Map[String, List[(String, String)]]): Iterable[(String, String, String)] = {
    styleProperties.filter(entry => {
      jsDefinitions.contains(convertToCamelCase(entry._1))
    }).map(entry => {
      val propertyName = convertToCamelCase(entry._1)
      val selectorAndValues = entry._2
      selectorAndValues.map(selectorAndValue => {
        (selectorAndValue._1, selectorAndValue._2, propertyName)
      })
    }).flatten
  }

  def convertToCamelCase(propertyName: String) = {
    propertyName.split("-").foldLeft[String]("")((lastThing: String, thisThing: String) => {
      lastThing match {
        case "" => thisThing
        case _ => lastThing + thisThing.capitalize
      }
    })
  }
}

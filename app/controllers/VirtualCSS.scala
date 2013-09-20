package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import scala._

object VirtualCSS extends Controller {

  def virtualCssJs(cssPath: String) = Action {
    Async {
      val cssFuture = CssParser.loadCssProperties(cssPath)
      val jsFuture = JsParser.loadVirtualCssDefinitions.flatMap(virtualCssDefinitions => {
        cssFuture.map(css => {
          combinePropertiesAndJS(virtualCssDefinitions, css)
        })
      })
      jsFuture.map(jsStatementDefinitions => Ok(views.html.jsTemplate(jsStatementDefinitions)))
    }
  }

  def combinePropertiesAndJS(virtualCSSDefinitions: Map[String, String], styleProperties: Map[String, List[(String, String)]]): Iterable[(String, String, String)] = {
    styleProperties.filter(entry => virtualCSSDefinitions.contains(convertToCamelCase(entry._1))).map(entry => {
      val propertyName = convertToCamelCase(entry._1)
      val selectorAndValues = entry._2
      val implementation = virtualCSSDefinitions(propertyName)
      selectorAndValues.map(selectorAndValue => {
        (selectorAndValue._1, selectorAndValue._2, implementation)
      })
    }).flatten
  }

  def convertToCamelCase(propertyName: String): String = {
    propertyName.split("-").foldLeft[String]("")((lastThing: String, thisThing: String) => {
      lastThing match {
        case "" => thisThing
        case _ => lastThing + thisThing.capitalize
      }
    })
  }
}

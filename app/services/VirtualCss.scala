package services


import play.api.libs.concurrent.Execution.Implicits.defaultContext


object VirtualCss {

  def cssForCss(cssPath: String) = {
    combineVirtualAndRealCss(cssPath)
  }

  def combineVirtualAndRealCss(cssPath: String) = {
    val definitions = CssParser.loadVirtualCssDefinitions


    val output = new StringBuilder()
    var currentSelector: String = ""
    parse(virtualCssText) {
      (selector: String, property: String, value: String) =>
        selector match {
          case currentSelector =>
          case _ => {
            output ++ s"""$selector {\n"""
            currentSelector = selector
          }
        }
        output ++ s"""$property :$value;\n"""
    }
    output.toString()

  }

  def jsForCss(cssPath: String) = {
    val cssFuture = CssParser.loadCssPropertiesFromUrl(cssPath)
    val implementationsFuture = JsParser.loadVirtualCssJsImplementations
    implementationsFuture.flatMap(implementations => {
      cssFuture.map(css => {
        (implementations._1, combinePropertiesAndJS(implementations._2, css))
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

  def convertToCamelCase(propertyName: String): String = {
    propertyName.split("-").foldLeft[String]("")((lastThing: String, thisThing: String) => {
      lastThing match {
        case "" => thisThing
        case _ => lastThing + thisThing.capitalize
      }
    })
  }
}

package controllers


object JsParser {

  def parse(jsText: String) = {

    val map: Map[String, String] = parseJS(jsText)

    Some(map)
  }

  def parseJS(css: String): Map[String, String] = {
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

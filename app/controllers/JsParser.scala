package controllers


object JsParser {

  def parse(jsText: String) = {

    val map: Map[String, String] = parseJS(jsText)

    Some(map)
  }

  def parseJS(css: String): Map[String, String] = {
    val cssText: String = removeComments(css)
    val block = """(?s).*?\{(.*)\}""".r
    val definition = """([^:]*):(.*)|\z""".r

    var propertyFunctionPairs: Map[String, String] = Map()
    for (block(innerBlock) <- block findAllIn cssText) {
      println( s"""inner:[\n$innerBlock\n]""")
      for (definition(property, value) <- innerBlock.split( """(?s)(.*?\{.*?\}),|\z""")) {
        println(s"$property: $value")
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

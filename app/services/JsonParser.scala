package services

object JsonParser {

  def parse(jsonText: String) = {

    scala.util.parsing.json.JSON.parseFull(jsonText)
  }
}

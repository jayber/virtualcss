package services

import java.io.InputStream
import scala.io.Source

trait Parser {

  def getFileText(path: String): String = {
    val inputStream: InputStream = this.getClass.getClassLoader.getResourceAsStream("public/javascripts/virtualProperties.js")
    try {
      Source.fromInputStream(inputStream, "utf-8").mkString("")
    }
    finally inputStream.close()
  }
}

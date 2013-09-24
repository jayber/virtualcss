package services

import java.io.{FileInputStream, InputStream}
import scala.io.Source

trait Parser {

  def getFileText(path: String): String = {
    var inputStream: InputStream = this.getClass.getClassLoader.getResourceAsStream(path)
    if (inputStream == null) {
      inputStream = new FileInputStream(path)
    }
    try {
      Source.fromInputStream(inputStream, "utf-8").mkString("")
    }
    finally if (inputStream != null) inputStream.close()
  }
}

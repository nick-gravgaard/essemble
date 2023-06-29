#!/usr/bin/env -S scala-cli shebang

//> using scala 3.2
//> using dep "com.lihaoyi::os-lib:0.9.1"
//> using dep "io.circe::circe-yaml:0.14.2"
//> using dep "io.circe::circe-parser:0.14.5"

import scala.util.{Try, Success, Failure}
import scala.util.matching.Regex
import scala.util.matching.Regex.Match

import io.circe.yaml.parser
import io.circe.Json.fromString
import os.{Path, RelPath, proc, pwd, read, remove, temp, write}

def interpolate(text: String, map: Map[String, String]) = {
  def replacer(m: Match): String = m match {
    case Regex.Groups(found) => {
      val keyAndIndexPattern = """([A-Za-z-]+)(\[\d+\])?""".r
      val maybeKeyIndex: Option[(String, String)] = found.trim match {
        case keyAndIndexPattern(key, index) => Some((key, index))
        case _ => None
      }
      maybeKeyIndex match {
        case Some((key, index)) => map.get(key) match {
          case Some(result) => Option(index) match {
            case None => result.toString
            case Some(_) => result.toString.apply(index.drop(1).dropRight(1).toInt).toString // remove surrounding square brackets
          }
          case None => ""
        }
        case None => ""
      }
    }
  }

  """\{\{([^}]+)\}\}""".r.replaceAllIn(text, replacer(_))
}

def getConfigFromText(text: String): Either[String, Map[String, String]] = {
  val yaml = text.split('\n').dropWhile(_ != "---").drop(1).takeWhile(_ != "---").mkString("\n")
  Try(parser.parse(yaml)) match {
    case Success(Right(obj)) => obj.hcursor.downField("essemble").as[Map[String, String]] match {
      case Right(res) => Right(res)
      case Left(err) => Left(err.toString)
    }
    case Success(Left(err)) => Left(err.toString)
    case Failure(err) => Left(err.toString)
  }
}

def countWords(proseLines: List[String], countUntilOption: Option[String], tempDir: Path): Int = {
  val countableProse = (countUntilOption match {
    case Some(countUntil) => proseLines.takeWhile(_.filterNot("#".contains(_)).trim != countUntil)
    case None => proseLines
  }).mkString("\n")

  val tempWordCountableMd = tempDir / "temp-word-countable.md"
  val tempWordCountablePdf = tempDir / "temp-word-countable.pdf"
  val tempWordCountableTxt = tempDir / "temp-word-countable.txt"

  write(tempWordCountableMd, countableProse)
  proc("pandoc", tempWordCountableMd, "-s", "-o", tempWordCountablePdf).call()
  proc("pdftotext", tempWordCountablePdf, tempWordCountableTxt).call()
  val splitText = read(tempWordCountableTxt).split(Array('\n', ' '))
  // each page in the PDF ends with 2 things; a page number and a form-feed, so subtract the number of form-feeds * 2 from the word count
  splitText.filter(_ != "").length - (splitText.filter(_ == "\f").length * 2)
}

def main(markdownFilename: String) = {
  val markdownPath = pwd / markdownFilename
  val templateDir = pwd / "coversheets"

  Try(read(markdownPath)) match {
    case Failure(_) => println(s"Cannot find Markdown file: $markdownPath")
    case Success(text) => getConfigFromText(text) match {
      case Left(err) => println("There was an error processing the YAML at the top of the Markdown file. The error message was: " + err)
      case Right(config) => config.get("coversheet-template-filename") match {
        case None => println("""Cannot find "coversheet-template-filename" in YAML at the top of the Markdown file""")
        case Some(templateFilename) => Try(read(templateDir / RelPath(templateFilename))) match {
          case Failure(_) => println(s"Cannot find template file: $templateDir/$templateFilename")
          case Success(template) => generateEssay(config, markdownPath, text, template)
        }
      }
    }
  }
}

def generateEssay(config: Map[String, String], markdownPath: Path, text: String, template: String) = {
  val tempDir = temp.dir()

  val proseLines = text.split('\n').toList.dropWhile(_ != "---").drop(1).dropWhile(_ != "---").drop(1)

  val modifiedConfig = config.get("word-count") match {
    case Some(_) => config
    case None => config + ("word-count" -> countWords(proseLines, config.get("count-until"), tempDir).toString)
  }

  val tempCoversheetHtml = tempDir / "temp-coversheet.html"
  val tempCoversheetPdf = tempDir / "temp-coversheet.pdf"
  val tempEssayPdf = tempDir / "temp-essay.pdf"

  val populatedTemplate = interpolate(template, modifiedConfig)
  write(tempCoversheetHtml, populatedTemplate)
  proc("wkhtmltopdf", tempCoversheetHtml, tempCoversheetPdf).call()
  proc("pandoc", markdownPath, "-s", "-o", tempEssayPdf).call()
  val markdownFilename = markdownPath.toString
  val resultFilename = modifiedConfig.getOrElse("result-filename", markdownFilename.splitAt(markdownFilename.lastIndexOf('.'))._1 + ".pdf")
  proc("pdfunite", tempCoversheetPdf, tempEssayPdf, pwd / resultFilename).call()

  println(s"Compiled essay saved as ${pwd / resultFilename}")

  removeDir(tempDir)
}

def removeDir(dir: Path) = {
  for (file <- dir.toIO.listFiles) {
    remove(Path(file))
  }
  remove(dir)
}

main(args(0))

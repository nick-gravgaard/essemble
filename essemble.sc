#!/usr/bin/env amm

import ammonite.ops._
import ammonite.ops.ImplicitWd._

import scala.util.{Try, Success, Failure}
import scala.util.matching.Regex

import $ivy.`io.circe::circe-yaml:0.8.0`, io.circe.yaml.parser, io.circe.Json.fromString
import $ivy.`io.circe::circe-parser:0.8.0`

def interpolate(text: String, map: Map[String, String]) =
  """\{\{([^}]+)\}\}""".r.replaceAllIn(text, _ match {
    case Regex.Groups(found) => {
      val keyAndIndexPattern = """([A-Za-z-]+)(\[\d+\])?""".r
      Try(found.trim match {
        case keyAndIndexPattern(key, index) => (key, index)
      }) match {
        case Success((key, index)) => map.get(key) match {
          case Some(result) => Option(index) match {
            case None => result.toString
            case Some(_) => result.toString.apply(index.drop(1).dropRight(1).toInt).toString  // remove surrounding square brackets
          }
          case None => ""
        }
        case Failure(_) => ""
      }
    }
  })

def getConfigFromText(text: String): Either[String, Map[String, String]] = {
  val yaml = text.split('\n').dropWhile(_ != "---").drop(1).takeWhile(_ != "---").mkString("\n")
  Try(parser.parse(yaml)) match {
    case Success(Right(obj)) => obj.hcursor.downField("essemble").as[Map[String, String]] match {
      case Right(res) => Right(res)
      case Left(err) => Left(err.toString)
    }
    case Failure(err) => Left(err.toString)
  }
}

def countWords(proseLines: List[String], countUntilOption: Option[String], tempDir: Path): Int = {
  val countableProse = (countUntilOption match {
    case Some(countUntil) => proseLines.takeWhile(_.filterNot("#".contains(_)).trim != countUntil)
    case None => proseLines
  }).mkString("\n")

  val tempWordCountableMd = tempDir + "/temp-word-countable.md"
  val tempWordCountablePdf = tempDir + "/temp-word-countable.pdf"
  val tempWordCountableTxt = tempDir + "/temp-word-countable.txt"

  write(Path(tempWordCountableMd), countableProse)
  %%pandoc(tempWordCountableMd, "-s", "-o", tempWordCountablePdf)
  %%pdftotext(tempWordCountablePdf, tempWordCountableTxt)
  val splitText = read(Path(tempWordCountableTxt)).split(Array('\n', ' '))
  // each page in the PDF ends with 2 things; a page number and a form-feed, so subtract the number of form-feeds * 2 from the word count
  splitText.filter(_ != "").length - (splitText.filter(_ == "\f").length * 2)
}

@main
def main(markdownFilename: String) = {
  val markdownPath = Path(s"$pwd/$markdownFilename")
  val templateDir = s"$pwd/coversheets"

  Try(read(markdownPath)) match {
    case Failure(_) => println(s"Cannot find Markdown file: $markdownPath")
    case Success(text) => getConfigFromText(text) match {
      case Left(err) => println("There was an error processing the YAML at the top of the Markdown file. The error message was: " + err)
      case Right(config) => config.get("coversheet-template-filename") match {
        case None => println("""Cannot find "coversheet-template-filename" in YAML at the top of the Markdown file""")
        case Some(templateFilename) => Try(read(Path(s"$templateDir/$templateFilename"))) match {
          case Failure(_) => println(s"Cannot find template file: $templateDir/$templateFilename")
          case Success(template) => generateEssay(config, markdownPath, text, template)
        }
      }
    }
  }
}

def generateEssay(config: Map[String, String], markdownPath: Path, text: String, template: String) = {
  val tempDir = tmp.dir()

  val proseLines = text.split('\n').toList.dropWhile(_ != "---").drop(1).dropWhile(_ != "---").drop(1)

  val modifiedConfig = config.get("word-count") match {
    case Some(_) => config
    case None => config + ("word-count" -> countWords(proseLines, config.get("count-until"), tempDir).toString)
  }

  val tempCoversheetHtml = tempDir + "/temp-coversheet.html"
  val tempCoversheetPdf = tempDir + "/temp-coversheet.pdf"
  val tempEssayPdf = tempDir + "/temp-essay.pdf"

  val populatedTemplate = interpolate(template, modifiedConfig)
  write(Path(tempCoversheetHtml), populatedTemplate)
  %%wkhtmltopdf(tempCoversheetHtml, tempCoversheetPdf)
  %%pandoc(markdownPath, "-s", "-o", tempEssayPdf)
  val markdownFilename = markdownPath.toString
  val resultFilename = modifiedConfig.getOrElse("result-filename", markdownFilename.splitAt(markdownFilename.lastIndexOf('.'))._1 + ".pdf")
  %%pdfunite(tempCoversheetPdf, tempEssayPdf, pwd/resultFilename)

  println(s"Compiled essay saved as ${pwd/resultFilename}")

  rm! tempDir
}


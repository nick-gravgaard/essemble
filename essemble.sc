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

enum Error(val message: String) {
  case NoMarkdownFile(markdownPath: Path) extends Error(s"Cannot find Markdown file: $markdownPath")
  case BadYAML(err: String) extends Error(s"There was an error processing the YAML at the top of the Markdown file. The error message was: $err")
  case NoTemplateFilename extends Error("""Cannot find "coversheet-template-filename" in YAML at the top of the Markdown file""")
  case NoTemplateFile(templatePath: Path) extends Error(s"Cannot find template file: $templatePath")
}

import Error.*

def interpolate(text: String, map: Map[String, String]): String = {
  def replacer(m: Match): String = m match {
    case Regex.Groups(found) => {
      val keyAndIndexPattern = """([A-Za-z-]+)\[([0-9]+)\]""".r
      val keyPattern = """([A-Za-z-]+)""".r
      found.trim match {
        case keyAndIndexPattern(key, indexStrOrNull) =>
          val maybeChar = for {
            value <- map.get(key)
            index <- Option(indexStrOrNull).flatMap(_.toIntOption)
            char <- value.lift(index)
          } yield char
          maybeChar.map(_.toString).getOrElse("")
        case keyPattern(key) => key
        case _ => ""
      }
    }
  }

  """\{\{([^}]+)\}\}""".r.replaceAllIn(text, replacer(_))
}

def getConfigFromText(text: String): Either[String, Map[String, String]] = {
  val yaml = text.split('\n').dropWhile(_ != "---").drop(1).takeWhile(_ != "---").mkString("\n")
  (for {
    parsed <- parser.parse(yaml)
    config <- parsed.hcursor.downField("essemble").as[Map[String, String]]
  } yield config).left.map(_.toString)
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

def main(markdownFilename: String): Unit = {
  val markdownPath = pwd / markdownFilename
  val templateDir = pwd / "coversheets"

  val res = for {
    text <- Try(read(markdownPath)).toEither.left.map(_ => NoMarkdownFile(markdownPath))
    config <- getConfigFromText(text).left.map(err => BadYAML(err))
    templateFilename <- config.get("coversheet-template-filename").map(Right.apply).getOrElse(Left(NoTemplateFilename))
    templatePath = templateDir / RelPath(templateFilename)
    template <- Try(read(templatePath)).toEither.left.map(_ => NoTemplateFile(templatePath))
  } yield generateEssay(config, markdownPath, text, template)

  res.left.foreach { err => println(err.message) }
}

def generateEssay(config: Map[String, String], markdownPath: Path, text: String, template: String): Unit = {
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
  dir.toIO.listFiles.foreach { file => remove(Path(file)) }
  remove(dir)
}

main(args(0))

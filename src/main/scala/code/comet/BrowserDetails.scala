package code
package comet

import code.model.{BrowserTests}
import code.snippet.Param._

import scala.xml.{NodeSeq, Text, Elem}

import net.liftweb._
import util._
import actor._
import http._
import common.{Box, Full,Logger}
import mapper.{OrderBy, Descending, SelectableField}
import http.SHtml._
import http.S._
import http.js.JsCmds.{SetHtml, SetValueAndFocus}
import net.liftweb.http.js.JE.Str
import Helpers._


class BrowserDetails extends CometActor with Logger with CometListener {

  override def defaultPrefix = Full("comet")
  //Which object will get messages that this comet actor needs to
  //process
  def registerWith = BrowserDetailsServer

  //private var msgs: Vector[String] = Vector()


  /**
    * Generate the Test Result view section
    */

  //val showingVersion= versionString
  var showingVersion= ""

  debug("Version number: %s".format(showingVersion))

  def render= {
    debug("Version number 2: %s".format(showingVersion))
    val testResultControlRender= BrowserTests.getBrowserTestResultByBrowserName( showingVersion, "Control render" )
    val testResultGraphsRender= BrowserTests.getBrowserTestResultByBrowserName( showingVersion, "Graphs render" )
    val testResultCGWorks= BrowserTests.getBrowserTestResultByBrowserName( showingVersion, "CG Works" )
    val testResultNoErrors= BrowserTests.getBrowserTestResultByBrowserName( showingVersion, "No Errors" )

    case class Result(OS: String, pass: Boolean) {
      def clss = if (pass) "success" else "error"
    }

    case class TestRow(tests: Map[Int, Option[Result]])


    // a list of all our tests
    val tests = List(testResultControlRender, testResultGraphsRender, testResultCGWorks, testResultNoErrors)

    // a set of the keys of the tests
    val browserNameSet = Set(tests.flatMap(_.keys) :_*)

    val results: Map[String, TestRow] = Map(browserNameSet.toSeq.map{
      set =>
        (set, TestRow(Map(tests.zipWithIndex.map {
          case (test, idx) => idx -> test.get(set).map{
                                                      case (browser, pass) => Result(browser, pass == "PASS")
                                                      }
        } :_*)))
      }:_*)

      //info(results)
                       

    def dd(f: NodeSeq => NodeSeq): NodeSeq => NodeSeq = {
      case e: Elem => f(e.child)
      case x => f(x)
    }

    ClearClearable andThen
    "h2 *" #> dd(_ ++ Text(showingVersion)) &
    "#row *" #> results.map {
      case (name, row) => "#col" #> (
        ("* *" #> name) :: row.tests.toList.map {
          case (pos, Some(res)) => "* *" #> name   & "* [class]" #> res.clss
          case (pos, _) => "* *" #> "N/A" & "* [class]" #> "notice"
        }
        )
    }

  }

  override def lowPriority : PartialFunction[Any,Unit] = {
    case v: String => {
      showingVersion= v
      info("Updating BrowserTestResults: %s".format(v))
      reRender()
    }
    case _ => {
      info("We are here")
      reRender()
    }

  }


}


package code
package snippet

import code.snippet.Param._

import scala.xml.{NodeSeq, Text, Elem}

import net.liftweb._
import actor._
import util._
import common.Logger
import http._
import SHtml._
import S._
import js.JsCmds.{SetHtml, SetValueAndFocus}

import Helpers._

class TopMenu extends Logger {

  /**
    * Generate the Test Result view section
    */

  val showingVersion= versionString

  debug(showingVersion)
  

  def addVersionToLinks ={
    //code.comet.BrowserDetailsServer ! showingVersion
    ClearClearable andThen
    "a [href+]" #> Text("/" + showingVersion)

  }



}


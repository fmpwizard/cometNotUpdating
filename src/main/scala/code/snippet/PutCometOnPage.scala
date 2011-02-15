package code
package snippet

import scala.xml.NodeSeq
import net.liftweb._
import util._
import actor._
import http._
import Helpers._
import common.Full


import code.snippet.Param._


object PutCometOnPage {
  def render(xhtml: NodeSeq): NodeSeq = {
    val showingVersion= versionString
    //val id = Helpers.nextFuncName
    val id = "browserdetails" + showingVersion
    for (sess <- S.session) sess.sendCometActorMessage(
      "BrowserDetails", Full(id), showingVersion
    ) // Note showingVersion is the version number to display
    <lift:comet type="BrowserDetails" name={id}>{xhtml}</lift:comet>
  }
}
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
    val id = Helpers.nextFuncName
    val showingVersion= versionString
    //val params = showingVersion // get params from request
    for (sess <- S.session) sess.sendCometActorMessage(
      "BrowserDetails", Full(id), showingVersion/*QueryParams(params)*/
    ) // Note QueryParams is your own case class
    <lift:comet type="BrowserDetails" name={id}>{xhtml}</lift:comet>
  }
}
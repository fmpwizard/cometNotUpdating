package code
package comet

import net.liftweb._
import http._
import actor._
import common.Logger


object BrowserDetailsServer extends  Logger with LiftActor with ListenerManager {


  private var messages: String = ""

  def createUpdate = messages

  override def lowPriority = {
    case code.api.Ping => {
      //messages = s
      updateListeners()
    }
    case s: String => {
      messages= s
      updateListeners()
    }
  }
}
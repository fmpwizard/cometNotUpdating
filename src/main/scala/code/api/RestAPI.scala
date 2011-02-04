package code
package api

//import java.text.SimpleDateFormat

import scala.xml.{Elem, Node, NodeSeq, Text}

import net.liftweb.common.{Box,Empty,Failure,Full,Logger}
import net.liftweb.http.rest.{RestHelper}
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.json._
//import net.liftweb.mapper.By
import net.liftweb.actor._

import model._
import net.liftweb.http._
import net.liftweb.util.Helpers


case object Ping

object RestHelperAPI extends RestHelper with Logger {




  case class BrowserTestResultExtractor(
                              apiversion: Option[String],
                              service_manager_version: Option[String],
                              test_name: Option[String],
                              test_result: Option[Int],
                              platform_name: Option[String],
                              browser_name: Option[String]
)

  serve {
    case "v1" :: "rest" :: "browsertests" :: _ JsonPut json -> _ =>
      // json is a net.liftweb.json.JsonAST.JValue
      verifyBrowserTestResult(Full(json.extract[BrowserTestResultExtractor]))

  }

  def verifyBrowserTestResult(
          parsedBrowserTestResult : Box[BrowserTestResultExtractor])
  : LiftResponse = parsedBrowserTestResult match {
    case Full(browserTestResult) => {
      browserTestResult   match {
        //If we have all fields, add them to the database
        case BrowserTestResultExtractor(
          Some(apiVersion),
          Some(srvmgrVersion),
          Some(testName),
          Some(testResult),
          Some(platform),
          Some(browser)) => {
            info("It passed: %s".format(browser))
            BrowserTests.updateOrAddBrowserTestResult(parsedBrowserTestResult)
            //Tell the BrowserDetails comet actor to update the UI
            val cometId= "browserdetails" + Some(srvmgrVersion).getOrElse("N/A")
            for (sess <- S.session) {
              sess.sendCometActorMessage(
                "BrowserDetails", Full(cometId), Some(srvmgrVersion).getOrElse("N/A")
              )
              info("We found these actors: %s".format(sess.findComet("BrowserDetails")))
            }
            //code.comet.BrowserDetailsServer ! Ping
            NoContentResponse()
        }
        // Else. log an error and return a erro4 400 with a message
        case BrowserTestResultExtractor(a, b, c, d, e, f) => {
          info("It did not passed: %s".format(a))
          val msg= ("We are missing some fields, " +
            "we got: %s, %s, %s, %s, %s").format( b, c, d, e, f)
          ResponseWithReason(BadResponse(), msg)
        }
      }
    }
    case Failure(msg, _, _) => {
      info(msg)
      ResponseWithReason(BadResponse(), msg)
    }
    case error => {
      info("Parsed browserTestResult as : " + error)
      BadResponse()
    }
  }

}

package bootstrap.liftweb

import java.sql.{Connection, DriverManager}

import code.snippet._
import net.liftweb._
import util._
import Helpers._

import common._
import http._
import sitemap._
import Loc._
import mapper._

import code.model._
import code.api._



import org.apache.commons.dbcp.DriverManagerConnectionFactory
import org.apache.commons.dbcp.PoolableConnectionFactory
import org.apache.commons.dbcp.PoolingDriver
import org.apache.commons.pool.impl.GenericObjectPool



/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  
  // Set up a logger to use for startup messages
  val logger = Logger(classOf[Boot])
  def boot {
    
    //if (!DB.jndiJdbcConnAvailable_?) DB.defineConnectionManager(DefaultConnectionIdentifier, DBVendor)
    if (!DB.jndiJdbcConnAvailable_?) {
      val vendor =
	new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
			     Props.get("db.url") openOr
			     "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE",
			     Props.get("db.user"), Props.get("db.password"))

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    }

    // Use Lift's Mapper ORM to populate the database
    // you don't need to use Mapper to use Lift... use
    // any ORM you want
    Schemifier.schemify(true, Schemifier.infoF _,   Versions, BrowserTests)

    // where to search snippet
    LiftRules.addToPackages("code")
    // rest api
    LiftRules.dispatch.prepend(RestHelperAPI)

    // Build SiteMap
    def sitemap(): SiteMap = SiteMap(
      Param.agentDetailsMenu,
      Param.serviceManagerDetailsMenu,
      Param.overviewMenu,
      Param.BrowserDetailsMenu
      ) 

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMapFunc(() => sitemap())

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    
    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    //LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    // Make a transaction span the whole HTTP request
    S.addAround(DB.buildLoanWrapper)
    




  }
}


object DBVendor extends ConnectionManager with Logger {
  def newConnection(name: ConnectionIdentifier): Box[Connection] = {
    try {
      Class.forName("com.mysql.jdbc.Driver")
      val jdbcurl= (Props.get("db.url") openOr "") + "?user=" + (Props.get("db.user") openOr "") + "&password=" + (Props.get("db.password") openOr "")
      debug( jdbcurl)

      // Connection pool

      val connectionPool = new GenericObjectPool(null)
      //connectionPool.setMaxIdle(30)
      //connectionPool.setMinIdle(5)
      val connectionFactory = new DriverManagerConnectionFactory(
          (Props.get("db.url") openOr "" ) + "?" +
          (Props.get("additionalurlparam") openOr("")),
          Props.get("db.user") openOr "",
          Props.get("db.password") openOr ""
      )
      val poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true)
      val driver = new PoolingDriver();
      driver.registerPool("qadashboard",connectionPool);




      val dm = DriverManager.getConnection("jdbc:apache:commons:dbcp:qadashboard")
      Full(dm)
    } catch {
      case e : Exception => e.printStackTrace; Empty
    }
  }
  def releaseConnection(conn: Connection) {conn.close}
}


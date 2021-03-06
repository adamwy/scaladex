package ch.epfl.
scala.index
package server
package routes

import model.misc.UserInfo

import TwirlSupport._

import com.softwaremill.session.SessionDirectives._
import com.softwaremill.session.SessionOptions._

import akka.http.scaladsl.server.Directives._

class FrontPage(dataRepository: DataRepository, session: GithubUserSession) {
  import session._

  private def frontPage(userInfo: Option[UserInfo]) = {
    import dataRepository._
    for {
      keywords <- keywords()
      targets <- targets()
      mostDependedUpon <- mostDependedUpon()
      latestProjects <- latestProjects()
      latestReleases <- latestReleases()
    } yield {

      def query(label: String)(xs: String*): String = 
        xs.map(v => s"$label:$v").mkString("search?q=", " OR ", "")

      val ecosystems = Map(
        "Akka" -> query("keywords")("akka-extension", "akka-http-extension", "akka-persistence-plugin"),
        "Scala.js" -> "search?targets=scala.js_0.6",
        "Spark" -> query("depends-on")("apache/spark-streaming", "apache/spark-graphx", "apache/spark-hive", "apache/spark-mllib", "apache/spark-sql"),
        "Typelevel" -> "typelevel"
      )

      val excludeTargets = Set(
        "scala_2.9",
        "scala_2.8"
      )
      val targets0 = targets.filterNot{ case(target, _) => excludeTargets.contains(target)}
      views.html.frontpage(keywords, targets0, latestProjects, mostDependedUpon, latestReleases, userInfo, ecosystems)
    }
  }

  val routes =
    pathSingleSlash {
      optionalSession(refreshable, usingCookies) { userId =>
        complete(frontPage(getUser(userId).map(_.user)))
      }
    }
}

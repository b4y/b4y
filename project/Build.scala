import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "todolist"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    "play.modules.mailer" %% "play-mailer" % "1.1.2",
  "net.vz.mongodb.jackson" %% "play-mongo-jackson-mapper" % "1.1.0"
//    jdbc,
//    anorm
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += "Rhinofly Internal Release Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-release-local"

    // Add your own project settings here      
  )

}

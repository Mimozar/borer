import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import sbt._

inThisBuild(
  List(
    organization := "io.bullet",
    homepage := Some(new URL("https://github.com/sirthias/borer/")),
    description := "CBOR and JSON (de)serialization in Scala",
    startYear := Some(2019),
    licenses := Seq("MPLv2" → new URL("https://www.mozilla.org/en-US/MPL/2.0/")),
    scmInfo := Some(ScmInfo(url("https://github.com/sirthias/borer/"), "scm:git:git@github.com:sirthias/borer.git")),
    developers :=
      List(
        "sirthias" -> "Mathias Doenitz",
      ).map { case (username, fullName) =>
        Developer(username, fullName, s"@$username", url(s"https://github.com/$username"))
      }
  )
)


lazy val commonSettings = Seq(
  scalaVersion := "3.0.0-RC1",

  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-unchecked",
    "-Xtarget:8",
    "-Xfatal-warnings",
  ),

  Compile / console / scalacOptions ~= (_ filterNot(o => o.contains("warn") || o.contains("Xlint"))),
  Test / console / scalacOptions := (Compile / console / scalacOptions).value,
  Compile / doc / scalacOptions += "-no-link-warnings",
  sourcesInBase := false,

  Compile / unmanagedResources += baseDirectory.value.getParentFile.getParentFile / "LICENSE",

  // file headers
  headerLicense := Some(HeaderLicense.MPLv2("2019-2021", "Mathias Doenitz")),

  // reformat main and test sources on compile
  //scalafmtOnCompile := true,

  testFrameworks += new TestFramework("utest.runner.Framework"),
  console / initialCommands := """import io.bullet.borer._""",

  // publishing
  publishMavenStyle := true,
  Test / publishArtifact := false,
  pomIncludeRepository := (_ ⇒ false),
  publishTo := sonatypePublishToBundle.value,
)

lazy val scalajsSettings = Seq(
  scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule).withSourceMap(false)),
  Global / scalaJSStage := FastOptStage,
  scalacOptions ~= { _.filterNot(_ == "-Ywarn-dead-code") }
)

lazy val releaseSettings = {
  import ReleaseTransformations._
  Seq(
    releaseCrossBuild := true,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      releaseStepCommand("sonatypeBundleRelease"),
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )
}

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  publish / skip := true
)

/////////////////////// DEPENDENCIES /////////////////////////

val utest               = Def.setting("com.lihaoyi"            %%% "utest"                   % "0.7.7"  % "test")

/////////////////////// PROJECTS /////////////////////////

lazy val borer = project.in(file("."))
  .aggregate(`core-jvm`, `core-js`)
  .aggregate(`derivation-jvm`, `derivation-js`)
  .settings(releaseSettings)
  .settings(noPublishSettings)
  .settings(
    onLoadMessage := welcomeMessage.value
  )

lazy val `core-jvm` = core.jvm.enablePlugins(SpecializeJsonParserPlugin)
lazy val `core-js`  = core.js
lazy val core = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .enablePlugins(AutomateHeaderPlugin, BoilerplatePlugin)
  .settings(commonSettings)
  .settings(releaseSettings)
  .settings(
    moduleName := "borer-core",
    libraryDependencies ++= Seq(utest.value),

    // point sbt-boilerplate to the common "project"
    Compile / boilerplateSource := baseDirectory.value.getParentFile / "src" / "main" / "boilerplate",
    Compile / sourceManaged := baseDirectory.value.getParentFile / "target" / "scala" / "src_managed" / "main"
  )
  .jvmSettings(
    Compile / specializeJsonParser / sourceDirectory := baseDirectory.value.getParentFile / "src" / "main",
    Compile / specializeJsonParser / sourceManaged := baseDirectory.value / "target" / "scala" / "src_managed" / "main",
    Compile / managedSourceDirectories += (Compile / specializeJsonParser / sourceManaged).value
  )
  .jsSettings(scalajsSettings: _*)

lazy val `derivation-jvm` = derivation.jvm
  .dependsOn(`core-jvm` % "compile->compile;test->test")
lazy val `derivation-js`  = derivation.js
  .dependsOn(`core-js` % "compile->compile;test->test")
lazy val derivation = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .enablePlugins(AutomateHeaderPlugin)
  .settings(commonSettings)
  .settings(releaseSettings)
  .settings(
    moduleName := "borer-derivation",
    libraryDependencies ++= Seq(utest.value)
  )
  .jsSettings(scalajsSettings: _*)

// welcome message in the style of zio.dev
def welcomeMessage = Def.setting {
  import scala.Console

  def red(text: String): String = s"${Console.RED}$text${Console.RESET}"
  def item(text: String): String = s"${Console.GREEN}▶ ${Console.CYAN}$text${Console.RESET}"

  s"""|${red(" _                            ")}
      |${red("| |                           ")}
      |${red("| |__   ___  _ __ ___ _ __    ")}
      |${red("| '_ \\ / _ \\| '__/ _ \\ '__|")}
      |${red("| |_) | (_) | | |  __/ |      ")}
      |${red("|_.__/ \\___/|_|  \\___|_|    " + version.value)}
      |
      |Useful sbt tasks:
      |${item("project core")} - Descend into the JVM core module
      |${item("project `core-js`")} - Descend into the JS core module
      |${item("test")} - Run all tests
      |${item("project benchmarks;benchmarkResults;project /")} - Show results of latest benchmark runs
      """.stripMargin
}
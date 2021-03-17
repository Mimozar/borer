//addSbtPlugin("org.scalameta"          % "sbt-scalafmt"                % "2.4.2")
addSbtPlugin("io.crashbox"            % "sbt-gpg"                     % "0.2.1")
addSbtPlugin("com.github.sbt"         % "sbt-release"                 % "1.0.15")
addSbtPlugin("org.xerial.sbt"         % "sbt-sonatype"                % "3.9.7")
addSbtPlugin("de.heikoseeberger"      % "sbt-header"                  % "5.6.0")
addSbtPlugin("io.spray"               % "sbt-boilerplate"             % "0.6.1")
addSbtPlugin("org.scala-js"           % "sbt-scalajs"                 % "1.5.0")
addSbtPlugin("org.portable-scala"     % "sbt-scalajs-crossproject"    % "1.0.0")
addSbtPlugin("com.typesafe.sbt"       % "sbt-ghpages"                 % "0.6.3")
addSbtPlugin("com.typesafe.sbt"       % "sbt-site"                    % "1.4.1")

libraryDependencies ++= Seq(
  "io.bullet" %% "borer-core"       % "1.6.3",
  "io.bullet" %% "borer-derivation" % "1.6.3"
)
import sbt._

object CompilerPlugin {

  object Version {
    val paradise = "2.1.1"
    val betterMonadicFor = "0.3.0"
    val kindProjector = "0.10.1"
  }

  val paradise = compilerPlugin("org.scalamacros"        % "paradise"            % Version.paradise cross CrossVersion.full)
  val betterMonadicFor = compilerPlugin("com.olegpy"     %% "better-monadic-for" % Version.betterMonadicFor)
  val kindProjector = compilerPlugin("org.typelevel"     %% "kind-projector"     % Version.kindProjector)
  val scalazDerivingPlugin = compilerPlugin("org.scalaz" %% "deriving-plugin"    % Dependency.Version.deriving)
}

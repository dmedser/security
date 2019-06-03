import sbt._

object Dependencies {
  object Version {
    val akkaActor = "2.5.23"
    val nimbusJwt = "7.1"
    val nimbusOAuth2 = "6.13"
    val catsCore = "1.6.0"
    val catsEffect = "1.3.0"
    val pureConfig = "0.11.0"
    val circe = "0.12.0-M1"
    val newType = "0.4.2"
    val deriving = "1.0.0"
    val doobie = "0.7.0-M5"
    val mouse = "0.21"
  }

  val akkaActor = "com.typesafe.akka"                %% "akka-actor"             % Version.akkaActor
  val nimbusJwt = "com.nimbusds"                     % "nimbus-jose-jwt"         % Version.nimbusJwt
  val nimbusOAuth2 = "com.nimbusds"                  % "oauth2-oidc-sdk"         % Version.nimbusOAuth2
  val catsCore = "org.typelevel"                     %% "cats-core"              % Version.catsCore
  val catsEffect = "org.typelevel"                   %% "cats-effect"            % Version.catsEffect
  val pureConfig = "com.github.pureconfig"           %% "pureconfig"             % Version.pureConfig
  val pureConfigCatsEffect = "com.github.pureconfig" %% "pureconfig-cats-effect" % Version.pureConfig
  val circeCore = "io.circe"                         %% "circe-core"             % Version.circe
  val newType = "io.estatico"                        %% "newtype"                % Version.newType
  val scalazDeriving = "org.scalaz"                  %% "scalaz-deriving"        % Version.deriving
  val doobieCore = "org.tpolecat"                    %% "doobie-core"            % Version.doobie
  val doobiePostgres = "org.tpolecat"                %% "doobie-postgres"        % Version.doobie
  val doobieHikari = "org.tpolecat"                  %% "doobie-hikari"          % Version.doobie
  val mouse = "org.typelevel"                        %% "mouse"                  % Version.mouse
}

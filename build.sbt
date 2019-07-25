import CompilerPlugin._
import Dependency._

name := "security"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  akkaActor,
  nimbusJwt,
  nimbusOAuth2,
  catsCore,
  catsEffect,
  pureConfig,
  pureConfigCatsEffect,
  circeCore,
  newType,
  scalazDeriving,
  doobieCore,
  doobiePostgres,
  doobieHikari,
  mouse,
  monixCatnap
)

addCompilerPlugin(paradise)
addCompilerPlugin(betterMonadicFor)
addCompilerPlugin(kindProjector)
addCompilerPlugin(scalazDeriving)

scalacOptions ++= List(
  "-deprecation",
  "-feature",
  "-Xfatal-warnings",
  "-Ypartial-unification",
  "-language:postfixOps",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:experimental.macros"
)

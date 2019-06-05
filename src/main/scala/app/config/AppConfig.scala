package app.config

import app.config.AppConfig.SecurityConfig.{JWK, JWT}
import app.config.AppConfig._
import cats.effect.Sync
import pureconfig.generic.auto._
import pureconfig.generic.ProductHint
import pureconfig.module.catseffect.loadConfigF
import pureconfig.{CamelCase, ConfigFieldMapping}

case class AppConfig(security: SecurityConfig)

object AppConfig {

  def load[F[_] : Sync]: F[AppConfig] = {
    implicit def hint[T]: ProductHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

    loadConfigF[F, AppConfig]("app")
  }

  case class SecurityConfig(jwk: JWK, jwt: JWT)
  object SecurityConfig {
    case class JWK(url: String, connectionTimeout: Int, readTimeout: Int, maxSize: Int)
    case class JWT(issuer: String, audience: List[String])
  }

}

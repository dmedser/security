package app.config

import app.config.AppConfig.AuthConfig.JWK
import app.config.AppConfig._
import cats.effect.Sync
import pureconfig.generic.auto._
import pureconfig.generic.ProductHint
import pureconfig.module.catseffect.loadConfigF
import pureconfig.{CamelCase, ConfigFieldMapping}

case class AppConfig(security: AuthConfig)

object AppConfig {

  def load[F[_] : Sync]: F[AppConfig] = {
    implicit def hint[T]: ProductHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

    loadConfigF[F, AppConfig]("app")
  }

  case class AuthConfig(jwk: JWK)
  object AuthConfig {
    case class JWK(
      url: String,
      issuer: String,
      audience: List[String],
      connectionTimeout: Int,
      readTimeout: Int,
      maxSize: Int
    )
  }

}

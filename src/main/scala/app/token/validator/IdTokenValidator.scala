package app.token.validator

import app.config.AppConfig.SecurityConfig
import app.token.Token
import app.token.Token.IdToken
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.nimbusds.openid.connect.sdk.claims.AccessTokenHash
import com.nimbusds.openid.connect.sdk.validators.{IDTokenValidator => NimbusIdTokenValidator}

class IdTokenValidator[F[_] : Sync](config: SecurityConfig) {
  def validate(token: IdToken): F[AccessTokenHash] =
    for {
      clientId  <- Token.getClientId(token, config.jwt.audience)
      validator <- NimbusValidatorFactory.create(config, clientId)(new NimbusIdTokenValidator(_, _, _, _, _))
      atHash    <- Sync[F].catchNonFatal(validator.validate(token.unwrap, null).getAccessTokenHash)
    } yield atHash
}

object IdTokenValidator {
  def create[F[_] : Sync](config: SecurityConfig): F[IdTokenValidator[F]] =
    Sync[F].delay(new IdTokenValidator(config))
}

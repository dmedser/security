package app.token.validator

import app.config.AppConfig.SecurityConfig
import app.service.BlacklistService
import app.token.Token.{AccessToken, IdToken, LogoutToken}
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._

class TokenValidator[F[_] : Sync](
  idTokenValidator: IdTokenValidator[F],
  accessTokenValidator: AccessTokenValidator[F],
  logoutTokenValidator: LogoutTokenValidator[F]
) {

  def validate(idToken: IdToken, accessToken: AccessToken): F[Unit] =
    idTokenValidator.validate(idToken) >>= (atHash => accessTokenValidator.validate(accessToken, atHash))

  def validate(logoutToken: LogoutToken): F[Unit] =
    logoutTokenValidator.validate(logoutToken)
}

object TokenValidator {
  def create[F[_] : Sync](config: SecurityConfig, blacklist: BlacklistService[F]): F[TokenValidator[F]] =
    for {
      itValidator <- IdTokenValidator.create(config)
      atValidator <- AccessTokenValidator.create(config, blacklist)
      ltValidator <- LogoutTokenValidator.create(config, blacklist)
    } yield new TokenValidator(itValidator, atValidator, ltValidator)
}

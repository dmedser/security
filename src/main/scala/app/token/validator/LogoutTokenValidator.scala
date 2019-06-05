package app.token.validator

import app.config.AppConfig.SecurityConfig
import app.model.AccessTokenId
import app.service.BlacklistService
import app.token.Token.LogoutToken
import app.token.{AccessTokenData, AccessTokenExpire, AccessTokenID, Token}
import cats.effect.Sync
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.nimbusds.jwt.util.DateUtils
import com.nimbusds.openid.connect.sdk.validators.{LogoutTokenValidator => NimbusLogoutTokenValidator}

class LogoutTokenValidator[F[_] : Sync](config: SecurityConfig, blacklist: BlacklistService[F]) {

  def validate(token: LogoutToken): F[Unit] =
    for {
      clientId  <- Token.getClientId(token, config.jwt.audience)
      validator <- NimbusValidatorFactory.create(config, clientId)(new NimbusLogoutTokenValidator(_, _, _, _, _))
      _         <- Sync[F].catchNonFatal(validator.validate(token.unwrap))
      atData    <- Sync[F].delay(token.unwrap.getJWTClaimsSet.getJSONObjectClaim(AccessTokenData))
      atId      <- AccessTokenId.parse(atData.getAsString(AccessTokenID)).liftTo[F]
      atExp <- Sync[F].delay(
        DateUtils.fromSecondsSinceEpoch(atData.getAsNumber(AccessTokenExpire).longValue()).toInstant
      )
      _ <- blacklist.add(atId, atExp)
    } yield ()
}

object LogoutTokenValidator {
  def create[F[_] : Sync](config: SecurityConfig, blacklist: BlacklistService[F]): F[LogoutTokenValidator[F]] =
    Sync[F].delay(new LogoutTokenValidator(config, blacklist: BlacklistService[F]))
}

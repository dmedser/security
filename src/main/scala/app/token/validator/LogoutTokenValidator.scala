package app.token.validator

import java.time.Instant

import app.config.AppConfig.AuthConfig
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
import net.minidev.json.JSONObject

class LogoutTokenValidator[F[_] : Sync](config: AuthConfig, blacklist: BlacklistService[F]) {

  private def getAccessTokenData(token: LogoutToken): F[JSONObject] =
    Sync[F].delay(token.unwrap.getJWTClaimsSet.getJSONObjectClaim(AccessTokenData))

  private def getAccessTokenId(atData: JSONObject): F[AccessTokenId] =
    AccessTokenId.parse(atData.getAsString(AccessTokenID)).liftTo[F]

  private def getAccessTokenExpiry(atData: JSONObject): F[Instant] =
    Sync[F].delay(DateUtils.fromSecondsSinceEpoch(atData.getAsNumber(AccessTokenExpire).longValue()).toInstant)

  def validate(token: LogoutToken): F[Unit] =
    for {
      clientId  <- Token.getClientId(token, config.jwk.audience)
      validator <- NimbusValidatorFactory.create(config, clientId)(new NimbusLogoutTokenValidator(_, _, _, _, _))
      _         <- Sync[F].catchNonFatal(validator.validate(token.unwrap))
      atData    <- getAccessTokenData(token)
      atId      <- getAccessTokenId(atData)
      atExp     <- getAccessTokenExpiry(atData)
      _         <- blacklist.add(atId, atExp)
    } yield ()
}

object LogoutTokenValidator {
  def create[F[_] : Sync](config: AuthConfig, blacklist: BlacklistService[F]): F[LogoutTokenValidator[F]] =
    Sync[F].delay(new LogoutTokenValidator(config, blacklist: BlacklistService[F]))
}

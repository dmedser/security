package app.token.validator

import app.config.AppConfig.AuthConfig
import app.model.AccessTokenId
import app.service.BlacklistService
import app.token.Token.AccessToken
import app.token.{JwtValidationException, Token, jwsAlgorithm}
import cats.effect.Sync
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.nimbusds.oauth2.sdk.token.TypelessAccessToken
import com.nimbusds.openid.connect.sdk.claims.AccessTokenHash
import com.nimbusds.openid.connect.sdk.validators.{AccessTokenValidator => NimbusAccessTokenValidator}

class AccessTokenValidator[F[_] : Sync](config: AuthConfig, blacklist: BlacklistService[F]) {

  private def checkBlacklist(id: AccessTokenId)(implicit F: Sync[F]): F[Unit] =
    F.ifM(blacklist.contains(id))(F.raiseError(RevokedTokenException), F.unit)

  def validate(token: AccessToken, atHash: AccessTokenHash): F[Unit] =
    for {
      _ <- Token.checkClientId(token, config.jwk.audience)
      _ <- Sync[F]
        .catchNonFatal(
          NimbusAccessTokenValidator
            .validate(new TypelessAccessToken(token.unwrap.getParsedString), jwsAlgorithm, atHash)
        )
      atId <- AccessTokenId.parse(token.unwrap.getJWTClaimsSet.getJWTID).liftTo[F]
      _    <- checkBlacklist(atId)
    } yield ()

  case object RevokedTokenException extends JwtValidationException("Token has been revoked")
}

object AccessTokenValidator {
  def create[F[_] : Sync](config: AuthConfig, blacklist: BlacklistService[F]): F[AccessTokenValidator[F]] =
    Sync[F].delay(new AccessTokenValidator(config, blacklist))
}

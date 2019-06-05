package app.token.validator

import app.config.AppConfig.SecurityConfig
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

class AccessTokenValidator[F[_]](config: SecurityConfig, blacklist: BlacklistService[F])(implicit F: Sync[F]) {

  def validate(token: AccessToken, atHash: AccessTokenHash): F[Unit] =
    for {
      _ <- Token.checkClientId(token, config.jwt.audience)
      _ <- Sync[F]
        .catchNonFatal(
          NimbusAccessTokenValidator
            .validate(new TypelessAccessToken(token.unwrap.getParsedString), jwsAlgorithm, atHash)
        )
      atId <- AccessTokenId.parse(token.unwrap.getJWTClaimsSet.getJWTID).liftTo[F]
      _    <- F.ifM(blacklist.contains(atId))(F.raiseError(RevokedTokenException), F.unit)
    } yield ()

  case object RevokedTokenException extends JwtValidationException("Token has been revoked")
}

object AccessTokenValidator {
  def create[F[_] : Sync](config: SecurityConfig, blacklist: BlacklistService[F]): F[AccessTokenValidator[F]] =
    Sync[F].delay(new AccessTokenValidator(config, blacklist))
}

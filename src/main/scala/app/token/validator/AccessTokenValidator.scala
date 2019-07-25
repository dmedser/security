package app.token.validator

import app.config.AppConfig.SecurityConfig
import app.newtypes.AccessTokenId
import app.token.Token.AccessToken
import app.token.{Token, jwsAlgorithm}
import cats.effect.Sync
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.nimbusds.oauth2.sdk.token.{BearerAccessToken, TypelessAccessToken}
import com.nimbusds.openid.connect.sdk.claims.AccessTokenHash
import com.nimbusds.openid.connect.sdk.validators.{AccessTokenValidator => NimbusAccessTokenValidator}

trait AccessTokenValidator[F[_]] {
  def validate(accessTokenHeader: String, accessTokenHash: AccessTokenHash): F[AccessTokenId]
}

object AccessTokenValidator {

  def apply[F[_] : Sync](config: SecurityConfig): F[AccessTokenValidator[F]] =
    Sync[F].delay(new Impl(config))

  private final class Impl[F[_]](config: SecurityConfig)(implicit F: Sync[F]) extends AccessTokenValidator[F] {

    def validate(accessTokenHeader: String, accessTokenHash: AccessTokenHash): F[AccessTokenId] = {

      def validateByNimbusValidator(at: AccessToken, atHash: AccessTokenHash): F[Unit] =
        F.delay(
          NimbusAccessTokenValidator
            .validate(new TypelessAccessToken(at.unwrap.getParsedString), jwsAlgorithm, atHash)
        )

      def getAccessTokenId(at: AccessToken): F[AccessTokenId] =
        AccessTokenId.parse(at.unwrap.getJWTClaimsSet.getJWTID).liftTo[F]

      for {
        at <- Token.parse(BearerAccessToken.parse(accessTokenHeader).getValue)(AccessToken)
        _  <- Token.checkClientId(at, config.jwt.audience)
        _  <- validateByNimbusValidator(at, accessTokenHash)
        id <- getAccessTokenId(at)
      } yield id
    }
  }
}

package app.token.validator

import app.config.AppConfig.SecurityConfig
import app.token.Token.IdToken
import app.token.{JwtValidationException, Token}
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.nimbusds.openid.connect.sdk.claims.{AccessTokenHash, IDTokenClaimsSet}
import com.nimbusds.openid.connect.sdk.validators.{IDTokenValidator => NimbusIdTokenValidator}

trait IdTokenValidator[F[_]] {
  def validate(idTokenHeader: String): F[AccessTokenHash]
}

object IdTokenValidator {

  def apply[F[_] : Sync](config: SecurityConfig): F[IdTokenValidator[F]] =
    Sync[F].delay(new Impl(config))

  private final class Impl[F[_] : Sync](config: SecurityConfig) extends IdTokenValidator[F] {

    def validate(idTokenHeader: String): F[AccessTokenHash] = {

      def createNimbusValidator(cid: String): F[NimbusIdTokenValidator] =
        NimbusValidatorFactory.create(config, cid)(new NimbusIdTokenValidator(_, _, _, _, _))

      def validateByNimbusValidator(vtor: NimbusIdTokenValidator, it: IdToken): F[IDTokenClaimsSet] =
        Sync[F].delay(vtor.validate(it.unwrap, null))

      def getAccessTokenHash(itcs: IDTokenClaimsSet): F[AccessTokenHash] =
        Sync[F].delay(itcs.getAccessTokenHash)

      for {
        it   <- Token.parse(idTokenHeader)(IdToken)
        cid  <- Token.getClientId(it, config.jwt.audience)
        vtor <- createNimbusValidator(cid)
        itcs <- validateByNimbusValidator(vtor, it)
        hash <- getAccessTokenHash(itcs)
      } yield hash
    }

    case class IdTokenValidationException(message: String) extends JwtValidationException(message)
  }
}

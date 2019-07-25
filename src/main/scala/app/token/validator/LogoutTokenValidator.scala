package app.token.validator

import app.config.AppConfig.SecurityConfig
import app.token.Token.LogoutToken
import app.token._
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.nimbusds.openid.connect.sdk.claims.LogoutTokenClaimsSet
import com.nimbusds.openid.connect.sdk.validators.{LogoutTokenValidator => NimbusLogoutTokenValidator}

trait LogoutTokenValidator[F[_]] {
  def validate(logoutTokenField: String): F[LogoutToken]
}

object LogoutTokenValidator {

  def apply[F[_] : Sync](config: SecurityConfig): F[LogoutTokenValidator[F]] =
    Sync[F].delay(new Impl(config))

  private final class Impl[F[_] : Sync](config: SecurityConfig) extends LogoutTokenValidator[F] {

    def validate(logoutTokenField: String): F[LogoutToken] = {

      def createNimbusValidator(cid: String): F[NimbusLogoutTokenValidator] =
        NimbusValidatorFactory.create(config, cid)(new NimbusLogoutTokenValidator(_, _, _, _, _))

      def validateByNimbusValidator(vtor: NimbusLogoutTokenValidator, lt: LogoutToken): F[LogoutTokenClaimsSet] =
        Sync[F].delay(vtor.validate(lt.unwrap))

      for {
        lt   <- Token.parse(logoutTokenField)(LogoutToken)
        cid  <- Token.getClientId(lt, config.jwt.audience)
        vtor <- createNimbusValidator(cid)
        _    <- validateByNimbusValidator(vtor, lt)
      } yield lt
    }
  }
}

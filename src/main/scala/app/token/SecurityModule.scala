package app.token

import java.time.Instant

import app.config.AppConfig.SecurityConfig
import app.newtypes.AccessTokenId
import app.service.BlacklistService
import app.token.Token.LogoutToken
import app.token.validator.{AccessTokenValidator, IdTokenValidator, LogoutTokenValidator}
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.monadError._
import cats.syntax.either._
import com.nimbusds.jwt.util.DateUtils
import net.minidev.json.JSONObject

class SecurityModule[F[_]](
  idTokenValidator: IdTokenValidator[F],
  accessTokenValidator: AccessTokenValidator[F],
  logoutTokenValidator: LogoutTokenValidator[F],
  blacklist: BlacklistService[F]
)(implicit F: Sync[F]) {

  def validate(idTokenHeader: String, accessTokenHeader: String): F[Unit] = {

    def checkBlacklist(id: AccessTokenId): F[Unit] =
      F.ifM(blacklist.contains(id))(F.raiseError(RevokedTokenException), F.unit)

    (for {
      hash <- idTokenValidator.validate(idTokenHeader)
      id   <- accessTokenValidator.validate(accessTokenHeader, hash)
      _    <- checkBlacklist(id)
    } yield ())
      .adaptError { case e => TokenValidationException(e.getMessage) }
  }

  def logout(logoutTokenField: String): F[Unit] = {

    def getAccessTokenData(lt: LogoutToken): F[JSONObject] =
      Sync[F].delay(lt.unwrap.getJWTClaimsSet.getJSONObjectClaim(AccessTokenData))

    def getAccessTokenExpiry(data: JSONObject): F[Instant] =
      Sync[F].delay(DateUtils.fromSecondsSinceEpoch(data.getAsNumber(AccessTokenExpire).longValue()).toInstant)

    def getAccessTokenId(data: JSONObject): F[AccessTokenId] =
      AccessTokenId.parse(data.getAsString(AccessTokenID)).liftTo[F]

    (for {
      lt   <- logoutTokenValidator.validate(logoutTokenField)
      data <- getAccessTokenData(lt)
      id   <- getAccessTokenId(data)
      exp  <- getAccessTokenExpiry(data)
      _    <- blacklist.add(id, exp)
    } yield ())
      .adaptError { case e => TokenValidationException(e.toString) }
  }

  case object RevokedTokenException extends JwtValidationException("Token has been revoked")

  case class TokenValidationException(message: String)
    extends JwtValidationException(s"Token validation error: $message")
}

object SecurityModule {
  def apply[F[_] : Sync](config: SecurityConfig, blacklist: BlacklistService[F]): F[SecurityModule[F]] =
    for {
      itv <- IdTokenValidator(config)
      atv <- AccessTokenValidator(config)
      ltv <- LogoutTokenValidator(config)
    } yield new SecurityModule(itv, atv, ltv, blacklist)
}

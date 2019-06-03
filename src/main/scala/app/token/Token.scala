package app.token

import cats.effect.Sync
import cats.syntax.option._
import com.nimbusds.jwt.SignedJWT

import scala.collection.JavaConverters._

sealed trait Token {
  def unwrap: SignedJWT
}

object Token {
  case class AccessToken(unwrap: SignedJWT) extends Token
  case class LogoutToken(unwrap: SignedJWT) extends Token
  case class IdToken(unwrap: SignedJWT)     extends Token

  def parse[F[_] : Sync, T <: Token](source: String)(f: SignedJWT => T): F[T] =
    Sync[F].catchNonFatal(f(SignedJWT.parse(source)))

  private def suitableClientId(token: Token): String => Boolean =
    clientId => token.unwrap.getJWTClaimsSet.getAudience.asScala.toList.contains(clientId)

  def getClientId[F[_] : Sync](token: Token, audience: List[String]): F[String] =
    audience.find(suitableClientId(token)).liftTo[F](WrongAudienceException)

  def checkClientId[F[_]](token: Token, audience: List[String])(implicit F: Sync[F]): F[Unit] =
    F.whenA(!audience.exists(suitableClientId(token)))(F.raiseError(WrongAudienceException))

  case object WrongAudienceException extends JwtValidationException("Given token is not issued for this application")
}

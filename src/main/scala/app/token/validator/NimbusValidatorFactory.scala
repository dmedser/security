package app.token.validator

import java.net.URL

import app.config.AppConfig.SecurityConfig
import app.token.jwsAlgorithm
import cats.effect.Sync
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.util.{DefaultResourceRetriever, ResourceRetriever}
import com.nimbusds.oauth2.sdk.id.{ClientID, Issuer}
import com.nimbusds.openid.connect.sdk.validators.AbstractJWTValidator

object NimbusValidatorFactory {
  def create[F[_] : Sync, V <: AbstractJWTValidator](config: SecurityConfig, clientID: String)(
    f: (Issuer, ClientID, JWSAlgorithm, URL, ResourceRetriever) => V
  ): F[V] = {
    import config._
    Sync[F].delay(
      f(
        new Issuer(jwt.issuer),
        new ClientID(clientID),
        jwsAlgorithm,
        new URL(jwk.url),
        new DefaultResourceRetriever(jwk.connectionTimeout, jwk.readTimeout, jwk.maxSize)
      )
    )
  }
}

package app.token.validator

import java.net.URL

import app.config.AppConfig.AuthConfig
import app.token.jwsAlgorithm
import cats.effect.Sync
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.util.{DefaultResourceRetriever, ResourceRetriever}
import com.nimbusds.oauth2.sdk.id.{ClientID, Issuer}
import com.nimbusds.openid.connect.sdk.validators.AbstractJWTValidator

object NimbusValidatorFactory {
  def create[F[_] : Sync, V <: AbstractJWTValidator](config: AuthConfig, clientID: String)(
    f: (Issuer, ClientID, JWSAlgorithm, URL, ResourceRetriever) => V
  ): F[V] = {
    import config.jwk._
    Sync[F].delay(
      f(
        new Issuer(issuer),
        new ClientID(clientID),
        jwsAlgorithm,
        new URL(url),
        new DefaultResourceRetriever(connectionTimeout, readTimeout, maxSize)
      )
    )
  }
}

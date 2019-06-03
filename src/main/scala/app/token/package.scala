package app

import com.nimbusds.jose.JWSAlgorithm

import scala.util.control.NoStackTrace

package object token {
  abstract class JwtValidationException(message: String) extends RuntimeException(message) with NoStackTrace
  val jwsAlgorithm: JWSAlgorithm = JWSAlgorithm.RS256
  val AccessTokenData = "at_data"
  val AccessTokenID = "jti"
  val AccessTokenExpire = "exp"
}

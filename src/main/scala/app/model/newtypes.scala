package app

import java.util.UUID

import cats.syntax.either._
import doobie.postgres.implicits._
import doobie.util.Meta
import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype
import scalaz.deriving

package object newtypes {
  @newtype
  @deriving(Decoder, Encoder, Meta)
  final case class AccessTokenId(unwrap: UUID)
  object AccessTokenId {

    def parse(uuid: String): Either[Throwable, AccessTokenId] =
      Either.catchNonFatal(AccessTokenId(UUID.fromString(uuid)))

    def randomAccessTokenId(): AccessTokenId = AccessTokenId(UUID.randomUUID())
  }
}

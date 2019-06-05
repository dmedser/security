package app.repository

import java.time.Instant

import app.model.AccessTokenId
import cats.effect.Sync
import doobie.free.connection.ConnectionIO

trait BlacklistRepository[DB[_]] {
  def add(id: AccessTokenId, expire: Instant): DB[Unit]
  def contains(id: AccessTokenId): DB[Boolean]
}

object BlacklistRepository {

  def create[F[_] : Sync]: F[BlacklistRepository[ConnectionIO]] = Sync[F].delay(new Impl)

  private class Impl extends BlacklistRepository[ConnectionIO] {
    def add(id: AccessTokenId, expire: Instant): ConnectionIO[Unit] = ???

    def contains(id: AccessTokenId): ConnectionIO[Boolean] = ???
  }

}

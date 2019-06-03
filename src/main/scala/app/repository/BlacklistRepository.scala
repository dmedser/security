package app.repository

import java.time.Instant

import app.model.AccessTokenId
import cats.effect.Sync
import doobie.free.connection.ConnectionIO

trait BlacklistRepository[DB[_]] {
  def contains(id: AccessTokenId): DB[Boolean]
  def add(id: AccessTokenId, expire: Instant): DB[Int]
}

object BlacklistRepository {

  def create[F[_] : Sync]: F[BlacklistRepository[ConnectionIO]] = Sync[F].delay(new Impl)

  private class Impl extends BlacklistRepository[ConnectionIO] {
    def contains(id: AccessTokenId): ConnectionIO[Boolean] = ???

    def add(id: AccessTokenId, expire: Instant): ConnectionIO[Int] = ???
  }

}

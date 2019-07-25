package app.repository

import java.time.Instant
import app.newtypes.AccessTokenId
import cats.effect.Sync
import doobie.free.connection.ConnectionIO
import doobie.syntax.string._

trait BlacklistRepository[DB[_]] {
  def contains(id: AccessTokenId): DB[Boolean]
  def add(id: AccessTokenId, expire: Instant): DB[Int]
}

object BlacklistRepository {

  def create[F[_] : Sync]: F[BlacklistRepository[ConnectionIO]] = Sync[F].delay(new Impl)

  private final class Impl extends BlacklistRepository[ConnectionIO] {

    def contains(id: AccessTokenId): ConnectionIO[Boolean] =
      sql"SELECT EXISTS (SELECT TRUE FROM blacklist WHERE ID = $id::UUID)".query[Boolean].unique

    def add(id: AccessTokenId, expire: Instant): ConnectionIO[Int] =
      sql"INSERT INTO blacklist (id, expire) VALUES ($id::UUID, $expire)".update.run
  }
}

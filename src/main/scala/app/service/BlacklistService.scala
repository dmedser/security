package app.service

import java.time.Instant

import app.newtypes.AccessTokenId
import app.repository.BlacklistRepository
import app.util.DbUtil._
import cats.effect.Sync
import cats.~>

trait BlacklistService[F[_]] {
  def contains(id: AccessTokenId): F[Boolean]
  def add(id: AccessTokenId, expire: Instant): F[Int]
}

object BlacklistService {

  def apply[F[_] : Sync, DB[_]](repository: BlacklistRepository[DB])(implicit xa: DB ~> F): F[BlacklistService[F]] =
    Sync[F].delay(new Impl(repository))

  private final class Impl[F[_] : Sync, DB[_]](repository: BlacklistRepository[DB])(implicit xa: DB ~> F)
    extends BlacklistService[F] {

    def contains(id: AccessTokenId): F[Boolean] = repository.contains(id).transact

    def add(id: AccessTokenId, expire: Instant): F[Int] = repository.add(id, expire).transact
  }
}

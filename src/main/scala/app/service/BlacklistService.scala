package app.service

import java.time.Instant

import app.model.AccessTokenId
import app.repository.BlacklistRepository
import cats.effect.Sync
import cats.~>
import mouse.anyf._

trait BlacklistService[F[_]] {
  def contains(id: AccessTokenId): F[Boolean]
  def add(id: AccessTokenId, expire: Instant): F[Int]
}

object BlacklistService {

  def create[F[_] : Sync, DB[_]](repository: BlacklistRepository[DB], xa: DB ~> F): F[BlacklistService[F]] =
    Sync[F].delay(new Impl(repository, xa))

  private class Impl[F[_] : Sync, DB[_]](repository: BlacklistRepository[DB], xa: DB ~> F) extends BlacklistService[F] {
    def contains(id: AccessTokenId): F[Boolean] = repository.contains(id) ||> xa

    def add(id: AccessTokenId, expire: Instant): F[Int] = repository.add(id, expire) ||> xa
  }

}

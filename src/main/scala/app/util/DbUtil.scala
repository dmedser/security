package app.util

import cats.~>
import mouse.anyf._

object DbUtil {
  implicit class TransactionResultUtilOps[F[_], DB[_], A](result: DB[A])(implicit xa: DB ~> F) {
    def transact: F[A] = result ||> xa
  }
}

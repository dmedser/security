package app.util

import cats.effect.Async
import monix.catnap.syntax._

import scala.concurrent.Future

object FutureUtil {
  implicit class FutureUtilOps[A](future: => Future[A]) {
    def liftTo[F[_] : Async]: F[A] = Async[F].delay(future).futureLift
  }
}

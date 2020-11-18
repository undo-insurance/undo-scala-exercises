package dk.undo.exercises

import scala.concurrent.ExecutionContext

import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import zio._
import zio.interop.catz._
import caliban.GraphQLInterpreter
import org.http4s.server.Router
import caliban.Http4sAdapter
import org.http4s.server.middleware.CORS
import cats.data.Kleisli
import org.http4s.StaticFile
import cats.effect.Blocker

object GraphQL {
  val serve =
    ZIO.runtime[App.Env].flatMap { implicit runtime =>
      App.interpreter.flatMap { interpreter =>
        blocking.blockingExecutor
          .map(_.asEC)
          .map(Blocker.liftExecutionContext)
          .flatMap { blocker =>
            BlazeServerBuilder[App.App](ExecutionContext.global)
              .bindHttp(8066, "0.0.0.0")
              .withHttpApp(
                Router[App.App](
                  "/api/graphql" -> CORS(
                    Http4sAdapter.makeHttpService(interpreter)
                  ),
                  "/ws/graphql" -> CORS(
                    Http4sAdapter.makeWebSocketService(interpreter)
                  ),
                  "/" -> Kleisli.liftF(
                    StaticFile.fromResource("/playground.html", blocker, None)
                  )
                ).orNotFound
              )
              .resource
              .toManaged
              .useForever
          }
      }
    }
}

package dk.undo.exercises

import zio._

object Main extends App {
  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    GraphQL.serve.exitCode
}

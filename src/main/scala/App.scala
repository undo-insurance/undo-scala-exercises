package dk.undo.exercises

import zio._
import zio.duration._
import zio.stream.ZStream
import caliban.GraphQLInterpreter
import caliban.GraphQL.graphQL
import caliban.GraphQL
import caliban.RootResolver
import caliban.schema.GenericSchema

object App {
  type Env = ZEnv

  type Error = Throwable

  type App[A] = ZIO[Env, Error, A]

  object schema extends GenericSchema[Env]
  import schema._

  case class User(id: Int)

  case class Queries(users: App[List[User]])
  val queries = Queries(ZIO.succeed(List(User(1))))

  case class Mutations(createUser: User => App[User])
  val mutations = Mutations(Task.succeed(_))

  case class Subscriptions(users: ZStream[Env, Error, User])
  val subscriptions = Subscriptions(users = ZStream.repeatWith(User(1), Schedule.fixed(1.second)))

  val api: GraphQL[Env] = graphQL(
    RootResolver(queries, mutations, subscriptions)
  )
  val interpreter: Task[GraphQLInterpreter[Env, Error]] = api.interpreter
}

package dk.undo.exercises

import caliban.GraphQL
import caliban.GraphQL.graphQL
import caliban.GraphQLInterpreter
import caliban.RootResolver
import caliban.schema.Annotations.GQLName
import caliban.schema.GenericSchema
import dk.undo.exercises.App.User.GetUser
import dk.undo.exercises.App.User.GetUsers
import zio._
import zio.duration._
import zio.query.CompletedRequestMap
import zio.query.DataSource.Batched
import zio.query.Request
import zio.query.ZQuery
import zio.stream.ZStream

object App {
  type Env = ZEnv

  type Error = Throwable

  type App[A] = ZIO[Env, Error, A]

  type AppQuery[A] = ZQuery[Env, Error, A]

  object schema extends GenericSchema[Env]
  import schema._

  case class User(id: Int)
  object User {
    sealed trait UserRequest

    case object GetUsers extends Request[Error, List[User]] with UserRequest

    case class GetUser(id: Int)
        extends Request[Error, Option[User]]
        with UserRequest

    val DataSource = Batched.make[Env, UserRequest]("User") { requests =>
      ZIO.foldLeft(requests)(CompletedRequestMap.empty) { (map, request) =>
        request match {
          case request: GetUsers.type =>
            console.putStrLn(request.toString).as {
              map.insert(request)(Right(List.empty))
            }
          case request @ GetUser(id) =>
            console.putStrLn(request.toString) as {
              map.insert(request)(Right(Some(User(id))))
            }
        }
      }
    }
  }

  case class Queries(
      users: AppQuery[List[User]],
      user: User.GetUser => AppQuery[Option[User]]
  )

  val queries = Queries(
    users = ZQuery.fromRequest(User.GetUsers)(User.DataSource),
    user = id => ZQuery.fromRequest(id)(User.DataSource)
  )

  case class Mutations(createUser: User => App[User])
  val mutations = Mutations(Task.succeed(_))

  case class Subscriptions(users: ZStream[Env, Error, User])
  val subscriptions =
    Subscriptions(users = ZStream.repeatWith(User(1), Schedule.fixed(1.second)))

  val api: GraphQL[Env] = graphQL(
    RootResolver(queries, mutations, subscriptions)
  )
  val interpreter: Task[GraphQLInterpreter[Env, Error]] = api.interpreter
}

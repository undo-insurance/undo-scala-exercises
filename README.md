# Undo Exercises
Ideas for tasks:
- Integrate doobie with ZIO
- SQL data modelling
- Validate GraphQL input with cats.data.Validated
- Build a realtime service, using GraphQL subscriptions
- Error handling that isn't based on `Throwable`

## Running
If you have SBT installed:
```shell
sbt --client run
```
If you do not have SBT installed:
```shell
./sbt run
```

This run an HTTP server on port `8066`, serving GraphQL on `/api/graphql` and GraphQL subscriptions on `/ws/graphql`.
For easy testing, GraphQL Playground is served on `/`

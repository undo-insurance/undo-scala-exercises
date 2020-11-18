package dk.undo.exercises

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.flywaydb.core.Flyway
import javax.sql.DataSource

object Doobie {
  // TODO: How do we handle the lifetime of this DataSource?
  def dataSource: DataSource = {
    val ds = EmbeddedPostgres.start.getPostgresDatabase()
    Flyway.configure.locations("db").dataSource(ds).load.migrate
    ds
  }
}

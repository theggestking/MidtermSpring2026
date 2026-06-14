import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class PersistenceSchemaTests {
    @Test
    void flywayCreatesSchemaAndHibernateValidatesMappings() throws Exception {
        DatabaseConfig config = DatabaseConfig.isolatedTestDatabase();

        try (PersistenceBootstrap ignored = PersistenceBootstrap.open(config);
                Connection connection = DriverManager.getConnection(config.url(), config.user(), config.password())) {
            DatabaseMetaData metadata = connection.getMetaData();
            Set<String> tables = new HashSet<>();
            try (ResultSet rows = metadata.getTables(null, "PUBLIC", "%", new String[] {"TABLE"})) {
                while (rows.next()) {
                    tables.add(rows.getString("TABLE_NAME").toLowerCase());
                }
            }

            assertTrue(tables.containsAll(Set.of(
                    "players", "games", "game_players", "rounds", "round_scores")));
            assertEquals(5, tables.stream().filter(name -> !name.equals("flyway_schema_history")).count());
        }
    }
}

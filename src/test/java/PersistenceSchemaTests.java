import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

            try (Statement statement = connection.createStatement();
                    ResultSet constraints = statement.executeQuery("""
                            SELECT CONSTRAINT_TYPE, COUNT(*) AS constraint_count
                            FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                            WHERE TABLE_SCHEMA = 'PUBLIC'
                              AND TABLE_NAME <> 'flyway_schema_history'
                            GROUP BY CONSTRAINT_TYPE
                            """)) {
                Map<String, Long> counts = new HashMap<>();
                while (constraints.next()) {
                    counts.put(
                            constraints.getString("CONSTRAINT_TYPE"),
                            constraints.getLong("constraint_count"));
                }

                assertEquals(5L, counts.get("PRIMARY KEY"));
                assertEquals(6L, counts.get("FOREIGN KEY"));
                assertEquals(5L, counts.get("UNIQUE"));
                assertEquals(9L, counts.get("CHECK"));
            }

            try (Statement statement = connection.createStatement();
                    ResultSet constraints = statement.executeQuery("""
                            SELECT CONSTRAINT_NAME
                            FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                            WHERE TABLE_SCHEMA = 'PUBLIC'
                            """)) {
                Set<String> names = new HashSet<>();
                while (constraints.next()) {
                    names.add(constraints.getString("CONSTRAINT_NAME").toLowerCase());
                }
                assertTrue(names.containsAll(Set.of(
                        "fk_game_players_game",
                        "fk_game_players_player",
                        "fk_rounds_game",
                        "fk_rounds_winner",
                        "fk_round_scores_round",
                        "fk_round_scores_player",
                        "uq_game_players_seat",
                        "uq_game_players_player",
                        "uq_rounds_number",
                        "uq_round_scores_player")));
            }
        }
    }
}

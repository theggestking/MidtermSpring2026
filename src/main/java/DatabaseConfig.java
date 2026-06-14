import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

record DatabaseConfig(String url, String user, String password) {
    private static final String DEFAULT_URL = "jdbc:h2:file:./data/uno-history";
    private static final String DEFAULT_USER = "sa";

    static DatabaseConfig fromEnvironment() {
        return new DatabaseConfig(
                environmentValue("UNO_DB_URL", DEFAULT_URL),
                environmentValue("UNO_DB_USER", DEFAULT_USER),
                environmentValue("UNO_DB_PASSWORD", ""));
    }

    static DatabaseConfig isolatedTestDatabase() {
        return new DatabaseConfig(
                "jdbc:h2:mem:uno-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1",
                DEFAULT_USER,
                "");
    }

    Map<String, Object> jpaProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.url", url);
        properties.put("jakarta.persistence.jdbc.user", user);
        properties.put("jakarta.persistence.jdbc.password", password);
        properties.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
        return properties;
    }

    void prepareStorage() {
        String prefix = "jdbc:h2:file:";
        if (!url.startsWith(prefix)) {
            return;
        }

        String databasePath = url.substring(prefix.length()).split(";", 2)[0];
        if (databasePath.startsWith("~")) {
            return;
        }

        Path parent = Path.of(databasePath).toAbsolutePath().getParent();
        if (parent == null) {
            return;
        }
        try {
            Files.createDirectories(parent);
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not create database directory", exception);
        }
    }

    private static String environmentValue(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}

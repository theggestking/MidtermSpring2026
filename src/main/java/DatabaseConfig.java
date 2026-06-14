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

    private static String environmentValue(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}

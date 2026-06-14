import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import org.flywaydb.core.Flyway;

final class PersistenceBootstrap implements AutoCloseable {
    private final EntityManagerFactory entityManagerFactory;

    private PersistenceBootstrap(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    static PersistenceBootstrap open(DatabaseConfig config) {
        Flyway.configure()
                .dataSource(config.url(), config.user(), config.password())
                .locations("classpath:db/migration")
                .load()
                .migrate();

        EntityManagerFactory factory = Persistence.createEntityManagerFactory(
                "uno-history", config.jpaProperties());
        return new PersistenceBootstrap(factory);
    }

    EntityManagerFactory entityManagerFactory() {
        return entityManagerFactory;
    }

    @Override
    public void close() {
        entityManagerFactory.close();
    }
}

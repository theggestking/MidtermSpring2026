import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;

class GameHistoryRepositoryTests {
    @Test
    void savesAndReloadsTheCompleteAggregate() {
        try (PersistenceBootstrap bootstrap = PersistenceBootstrap.open(DatabaseConfig.isolatedTestDatabase())) {
            GameHistoryRepository repository = new GameHistoryRepository(bootstrap.entityManagerFactory());
            CompletedGame game = completedGame(
                    Instant.parse("2026-06-01T12:00:00Z"),
                    12,
                    25,
                    true,
                    false);

            repository.save(game);

            try (EntityManager entityManager = bootstrap.entityManagerFactory().createEntityManager()) {
                GameEntity saved = entityManager.createQuery(
                        "select game from GameEntity game", GameEntity.class).getSingleResult();
                assertEquals(game.startedAt(), saved.startedAt());
                assertEquals(game.completedAt(), saved.completedAt());
                assertEquals(1, saved.completedRounds());
                assertEquals(2, saved.players().size());
                assertEquals(1, saved.rounds().size());

                RoundEntity round = saved.rounds().getFirst();
                assertEquals(RoundStatus.COMPLETED.name(), round.status());
                assertEquals("Bot2", round.winner().displayName());
                assertEquals(2, round.scores().size());
                assertEquals(25, round.awardedPoints());
                assertEquals(25, round.scores().get(1).scoreAfter());
            }
        }
    }

    @Test
    void reusesPlayersCaseInsensitivelyAcrossGames() {
        try (PersistenceBootstrap bootstrap = PersistenceBootstrap.open(DatabaseConfig.isolatedTestDatabase())) {
            GameHistoryRepository repository = new GameHistoryRepository(bootstrap.entityManagerFactory());
            repository.save(completedGame(
                    Instant.parse("2026-06-01T12:00:00Z"), 10, 20, false, true));
            repository.save(completedGameWithNames(
                    Instant.parse("2026-06-02T12:00:00Z"), "bot1", "BOT2", 30, 40, false, true));

            try (EntityManager entityManager = bootstrap.entityManagerFactory().createEntityManager()) {
                long playerCount = entityManager.createQuery(
                        "select count(player) from PlayerEntity player", Long.class).getSingleResult();
                long gameCount = entityManager.createQuery(
                        "select count(game) from GameEntity game", Long.class).getSingleResult();
                assertEquals(2, playerCount);
                assertEquals(2, gameCount);
            }
        }
    }

    @Test
    void failedSaveRollsBackTheWholeAggregate() {
        try (PersistenceBootstrap bootstrap = PersistenceBootstrap.open(DatabaseConfig.isolatedTestDatabase())) {
            GameHistoryRepository repository = new GameHistoryRepository(bootstrap.entityManagerFactory());
            CompletedGame invalid = new CompletedGame(
                    Instant.parse("2026-06-01T12:00:00Z"),
                    Instant.parse("2026-06-01T12:01:00Z"),
                    1,
                    List.of(),
                    List.of(
                            new FinalPlayerScore("Bot1", 0, 10, true),
                            new FinalPlayerScore("BOT1", 1, 10, true)));

            assertThrows(RuntimeException.class, () -> repository.save(invalid));

            try (EntityManager entityManager = bootstrap.entityManagerFactory().createEntityManager()) {
                assertEquals(0L, entityManager.createQuery(
                        "select count(game) from GameEntity game", Long.class).getSingleResult());
                assertEquals(0L, entityManager.createQuery(
                        "select count(player) from PlayerEntity player", Long.class).getSingleResult());
            }
        }
    }

    @Test
    void recentGamesAndWinCountsHandleOrderingLimitsCaseTiesAndUnknownPlayers() {
        try (PersistenceBootstrap bootstrap = PersistenceBootstrap.open(DatabaseConfig.isolatedTestDatabase())) {
            GameHistoryRepository repository = new GameHistoryRepository(bootstrap.entityManagerFactory());
            repository.save(completedGame(
                    Instant.parse("2026-06-01T12:00:00Z"), 100, 80, true, false));
            repository.save(completedGame(
                    Instant.parse("2026-06-03T12:00:00Z"), 90, 90, true, true));
            repository.save(completedGame(
                    Instant.parse("2026-06-02T12:00:00Z"), 60, 120, false, true));

            List<RecentGameReport> recent = repository.recentGames(2);

            assertEquals(2, recent.size());
            assertEquals(Instant.parse("2026-06-03T12:01:00Z"), recent.get(0).completedAt());
            assertEquals(Instant.parse("2026-06-02T12:01:00Z"), recent.get(1).completedAt());
            assertEquals(2L, repository.playerWinCount("bOt1"));
            assertEquals(2L, repository.playerWinCount("BOT2"));
            assertEquals(0L, repository.playerWinCount("Unknown"));
            assertThrows(IllegalArgumentException.class, () -> repository.recentGames(0));
            assertThrows(IllegalArgumentException.class, () -> repository.recentGames(101));
        }
    }

    @Test
    void highestScoresSortByScoreThenCompletionTimeAndApplyLimits() {
        try (PersistenceBootstrap bootstrap = PersistenceBootstrap.open(DatabaseConfig.isolatedTestDatabase())) {
            GameHistoryRepository repository = new GameHistoryRepository(bootstrap.entityManagerFactory());
            repository.save(completedGame(
                    Instant.parse("2026-06-01T12:00:00Z"), 100, 200, false, true));
            repository.save(completedGame(
                    Instant.parse("2026-06-03T12:00:00Z"), 200, 50, true, false));
            repository.save(completedGame(
                    Instant.parse("2026-06-02T12:00:00Z"), 200, 75, true, false));

            List<HighScoreReport> scores = repository.highestScores(3);

            assertEquals(List.of(200, 200, 200), scores.stream()
                    .map(HighScoreReport::finalScore)
                    .toList());
            assertEquals(Instant.parse("2026-06-03T12:01:00Z"), scores.get(0).completedAt());
            assertEquals(Instant.parse("2026-06-02T12:01:00Z"), scores.get(1).completedAt());
            assertEquals(Instant.parse("2026-06-01T12:01:00Z"), scores.get(2).completedAt());
            assertTrue(scores.get(0).winner());
            assertThrows(IllegalArgumentException.class, () -> repository.highestScores(101));
        }
    }

    private static CompletedGame completedGame(
            Instant startedAt, int bot1Score, int bot2Score, boolean bot1Winner, boolean bot2Winner) {
        return completedGameWithNames(
                startedAt, "Bot1", "Bot2", bot1Score, bot2Score, bot1Winner, bot2Winner);
    }

    private static CompletedGame completedGameWithNames(
            Instant startedAt,
            String bot1Name,
            String bot2Name,
            int bot1Score,
            int bot2Score,
            boolean bot1Winner,
            boolean bot2Winner) {
        Instant completedAt = startedAt.plusSeconds(60);
        int awardedPoints = Math.max(bot1Score, bot2Score);
        String roundWinner = bot1Score >= bot2Score ? bot1Name : bot2Name;
        List<RoundPlayerScore> roundScores = List.of(
                new RoundPlayerScore(bot1Name, 0, bot1Score, bot1Score),
                new RoundPlayerScore(bot2Name, 0, bot2Score, bot2Score));
        CompletedRound round = new CompletedRound(
                1,
                startedAt,
                completedAt,
                RoundStatus.COMPLETED,
                roundWinner,
                awardedPoints,
                roundScores);
        return new CompletedGame(
                startedAt,
                completedAt,
                1,
                List.of(round),
                List.of(
                        new FinalPlayerScore(bot1Name, 0, bot1Score, bot1Winner),
                        new FinalPlayerScore(bot2Name, 1, bot2Score, bot2Winner)));
    }
}

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

class CliReportTests {
    @Test
    void parsesGameplayAndReportModes() {
        CliOptions gameplay = CliOptions.parse(
                new String[] {"--bots", "2", "--games", "5", "--quiet", "--seed", "123"}, 999);
        assertEquals(CliMode.GAMEPLAY, gameplay.mode());
        assertEquals(2, gameplay.bots());
        assertEquals(5, gameplay.games());
        assertTrue(gameplay.quiet());
        assertEquals(123L, gameplay.seed());

        CliOptions recentDefault = CliOptions.parse(new String[] {"--recent-games"}, 999);
        assertEquals(CliMode.RECENT_GAMES, recentDefault.mode());
        assertEquals(10, recentDefault.reportLimit());

        CliOptions recentLimited = CliOptions.parse(new String[] {"--recent-games", "25"}, 999);
        assertEquals(25, recentLimited.reportLimit());

        CliOptions wins = CliOptions.parse(new String[] {"--player-wins", "bOt2"}, 999);
        assertEquals(CliMode.PLAYER_WINS, wins.mode());
        assertEquals("bOt2", wins.playerName());
    }

    @Test
    void rejectsInvalidOrCombinedReportArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> CliOptions.parse(new String[] {"--recent-games", "0"}, 1));
        assertThrows(IllegalArgumentException.class,
                () -> CliOptions.parse(new String[] {"--highest-scores", "101"}, 1));
        assertThrows(IllegalArgumentException.class,
                () -> CliOptions.parse(new String[] {"--recent-games", "--quiet"}, 1));
        assertThrows(IllegalArgumentException.class,
                () -> CliOptions.parse(new String[] {"--recent-games", "--highest-scores"}, 1));
        assertThrows(IllegalArgumentException.class,
                () -> CliOptions.parse(new String[] {"--player-wins"}, 1));
        assertThrows(IllegalArgumentException.class,
                () -> CliOptions.parse(new String[] {"--unknown"}, 1));
    }

    @Test
    void rendersDedicatedReportRecords() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        HistoryReportView view = new HistoryReportView(
                new PrintStream(bytes, true, StandardCharsets.UTF_8));
        Instant completedAt = Instant.parse("2026-06-15T10:00:00Z");
        RecentGameReport game = new RecentGameReport(
                7,
                completedAt,
                5,
                5,
                List.of(
                        new FinalPlayerScore("Bot1", 0, 138, false),
                        new FinalPlayerScore("Bot2", 1, 246, true),
                        new FinalPlayerScore("Bot3", 2, 98, false)));

        view.showRecentGames(List.of(game));
        view.showPlayerWins("Bot2", 3);
        view.showHighestScores(List.of(
                new HighScoreReport(7, "Bot2", 246, completedAt, true)));

        String output = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Game 7"));
        assertTrue(output.contains("rounds=5/5"));
        assertTrue(output.contains("winners=Bot2"));
        assertTrue(output.contains("Bot1=138, Bot2=246, Bot3=98"));
        assertTrue(output.contains("Wins for Bot2: 3"));
        assertTrue(output.contains("Bot2: 246 | game=7"));
    }

    @Test
    void reportControllerUsesRepositoryQueries() {
        try (PersistenceBootstrap bootstrap = PersistenceBootstrap.open(DatabaseConfig.isolatedTestDatabase())) {
            GameHistoryRepository repository = new GameHistoryRepository(bootstrap.entityManagerFactory());
            repository.save(sampleGame());
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            GameHistoryReportController controller = new GameHistoryReportController(
                    repository,
                    new HistoryReportView(new PrintStream(bytes, true, StandardCharsets.UTF_8)));

            controller.show(CliOptions.parse(new String[] {"--recent-games", "1"}, 1));
            controller.show(CliOptions.parse(new String[] {"--player-wins", "BOT2"}, 1));
            controller.show(CliOptions.parse(new String[] {"--highest-scores", "1"}, 1));

            String output = bytes.toString(StandardCharsets.UTF_8);
            assertTrue(output.contains("Recent games:"));
            assertTrue(output.contains("Wins for BOT2: 1"));
            assertTrue(output.contains("Highest scores:"));
            assertTrue(output.contains("Bot2: 246"));
        }
    }

    private static CompletedGame sampleGame() {
        Instant startedAt = Instant.parse("2026-06-15T09:59:00Z");
        Instant completedAt = Instant.parse("2026-06-15T10:00:00Z");
        return new CompletedGame(
                startedAt,
                completedAt,
                1,
                List.of(new CompletedRound(
                        1,
                        startedAt,
                        completedAt,
                        RoundStatus.COMPLETED,
                        "Bot2",
                        246,
                        List.of(
                                new RoundPlayerScore("Bot1", 0, 138, 138),
                                new RoundPlayerScore("Bot2", 0, 246, 246),
                                new RoundPlayerScore("Bot3", 0, 98, 98)))),
                List.of(
                        new FinalPlayerScore("Bot1", 0, 138, false),
                        new FinalPlayerScore("Bot2", 1, 246, true),
                        new FinalPlayerScore("Bot3", 2, 98, false)));
    }
}

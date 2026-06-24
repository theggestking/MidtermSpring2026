import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

class GameSessionPersistenceTests {
    @Test
    void seededFiveRoundSessionPersistsExpectedScoresAndWinner() {
        GameState state = new GameState();
        state.addPlayer("Bot1", false);
        state.addPlayer("Bot2", false);
        state.addPlayer("Bot3", false);
        Clock clock = Clock.fixed(Instant.parse("2026-06-15T00:00:00Z"), ZoneOffset.UTC);
        TurnController turnController = new TurnController(
                state, new Random(123), new NoOpView(), true, new BotStrategy(), clock);
        GameSessionController sessionController = new GameSessionController(
                state, turnController, new NoOpView(), true, clock);

        CompletedGame game = sessionController.play(5);

        assertEquals(5, game.rounds().size());
        assertEquals(List.of(156, 133, 17), game.finalScores().stream()
                .map(FinalPlayerScore::finalScore)
                .toList());
        assertEquals(List.of("Bot1"), game.finalScores().stream()
                .filter(FinalPlayerScore::winner)
                .map(FinalPlayerScore::playerName)
                .toList());

        try (PersistenceBootstrap bootstrap = PersistenceBootstrap.open(DatabaseConfig.isolatedTestDatabase())) {
            GameHistoryRepository repository = new GameHistoryRepository(bootstrap.entityManagerFactory());
            repository.save(game);

            RecentGameReport saved = repository.recentGames(1).getFirst();
            assertEquals(5, saved.completedRounds());
            assertEquals(List.of(156, 133, 17), saved.finalScores().stream()
                    .map(FinalPlayerScore::finalScore)
                    .toList());
            assertEquals(1L, repository.playerWinCount("bot1"));
            assertFalse(repository.highestScores(3).isEmpty());
        }
    }

    @Test
    void finalScoreTiesMarkEveryLeaderAsWinner() {
        GameState state = new GameState();
        state.addPlayer("Bot1", false);
        state.addPlayer("Bot2", false);
        state.addScoreToPlayer(0, 25);
        state.addScoreToPlayer(1, 25);
        List<CompletedRound> rounds = List.of(new CompletedRound(
                1,
                Instant.EPOCH,
                Instant.EPOCH,
                RoundStatus.COMPLETED,
                "Bot1",
                25,
                List.of(
                        new RoundPlayerScore("Bot1", 0, 25, 25),
                        new RoundPlayerScore("Bot2", 0, 25, 25))));

        GameSessionController controller = new GameSessionController(
                state,
                new StubTurnController(state, rounds.getFirst()),
                new NoOpView(),
                true,
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        CompletedGame game = controller.play(1);

        assertTrue(game.finalScores().stream().allMatch(FinalPlayerScore::winner));
    }

    @Test
    void allSafetyLimitRoundsHaveNoFinalWinner() {
        GameState state = new GameState();
        state.addPlayer("Bot1", false);
        state.addPlayer("Bot2", false);
        CompletedRound safetyLimit = new CompletedRound(
                1,
                Instant.EPOCH,
                Instant.EPOCH,
                RoundStatus.SAFETY_LIMIT,
                null,
                0,
                List.of(
                        new RoundPlayerScore("Bot1", 0, 0, 0),
                        new RoundPlayerScore("Bot2", 0, 0, 0)));

        GameSessionController controller = new GameSessionController(
                state,
                new StubTurnController(state, safetyLimit),
                new NoOpView(),
                true,
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        CompletedGame game = controller.play(1);

        assertTrue(game.finalScores().stream().noneMatch(FinalPlayerScore::winner));
    }

    private static final class StubTurnController extends TurnController {
        private final CompletedRound round;

        StubTurnController(GameState state, CompletedRound round) {
            super(state, new Random(1), new NoOpView(), true);
            this.round = round;
        }

        @Override
        CompletedRound playRound(int roundNumber) {
            return round;
        }
    }

    private static class NoOpView implements GameView {
        public void showGameHeader(int gameNumber) {
        }

        public void showFinalScores(GameState state) {
        }

        public void showTurn(String upCard, String calledColor, String name, List<Card> hand) {
        }

        public String askMoveInput() {
            return "DRAW";
        }

        public void showIllegalSelection() {
        }

        public void showCardNotFound() {
        }

        public String askColor() {
            return "R";
        }

        public boolean askPlayDrawnCard(String drawn) {
            return false;
        }

        public void showDraw(String name, String drawn) {
        }

        public void showInvalidIndexPenalty(String name) {
        }

        public void showIllegalCardPenalty(String name, String card) {
        }

        public void showPlayedCard(String name, String card) {
        }

        public void showCalledColor(String name, String calledColor) {
        }

        public void showUno(String name) {
        }

        public void showWin(String name, int points) {
        }

        public void showEffectMessage(String message) {
        }

        public void showSafetyLimit() {
        }
    }
}

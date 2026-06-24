import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

class TargetScoreSessionTests {
    @Test
    void parsesTargetScoreModeAsGameplayAlternativeToGames() {
        CliOptions options = CliOptions.parse(
                new String[]{"--bots", "3", "--target-score", "100", "--quiet", "--seed", "123"}, 999);

        assertEquals(CliMode.GAMEPLAY, options.mode());
        assertTrue(options.usesTargetScore());
        assertEquals(100, options.targetScore());
        assertEquals(3, options.bots());
        assertTrue(options.quiet());
        assertEquals(123L, options.seed());
    }

    @Test
    void rejectsInvalidOrCombinedTargetScoreArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> CliOptions.parse(new String[]{"--target-score", "0"}, 1));
        assertThrows(IllegalArgumentException.class,
                () -> CliOptions.parse(new String[]{"--games", "5", "--target-score", "100"}, 1));
        assertThrows(IllegalArgumentException.class,
                () -> CliOptions.parse(new String[]{"--recent-games", "--target-score", "100"}, 1));
    }

    @Test
    void targetScoreSessionStopsWhenTargetIsReached() {
        GameState state = new GameState();
        state.addPlayer("Bot1", false);
        state.addPlayer("Bot2", false);
        Clock clock = Clock.fixed(Instant.parse("2026-06-15T00:00:00Z"), ZoneOffset.UTC);
        GameSessionController controller = new GameSessionController(
                state,
                new StubScoringTurnController(state, 30, 25, 100),
                new NoopView(),
                true,
                clock);

        CompletedGame game = controller.playToTargetScore(50);

        assertEquals(2, game.rounds().size());
        assertEquals(2, game.requestedRounds());
        assertEquals(55, game.finalScores().getFirst().finalScore());
        assertTrue(game.finalScores().getFirst().winner());
    }

    private static final class StubScoringTurnController extends TurnController {
        private final GameState state;
        private final int[] roundScores;

        private StubScoringTurnController(GameState state, int... roundScores) {
            super(state, new Random(1), new NoopView(), true);
            this.state = state;
            this.roundScores = roundScores;
        }

        @Override
        CompletedRound playRound(int roundNumber) {
            int before = state.scoreForPlayer(0);
            int delta = roundScores[roundNumber - 1];
            state.addScoreToPlayer(0, delta);
            return new CompletedRound(
                    roundNumber,
                    Instant.EPOCH,
                    Instant.EPOCH,
                    RoundStatus.COMPLETED,
                    "Bot1",
                    delta,
                    List.of(
                            new RoundPlayerScore("Bot1", before, delta, before + delta),
                            new RoundPlayerScore("Bot2", 0, 0, 0)));
        }
    }

    private static final class NoopView implements GameView {
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

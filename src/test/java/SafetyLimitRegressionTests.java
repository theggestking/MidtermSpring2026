import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

class SafetyLimitRegressionTests {
    @Test
    void botChoosesPlayableReverseCard() {
        BotStrategy strategy = new BotStrategy();
        ArrayList<Card> hand = new ArrayList<>();
        hand.add(Card.from("BR"));
        hand.add(Card.from("W"));

        assertEquals(0, strategy.chooseCard(hand, "RR", ""));
    }

    @Test
    void sampledBotOnlySeedOneFinishesNormally() {
        CompletedRound round = playSingleBotRound(1);

        assertEquals(RoundStatus.COMPLETED, round.status());
        assertTrue(round.awardedPoints() > 0);
        assertTrue(round.winnerName() != null && !round.winnerName().isBlank());
    }

    @Test
    void sampledBotOnlySeedTwoFinishesNormally() {
        CompletedRound round = playSingleBotRound(2);

        assertEquals(RoundStatus.COMPLETED, round.status());
        assertTrue(round.awardedPoints() > 0);
        assertTrue(round.winnerName() != null && !round.winnerName().isBlank());
    }

    private static CompletedRound playSingleBotRound(long seed) {
        GameState state = new GameState();
        state.addPlayer("Bot1", false);
        state.addPlayer("Bot2", false);
        state.addPlayer("Bot3", false);

        TurnController controller = new TurnController(
                state,
                new Random(seed),
                new NoopView(),
                true,
                new BotStrategy(),
                Clock.systemUTC());
        return controller.playRound(1);
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

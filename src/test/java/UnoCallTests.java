import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

class UnoCallTests {
    @Test
    void botCallsUnoAutomaticallyWhenReachingOneCard() {
        GameState state = twoPlayerState(false);
        state.setUpCard(Card.from("R9"));
        state.addCardToPlayer(0, Card.from("R4"));
        state.addCardToPlayer(0, Card.from("B5"));
        UnoView view = new UnoView(true);
        TurnController controller = new TurnController(state, new Random(1), view, false, new BotStrategy());

        boolean endedGame = controller.resolveChosenCard(0, "Bot1");

        assertFalse(endedGame);
        assertEquals(1, state.handSize(0));
        assertTrue(view.unoShown);
        assertFalse(view.penaltyShown);
    }

    @Test
    void humanCallUnoAvoidsPenalty() {
        GameState state = twoPlayerState(true);
        state.setUpCard(Card.from("R9"));
        state.addCardToPlayer(0, Card.from("R4"));
        state.addCardToPlayer(0, Card.from("B5"));
        UnoView view = new UnoView(true);
        TurnController controller = new TurnController(state, new Random(1), view, false, new BotStrategy());

        boolean endedGame = controller.resolveChosenCard(0, "You");

        assertFalse(endedGame);
        assertEquals(1, state.handSize(0));
        assertTrue(view.unoPrompted);
        assertTrue(view.unoShown);
        assertFalse(view.penaltyShown);
    }

    @Test
    void missedHumanUnoCallDrawsTwoPenaltyCards() {
        GameState state = twoPlayerState(true);
        state.setUpCard(Card.from("R9"));
        state.addCardToPlayer(0, Card.from("R4"));
        state.addCardToPlayer(0, Card.from("B5"));
        state.clearDeck();
        state.addToDeck(Card.from("G1"));
        state.addToDeck(Card.from("G2"));
        UnoView view = new UnoView(false);
        TurnController controller = new TurnController(state, new Random(1), view, false, new BotStrategy());

        boolean endedGame = controller.resolveChosenCard(0, "You");

        assertFalse(endedGame);
        assertEquals(3, state.handSize(0));
        assertEquals("B5", state.cardInHand(0, 0).code());
        assertEquals("G1", state.cardInHand(0, 1).code());
        assertEquals("G2", state.cardInHand(0, 2).code());
        assertTrue(view.unoPrompted);
        assertFalse(view.unoShown);
        assertTrue(view.penaltyShown);
    }

    private static GameState twoPlayerState(boolean human) {
        GameState state = new GameState();
        state.addPlayer(human ? "You" : "Bot1", human);
        state.addPlayer("Bot2", false);
        state.setCurrentPlayer(0);
        state.setDirection(1);
        return state;
    }

    private static final class UnoView implements GameView {
        private final boolean callUno;
        private boolean unoPrompted;
        private boolean unoShown;
        private boolean penaltyShown;

        private UnoView(boolean callUno) {
            this.callUno = callUno;
        }

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

        public boolean askCallUno() {
            unoPrompted = true;
            return callUno;
        }

        public void showUno(String name) {
            unoShown = true;
        }

        public void showMissedUnoPenalty(String name) {
            penaltyShown = true;
        }

        public void showWin(String name, int points) {
        }

        public void showEffectMessage(String message) {
        }

        public void showSafetyLimit() {
        }
    }
}

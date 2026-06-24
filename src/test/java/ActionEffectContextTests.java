import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ActionEffectContextTests {
    @Test
    void drawTwoUsesNarrowTurnEffectContext() {
        FakeEffectContext context = new FakeEffectContext(3);

        String message = ActionEffects.apply(Card.from("R+2"), context);

        assertEquals("Bot2 draws two.", message);
        assertEquals(2, context.currentPlayerIndex());
        assertEquals(2, context.cardsDrawnByPlayer(1));
    }

    @Test
    void reverseUsesNarrowTurnEffectContext() {
        FakeEffectContext context = new FakeEffectContext(3);

        ActionEffects.apply(Card.from("RR"), context);

        assertEquals(-1, context.direction());
        assertEquals(2, context.currentPlayerIndex());
    }

    private static final class FakeEffectContext implements TurnEffectContext {
        private final int[] drawnCards;
        private int currentPlayer;
        private int direction = 1;

        private FakeEffectContext(int playerCount) {
            this.drawnCards = new int[playerCount];
        }

        public int playerCount() {
            return drawnCards.length;
        }

        public int currentPlayerIndex() {
            return currentPlayer;
        }

        public String currentPlayerName() {
            return "Bot" + (currentPlayer + 1);
        }

        public int direction() {
            return direction;
        }

        public void reverseDirection() {
            direction *= -1;
        }

        public void nextPlayer() {
            currentPlayer += direction;
            if (currentPlayer >= drawnCards.length) {
                currentPlayer = 0;
            }
            if (currentPlayer < 0) {
                currentPlayer = drawnCards.length - 1;
            }
        }

        public void drawCardsForCurrentPlayer(int count) {
            drawnCards[currentPlayer] += count;
        }

        int cardsDrawnByPlayer(int player) {
            return drawnCards[player];
        }
    }
}

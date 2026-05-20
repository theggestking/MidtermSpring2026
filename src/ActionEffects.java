import java.util.function.Supplier;

public class ActionEffects {
    private ActionEffects() {
    }

    static String apply(Card card, GameState state, Supplier<Card> drawCard) {
        String cardRank = card.rank();

        if (cardRank.equals("SKIP")) {
            return applySkip(state);
        }
        if (cardRank.equals("REVERSE")) {
            return applyReverse(state);
        }
        if (cardRank.equals("DRAW_TWO")) {
            return applyDrawTwo(state, drawCard);
        }
        if (cardRank.equals("WILD_DRAW_FOUR")) {
            return applyWildDrawFour(state, drawCard);
        }

        state.nextPlayer();
        return "";
    }

    private static String applySkip(GameState state) {
        state.nextPlayer();
        state.nextPlayer();
        return "";
    }

    private static String applyReverse(GameState state) {
        state.setDirection(state.direction() * -1);
        if (state.playerCount() == 2) {
            state.nextPlayer();
            state.nextPlayer();
        } else {
            state.nextPlayer();
        }
        return "";
    }

    private static String applyDrawTwo(GameState state, Supplier<Card> drawCard) {
        state.nextPlayer();
        state.addCardToCurrentPlayer(drawCard.get());
        state.addCardToCurrentPlayer(drawCard.get());
        String message = state.currentPlayerName() + " draws two.";
        state.nextPlayer();
        return message;
    }

    private static String applyWildDrawFour(GameState state, Supplier<Card> drawCard) {
        state.nextPlayer();
        for (int i = 0; i < 4; i++) {
            state.addCardToCurrentPlayer(drawCard.get());
        }
        String message = state.currentPlayerName() + " draws four.";
        state.nextPlayer();
        return message;
    }
}
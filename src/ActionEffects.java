import java.util.function.Supplier;

public class ActionEffects {
    private ActionEffects() {
    }

    static String apply(String card, GameState state, Supplier<String> drawCard) {
        String cardRank = CardRules.rank(card);

        if (cardRank.equals("SKIP")) {
            state.nextPlayer();
            state.nextPlayer();
            return "";
        } else if (cardRank.equals("REVERSE")) {
            state.direction = state.direction * -1;
            if (state.playerNames.size() == 2) {
                state.nextPlayer();
                state.nextPlayer();
            } else {
                state.nextPlayer();
            }
            return "";
        } else if (cardRank.equals("DRAW_TWO")) {
            state.nextPlayer();
            state.hands.get(state.currentPlayer).add(drawCard.get());
            state.hands.get(state.currentPlayer).add(drawCard.get());
            String message = state.playerNames.get(state.currentPlayer) + " draws two.";
            state.nextPlayer();
            return message;
        } else if (cardRank.equals("WILD_DRAW_FOUR")) {
            state.nextPlayer();
            for (int i = 0; i < 4; i++) {
                state.hands.get(state.currentPlayer).add(drawCard.get());
            }
            String message = state.playerNames.get(state.currentPlayer) + " draws four.";
            state.nextPlayer();
            return message;
        } else {
            state.nextPlayer();
            return "";
        }
    }
}
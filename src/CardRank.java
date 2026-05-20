import java.util.function.Supplier;

enum CardRank {
    NUMBER {
        String applyEffect(GameState state, Supplier<Card> drawCard) {
            state.nextPlayer();
            return "";
        }
    },

    SKIP {
        String applyEffect(GameState state, Supplier<Card> drawCard) {
            state.nextPlayer();
            state.nextPlayer();
            return "";
        }
    },

    REVERSE {
        String applyEffect(GameState state, Supplier<Card> drawCard) {
            state.setDirection(state.direction() * -1);
            if (state.playerCount() == 2) {
                state.nextPlayer();
                state.nextPlayer();
            } else {
                state.nextPlayer();
            }
            return "";
        }
    },

    DRAW_TWO {
        String applyEffect(GameState state, Supplier<Card> drawCard) {
            state.nextPlayer();
            state.addCardToCurrentPlayer(drawCard.get());
            state.addCardToCurrentPlayer(drawCard.get());
            String message = state.currentPlayerName() + " draws two.";
            state.nextPlayer();
            return message;
        }
    },

    WILD {
        String applyEffect(GameState state, Supplier<Card> drawCard) {
            state.nextPlayer();
            return "";
        }
    },

    WILD_DRAW_FOUR {
        String applyEffect(GameState state, Supplier<Card> drawCard) {
            state.nextPlayer();
            for (int i = 0; i < 4; i++) {
                state.addCardToCurrentPlayer(drawCard.get());
            }
            String message = state.currentPlayerName() + " draws four.";
            state.nextPlayer();
            return message;
        }
    };

    abstract String applyEffect(GameState state, Supplier<Card> drawCard);
}

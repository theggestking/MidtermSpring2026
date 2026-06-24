enum CardRank {
    NUMBER {
        String applyEffect(TurnEffectContext context) {
            context.nextPlayer();
            return "";
        }
    },

    SKIP {
        String applyEffect(TurnEffectContext context) {
            context.nextPlayer();
            context.nextPlayer();
            return "";
        }
    },

    REVERSE {
        String applyEffect(TurnEffectContext context) {
            context.reverseDirection();
            if (context.playerCount() == 2) {
                context.nextPlayer();
                context.nextPlayer();
            } else {
                context.nextPlayer();
            }
            return "";
        }
    },

    DRAW_TWO {
        String applyEffect(TurnEffectContext context) {
            context.nextPlayer();
            context.drawCardsForCurrentPlayer(2);
            String message = context.currentPlayerName() + " draws two.";
            context.nextPlayer();
            return message;
        }
    },

    WILD {
        String applyEffect(TurnEffectContext context) {
            context.nextPlayer();
            return "";
        }
    },

    WILD_DRAW_FOUR {
        String applyEffect(TurnEffectContext context) {
            context.nextPlayer();
            context.drawCardsForCurrentPlayer(4);
            String message = context.currentPlayerName() + " draws four.";
            context.nextPlayer();
            return message;
        }
    };

    abstract String applyEffect(TurnEffectContext context);
}

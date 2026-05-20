import java.util.Random;

public class TurnResolver {
    private final GameState state;
    private final Random random;
    private final GameView view;
    private final boolean quiet;

    TurnResolver(GameState state, Random random, GameView view, boolean quiet) {
        this.state = state;
        this.random = random;
        this.view = view;
        this.quiet = quiet;
    }

    boolean resolveChosenCard(int chosen, String name) {
        if (chosen < 0) {
            state.nextPlayer();
            return false;
        }

        if (chosen >= state.currentHandSize()) {
            if (!quiet) {
                view.showInvalidIndexPenalty(name);
            }
            state.addCardToCurrentPlayer(state.draw(random));
            state.nextPlayer();
            return false;
        }

        Card card = state.cardInCurrentHand(chosen);
        boolean ok = CardRules.isLegal(card.code(), state.upCardCode(), state.calledColor());

        if (!ok) {
            if (!quiet) {
                view.showIllegalCardPenalty(name, card.code());
            }
            state.addCardToCurrentPlayer(state.draw(random));
            state.nextPlayer();
            return false;
        }

        state.removeCardFromCurrentHand(chosen);
        state.discardUpCard();
        state.setUpCard(card);
        state.clearCalledColor();

        if (!quiet) {
            view.showPlayedCard(name, card.code());
        }

        callColorIfWild(card, name);

        if (state.currentHandSize() == 1 && !quiet) {
            view.showUno(name);
        }

        if (state.currentHandSize() == 0) {
            scoreWin(name);
            return true;
        }

        applyCardEffect(card);
        return false;
    }

    private void callColorIfWild(Card card, String name) {
        if (!card.isWild()) {
            return;
        }

        if (state.isCurrentPlayerHuman()) {
            state.setCalledColor(view.askColor());
        } else {
            state.setCalledColor(BotStrategy.chooseColor(state.currentHandSnapshot()));
        }

        if (!quiet) {
            view.showCalledColor(name, state.calledColor());
        }
    }

    private void scoreWin(String name) {
        int points = ScoreCalculator.scoreForWinner(state.handsSnapshot(), state.currentPlayerIndex());
        state.addScoreToPlayer(state.currentPlayerIndex(), points);

        if (!quiet) {
            view.showWin(name, points);
        }
    }

    private void applyCardEffect(Card card) {
        String effectMessage = ActionEffects.apply(card, state, () -> state.draw(random));
        if (!quiet && !effectMessage.equals("")) {
            view.showEffectMessage(effectMessage);
        }
    }
}
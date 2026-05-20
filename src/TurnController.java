import java.util.ArrayList;
import java.util.Random;

public class TurnController {
    private final GameState state;
    private final Random random;
    private final ConsoleView view;
    private final boolean quiet;

    TurnController(GameState state, Random random, ConsoleView view, boolean quiet) {
        this.state = state;
        this.random = random;
        this.view = view;
        this.quiet = quiet;
    }

    void playGame() {
        startNewGame();

        int guard = 0;
        while (guard < 3000) {
            guard++;
            if (takeTurn()) {
                return;
            }
        }

        if (!quiet) {
            view.showSafetyLimit();
        }
    }

    boolean takeTurn() {
        String name = state.currentPlayerName();
        ArrayList<Card> hand = state.currentHandSnapshot();

        renderTurn(name, hand);
        int chosen = chooseMove(hand);
        chosen = handleDrawIfNeeded(chosen, name);
        return resolveChosenCard(chosen, name);
    }

    private void renderTurn(String name, ArrayList<Card> hand) {
        if (!quiet) {
            view.showTurn(state.upCardCode(), state.calledColor(), name, hand);
        }
    }

    private int chooseMove(ArrayList<Card> hand) {
        if (state.isCurrentPlayerHuman()) {
            return view.askHuman(hand, state.upCardCode(), state.calledColor());
        }
        return BotStrategy.chooseCard(hand, state.upCardCode(), state.calledColor());
    }

    int handleDrawIfNeeded(int chosen, String name) {
        if (chosen != -1) {
            return chosen;
        }

        Card drawn = state.draw(random);
        state.addCardToCurrentPlayer(drawn);

        if (!quiet) {
            view.showDraw(name, drawn.code());
        }

        if (CardRules.isLegal(drawn.code(), state.upCardCode(), state.calledColor())) {
            if (!state.isCurrentPlayerHuman()) {
                return state.currentHandSize() - 1;
            }

            if (view.askPlayDrawnCard(drawn.code())) {
                return state.currentHandSize() - 1;
            }
        }

        return chosen;
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

        if (card.isWild()) {
            if (state.isCurrentPlayerHuman()) {
                state.setCalledColor(view.askColor());
            } else {
                state.setCalledColor(BotStrategy.chooseColor(state.currentHandSnapshot()));
            }
            if (!quiet) {
                view.showCalledColor(name, state.calledColor());
            }
        }

        if (state.currentHandSize() == 1 && !quiet) {
            view.showUno(name);
        }

        if (state.currentHandSize() == 0) {
            int points = ScoreCalculator.scoreForWinner(state.handsSnapshot(), state.currentPlayerIndex());
            state.addScoreToPlayer(state.currentPlayerIndex(), points);
            if (!quiet) {
                view.showWin(name, points);
            }
            return true;
        }

        String effectMessage = ActionEffects.apply(card, state, () -> state.draw(random));
        if (!quiet && !effectMessage.equals("")) {
            view.showEffectMessage(effectMessage);
        }

        return false;
    }

    void startNewGame() {
        state.buildDeck(random);
        state.clearDiscard();
        state.clearHands();

        for (int i = 0; i < state.playerCount(); i++) {
            for (int j = 0; j < 7; j++) {
                state.addCardToPlayer(i, state.draw(random));
            }
        }

        state.setUpCard(state.draw(random));
        while (state.isUpCardWild()) {
            state.discardUpCard();
            state.setUpCard(state.draw(random));
        }

        state.clearCalledColor();
        state.resetDirection();
        state.chooseRandomCurrentPlayer(random);
    }
}
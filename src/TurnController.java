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
        String name = state.playerNames.get(state.currentPlayer);
        ArrayList<Card> hand = state.hands.get(state.currentPlayer);

        renderTurn(name, hand);
        int chosen = chooseMove(hand);
        chosen = handleDrawIfNeeded(chosen, hand, name);
        return resolveChosenCard(chosen, hand, name);
    }

    private void renderTurn(String name, ArrayList<Card> hand) {
        if (!quiet) {
            view.showTurn(state.upCardCode(), state.calledColor, name, hand);
        }
    }

    private int chooseMove(ArrayList<Card> hand) {
        if (state.humanPlayers.get(state.currentPlayer).booleanValue()) {
            return view.askHuman(hand, state.upCardCode(), state.calledColor);
        }
        return BotStrategy.chooseCard(hand, state.upCardCode(), state.calledColor);
    }

    int handleDrawIfNeeded(int chosen, ArrayList<Card> hand, String name) {
        if (chosen != -1) {
            return chosen;
        }

        Card drawn = state.draw(random);
        hand.add(drawn);

        if (!quiet) {
            view.showDraw(name, drawn.code());
        }

        if (CardRules.isLegal(drawn.code(), state.upCardCode(), state.calledColor)) {
            if (!state.humanPlayers.get(state.currentPlayer).booleanValue()) {
                return hand.size() - 1;
            }

            if (view.askPlayDrawnCard(drawn.code())) {
                return hand.size() - 1;
            }
        }

        return chosen;
    }

    boolean resolveChosenCard(int chosen, ArrayList<Card> hand, String name) {
        if (chosen < 0) {
            state.nextPlayer();
            return false;
        }

        if (chosen >= hand.size()) {
            if (!quiet) {
                view.showInvalidIndexPenalty(name);
            }
            hand.add(state.draw(random));
            state.nextPlayer();
            return false;
        }

        Card card = hand.get(chosen);
        boolean ok = CardRules.isLegal(card.code(), state.upCardCode(), state.calledColor);

        if (!ok) {
            if (!quiet) {
                view.showIllegalCardPenalty(name, card.code());
            }
            hand.add(state.draw(random));
            state.nextPlayer();
            return false;
        }

        hand.remove(chosen);
        state.discard.add(state.upCard);
        state.upCard = card;
        state.calledColor = "";

        if (!quiet) {
            view.showPlayedCard(name, card.code());
        }

        if (card.isWild()) {
            if (state.humanPlayers.get(state.currentPlayer).booleanValue()) {
                state.calledColor = view.askColor();
            } else {
                state.calledColor = BotStrategy.chooseColor(hand);
            }
            if (!quiet) {
                view.showCalledColor(name, state.calledColor);
            }
        }

        if (hand.size() == 1 && !quiet) {
            view.showUno(name);
        }

        if (hand.size() == 0) {
            int points = ScoreCalculator.scoreForWinner(state.hands, state.currentPlayer);
            state.scores[state.currentPlayer] += points;
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
        state.discard.clear();
        for (int i = 0; i < state.hands.size(); i++) {
            state.hands.get(i).clear();
        }
        for (int i = 0; i < state.playerNames.size(); i++) {
            for (int j = 0; j < 7; j++) {
                state.hands.get(i).add(state.draw(random));
            }
        }
        state.upCard = state.draw(random);
        while (state.upCard.isWild()) {
            state.discard.add(state.upCard);
            state.upCard = state.draw(random);
        }
        state.calledColor = "";
        state.direction = 1;
        state.currentPlayer = random.nextInt(state.playerNames.size());
    }
}
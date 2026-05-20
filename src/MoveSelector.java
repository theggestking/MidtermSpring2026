import java.util.ArrayList;
import java.util.Random;

public class MoveSelector {
    private final GameState state;
    private final Random random;
    private final ConsoleView view;
    private final boolean quiet;

    MoveSelector(GameState state, Random random, ConsoleView view, boolean quiet) {
        this.state = state;
        this.random = random;
        this.view = view;
        this.quiet = quiet;
    }

    int chooseMove(String playerName) {
        ArrayList<Card> hand = state.currentHandSnapshot();

        int chosen;
        if (state.isCurrentPlayerHuman()) {
            chosen = chooseHumanMove(hand);
        } else {
            chosen = BotStrategy.chooseCard(hand, state.upCardCode(), state.calledColor());
        }

        return handleDrawIfNeeded(chosen, playerName);
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

    private int chooseHumanMove(ArrayList<Card> hand) {
        while (true) {
            String input = view.askMoveInput();

            if (input.equals("DRAW")) {
                return -1;
            }

            int index = InputParser.parseCardIndex(input, hand.size());
            if (index != -1) {
                return index;
            }

            int cardIndex = findCardCodeInHand(hand, input);
            if (cardIndex != -1) {
                Card card = hand.get(cardIndex);
                if (CardRules.isLegal(card.code(), state.upCardCode(), state.calledColor())) {
                    return cardIndex;
                }
                view.showIllegalSelection();
            } else {
                view.showCardNotFound();
            }
        }
    }

    private int findCardCodeInHand(ArrayList<Card> hand, String cardCode) {
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).code().equals(cardCode)) {
                return i;
            }
        }
        return -1;
    }
}
import java.util.ArrayList;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record MoveSelector(GameState state, Random random, GameView view, boolean quiet, PlayerStrategy botStrategy) {
    private static final Logger LOGGER = LoggerFactory.getLogger(MoveSelector.class);

    int chooseMove(String playerName) {
        ArrayList<Card> hand = state.currentHandSnapshot();

        int chosen;
        if (state.isCurrentPlayerHuman()) {
            chosen = chooseHumanMove(hand);
        } else {
            chosen = botStrategy.chooseCard(hand, state.upCardCode(), state.calledColor());
        }

        return handleDrawIfNeeded(chosen, playerName);
    }

    int handleDrawIfNeeded(int chosen, String name) {
        if (chosen != -1) {
            return chosen;
        }

        Card drawn = state.draw(random);
        state.addCardToCurrentPlayer(drawn);
        LOGGER.info("event=card_drawn player={} card={} reason=turn_draw", name, drawn.code());

        if (!quiet) {
            view.showDraw(name, drawn.code());
        }

        if (CardRules.isLegal(drawn, state.upCard(), state.calledColor())) {
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
                if (CardRules.isLegal(card, state.upCard(), state.calledColor())) {
                    return cardIndex;
                }
                LOGGER.warn("event=invalid_input player={} input={} reason=illegal_card", state.currentPlayerName(), input);
                view.showIllegalSelection();
            } else {
                LOGGER.warn("event=invalid_input player={} input={} reason=card_not_found", state.currentPlayerName(), input);
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

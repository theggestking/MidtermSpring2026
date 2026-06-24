import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TurnResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(TurnResolver.class);

    private final GameState state;
    private final Random random;
    private final GameView view;
    private final boolean quiet;
    private final PlayerStrategy botStrategy;

    TurnResolver(GameState state, Random random, GameView view, boolean quiet, PlayerStrategy botStrategy) {
        this.state = state;
        this.random = random;
        this.view = view;
        this.quiet = quiet;
        this.botStrategy = botStrategy;
    }

    boolean resolveChosenCard(int chosen, String name) {
        if (chosen < 0) {
            state.nextPlayer();
            return false;
        }

        if (chosen >= state.currentHandSize()) {
            Card penalty = state.draw(random);
            if (!quiet) {
                view.showInvalidIndexPenalty(name);
            }
            LOGGER.warn("event=invalid_input player={} input={} reason=index_out_of_range", name, chosen);
            LOGGER.info("event=card_drawn player={} card={} reason=invalid_index_penalty", name, penalty.code());
            state.addCardToCurrentPlayer(penalty);
            state.nextPlayer();
            return false;
        }

        Card card = state.cardInCurrentHand(chosen);
        boolean ok = CardRules.isLegal(card, state.upCard(), state.calledColor());

        if (!ok) {
            Card penalty = state.draw(random);
            if (!quiet) {
                view.showIllegalCardPenalty(name, card.code());
            }
            LOGGER.warn("event=invalid_input player={} input={} reason=illegal_card", name, card.code());
            LOGGER.info("event=card_drawn player={} card={} reason=illegal_card_penalty", name, penalty.code());
            state.addCardToCurrentPlayer(penalty);
            state.nextPlayer();
            return false;
        }

        state.removeCardFromCurrentHand(chosen);
        state.discardUpCard();
        state.setUpCard(card);
        state.clearCalledColor();
        LOGGER.info("event=card_played player={} card={} cards_remaining={}",
                name, card.code(), state.currentHandSize());

        if (!quiet) {
            view.showPlayedCard(name, card.code());
        }

        callColorIfWild(card, name);

        handleUnoCall(name);

        if (state.currentHandSize() == 0) {
            scoreWin(name);
            return true;
        }

        applyCardEffect(card);
        return false;
    }

    private void handleUnoCall(String name) {
        if (state.currentHandSize() != 1) {
            return;
        }

        if (!state.isCurrentPlayerHuman()) {
            LOGGER.info("event=uno_called player={}", name);
            if (!quiet) {
                view.showUno(name);
            }
            return;
        }

        if (view.askCallUno()) {
            LOGGER.info("event=uno_called player={}", name);
            if (!quiet) {
                view.showUno(name);
            }
            return;
        }

        LOGGER.warn("event=uno_penalty player={} reason=missed_uno", name);
        for (int penaltyCard = 0; penaltyCard < 2; penaltyCard++) {
            Card drawn = state.draw(random);
            state.addCardToCurrentPlayer(drawn);
            LOGGER.info("event=card_drawn player={} card={} reason=missed_uno_penalty", name, drawn.code());
        }
        if (!quiet) {
            view.showMissedUnoPenalty(name);
        }
    }

    private void callColorIfWild(Card card, String name) {
        if (!card.isWild()) {
            return;
        }

        if (state.isCurrentPlayerHuman()) {
            state.setCalledColor(view.askColor());
        } else {
            state.setCalledColor(botStrategy.chooseColor(state.currentHandSnapshot()));
        }

        if (!quiet) {
            view.showCalledColor(name, state.calledColor());
        }
    }

    private void scoreWin(String name) {
        int points = ScoreCalculator.scoreForWinner(state.handsSnapshot(), state.currentPlayerIndex());
        state.addScoreToPlayer(state.currentPlayerIndex(), points);
        LOGGER.info("event=game_end winner={} points={}", name, points);

        if (!quiet) {
            view.showWin(name, points);
        }
    }

    private void applyCardEffect(Card card) {
        String effectMessage = ActionEffects.apply(
                card,
                new GameStateTurnEffectContext(state, () -> state.draw(random)));
        if (!quiet && !effectMessage.equals("")) {
            view.showEffectMessage(effectMessage);
        }
    }
}

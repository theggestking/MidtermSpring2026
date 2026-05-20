import java.util.ArrayList;
import java.util.Random;

public class TurnController {
    private final GameState state;
    private final Random random;
    private final ConsoleView view;
    private final boolean quiet;
    private final MoveSelector moveSelector;
    private final TurnResolver turnResolver;

    TurnController(GameState state, Random random, ConsoleView view, boolean quiet) {
        this.state = state;
        this.random = random;
        this.view = view;
        this.quiet = quiet;
        this.moveSelector = new MoveSelector(state, random, view, quiet);
        this.turnResolver = new TurnResolver(state, random, view, quiet);
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
        int chosen = moveSelector.chooseMove(name);
        return turnResolver.resolveChosenCard(chosen, name);
    }

    private void renderTurn(String name, ArrayList<Card> hand) {
        if (!quiet) {
            view.showTurn(state.upCardCode(), state.calledColor(), name, hand);
        }
    }

    int handleDrawIfNeeded(int chosen, String name) {
        return moveSelector.handleDrawIfNeeded(chosen, name);
    }

    boolean resolveChosenCard(int chosen, String name) {
        return turnResolver.resolveChosenCard(chosen, name);
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
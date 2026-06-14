import java.util.ArrayList;
import java.util.Random;

public class TurnController {
    private final GameState state;
    private final Random random;
    private final GameView view;
    private final boolean quiet;
    private final MoveSelector moveSelector;
    private final TurnResolver turnResolver;
    private final PlayerStrategy botStrategy;

    TurnController(GameState state, Random random, GameView view, boolean quiet) {
        this(state, random, view, quiet, new BotStrategy());
    }

    TurnController(GameState state, Random random, GameView view, boolean quiet, PlayerStrategy botStrategy) {
        this.state = state;
        this.random = random;
        this.view = view;
        this.quiet = quiet;
        this.botStrategy = botStrategy;
        this.moveSelector = new MoveSelector(state, random, view, quiet, botStrategy);
        this.turnResolver = new TurnResolver(state, random, view, quiet, botStrategy);
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

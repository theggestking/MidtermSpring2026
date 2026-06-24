import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TurnController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TurnController.class);

    private final GameState state;
    private final Random random;
    private final GameView view;
    private final boolean quiet;
    private final MoveSelector moveSelector;
    private final TurnResolver turnResolver;
    private final PlayerStrategy botStrategy;
    private final Clock clock;

    TurnController(GameState state, Random random, GameView view, boolean quiet) {
        this(state, random, view, quiet, new BotStrategy(), Clock.systemUTC());
    }

    TurnController(GameState state, Random random, GameView view, boolean quiet, PlayerStrategy botStrategy) {
        this(state, random, view, quiet, botStrategy, Clock.systemUTC());
    }

    TurnController(
            GameState state,
            Random random,
            GameView view,
            boolean quiet,
            PlayerStrategy botStrategy,
            Clock clock) {
        this.state = state;
        this.random = random;
        this.view = view;
        this.quiet = quiet;
        this.botStrategy = botStrategy;
        this.clock = clock;
        this.moveSelector = new MoveSelector(state, random, view, quiet, botStrategy);
        this.turnResolver = new TurnResolver(state, random, view, quiet, botStrategy);
    }

    void playGame() {
        playRound(1);
    }

    CompletedRound playRound(int roundNumber) {
        Instant startedAt = clock.instant();
        List<Integer> scoresBefore = state.scoresSnapshot();
        startNewGame();

        int guard = 0;
        while (guard < 3000) {
            guard++;
            if (takeTurn()) {
                return completedRound(
                        roundNumber,
                        startedAt,
                        RoundStatus.COMPLETED,
                        state.currentPlayerName(),
                        scoresBefore);
            }
        }

        if (!quiet) {
            view.showSafetyLimit();
        }
        LOGGER.warn("event=game_end result=safety_limit turns={}", guard);
        return completedRound(roundNumber, startedAt, RoundStatus.SAFETY_LIMIT, null, scoresBefore);
    }

    boolean takeTurn() {
        String name = state.currentPlayerName();
        ArrayList<Card> hand = state.currentHandSnapshot();
        LOGGER.info("event=player_turn player={} hand_size={} up_card={}",
                name, hand.size(), state.upCardCode());
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
        int redraws = 0;
        while (state.upCard().rankValue() != CardRank.NUMBER && redraws < 108) {
            state.discardUpCard();
            state.setUpCard(state.draw(random));
            redraws++;
        }

        state.clearCalledColor();
        state.resetDirection();
        state.chooseRandomCurrentPlayer(random);
        LOGGER.info("event=game_start players={} starting_player={} up_card={}",
                state.playerCount(), state.currentPlayerName(), state.upCardCode());
    }

    private CompletedRound completedRound(
            int roundNumber,
            Instant startedAt,
            RoundStatus status,
            String winnerName,
            List<Integer> scoresBefore) {
        List<Integer> scoresAfter = state.scoresSnapshot();
        List<RoundPlayerScore> playerScores = new ArrayList<>(state.playerCount());
        int awardedPoints = 0;
        for (int player = 0; player < state.playerCount(); player++) {
            int scoreBefore = scoresBefore.get(player);
            int scoreAfter = scoresAfter.get(player);
            int scoreDelta = scoreAfter - scoreBefore;
            playerScores.add(new RoundPlayerScore(
                    state.playerName(player), scoreBefore, scoreDelta, scoreAfter));
            awardedPoints = Math.max(awardedPoints, scoreDelta);
        }

        return new CompletedRound(
                roundNumber,
                startedAt,
                clock.instant(),
                status,
                winnerName,
                awardedPoints,
                playerScores);
    }
}

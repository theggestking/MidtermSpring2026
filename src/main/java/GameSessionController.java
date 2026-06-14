import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

final class GameSessionController {
    private final GameState state;
    private final TurnController turnController;
    private final GameView view;
    private final boolean quiet;
    private final Clock clock;

    GameSessionController(
            GameState state,
            TurnController turnController,
            GameView view,
            boolean quiet,
            Clock clock) {
        this.state = state;
        this.turnController = turnController;
        this.view = view;
        this.quiet = quiet;
        this.clock = clock;
    }

    CompletedGame play(int requestedRounds) {
        if (requestedRounds < 1) {
            throw new IllegalArgumentException("Games must be at least 1");
        }

        Instant startedAt = clock.instant();
        List<CompletedRound> rounds = new ArrayList<>(requestedRounds);
        for (int roundNumber = 1; roundNumber <= requestedRounds; roundNumber++) {
            if (!quiet) {
                view.showGameHeader(roundNumber);
            }
            rounds.add(turnController.playRound(roundNumber));
        }

        view.showFinalScores(state);
        return new CompletedGame(
                startedAt,
                clock.instant(),
                requestedRounds,
                rounds,
                finalScores(rounds));
    }

    private List<FinalPlayerScore> finalScores(List<CompletedRound> rounds) {
        boolean hasCompletedRound = rounds.stream()
                .anyMatch(round -> round.status() == RoundStatus.COMPLETED);
        int highestScore = state.scoresSnapshot().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);

        List<FinalPlayerScore> scores = new ArrayList<>(state.playerCount());
        for (int player = 0; player < state.playerCount(); player++) {
            int score = state.scoreForPlayer(player);
            scores.add(new FinalPlayerScore(
                    state.playerName(player),
                    player,
                    score,
                    hasCompletedRound && score == highestScore));
        }
        return List.copyOf(scores);
    }
}

import java.time.Instant;
import java.util.List;
import java.util.Objects;

record CompletedGame(
        Instant startedAt,
        Instant completedAt,
        int requestedRounds,
        List<CompletedRound> rounds,
        List<FinalPlayerScore> finalScores) {

    CompletedGame {
        Objects.requireNonNull(startedAt, "startedAt");
        Objects.requireNonNull(completedAt, "completedAt");
        Objects.requireNonNull(rounds, "rounds");
        Objects.requireNonNull(finalScores, "finalScores");
        if (requestedRounds < 1) {
            throw new IllegalArgumentException("Requested rounds must be positive");
        }
        if (rounds.size() > requestedRounds) {
            throw new IllegalArgumentException("Completed rounds cannot exceed requested rounds");
        }
        if (completedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("Game completion cannot precede its start");
        }
        if (finalScores.isEmpty()) {
            throw new IllegalArgumentException("A game must contain players");
        }
        rounds = List.copyOf(rounds);
        finalScores = List.copyOf(finalScores);
    }

    int completedRounds() {
        return rounds.size();
    }
}

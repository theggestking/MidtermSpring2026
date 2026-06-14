import java.time.Instant;
import java.util.List;
import java.util.Objects;

record CompletedRound(
        int roundNumber,
        Instant startedAt,
        Instant completedAt,
        RoundStatus status,
        String winnerName,
        int awardedPoints,
        List<RoundPlayerScore> playerScores) {

    CompletedRound {
        Objects.requireNonNull(startedAt, "startedAt");
        Objects.requireNonNull(completedAt, "completedAt");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(playerScores, "playerScores");
        if (roundNumber < 1) {
            throw new IllegalArgumentException("Round number must be positive");
        }
        if (completedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("Round completion cannot precede its start");
        }
        if (awardedPoints < 0) {
            throw new IllegalArgumentException("Awarded points cannot be negative");
        }
        if (status == RoundStatus.COMPLETED && (winnerName == null || winnerName.isBlank())) {
            throw new IllegalArgumentException("A completed round must have a winner");
        }
        if (status == RoundStatus.SAFETY_LIMIT && winnerName != null) {
            throw new IllegalArgumentException("A safety-limit round cannot have a winner");
        }
        playerScores = List.copyOf(playerScores);
    }
}

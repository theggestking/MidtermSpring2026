import java.time.Instant;
import java.util.List;

record RecentGameReport(
        long gameId,
        Instant completedAt,
        int requestedRounds,
        int completedRounds,
        List<FinalPlayerScore> finalScores) {

    RecentGameReport {
        finalScores = List.copyOf(finalScores);
    }
}
